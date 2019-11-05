package com.platformia.winkwide.admin.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.entity.Record;
import com.platformia.winkwide.core.exception.ApiError;
import com.platformia.winkwide.core.model.FileProperties;
import com.platformia.winkwide.core.repository.MediaRepository;
import com.platformia.winkwide.core.repository.RecordRepository;
import com.platformia.winkwide.core.service.FileStorageService;
import com.platformia.winkwide.core.utils.AdminSettingsProperties;

@EnableConfigurationProperties({ AdminSettingsProperties.class })
@RepositoryRestController
public class MediaRepositoryController {

	private final MediaRepository mediaRepo;
	private final RecordRepository recordRepo;

	@Autowired
	public MediaRepositoryController(MediaRepository mRepo, RecordRepository rRepo) {
		mediaRepo = mRepo;
		recordRepo = rRepo;
	}

	@Autowired
	private AdminSettingsProperties adminSettingsProperties;
	
	@Autowired
	private FileStorageService fileStorageService;

	@PostMapping("/medias/uploads")
	public @ResponseBody ResponseEntity<?> createMedia(@RequestParam("name") String name,
			@RequestParam("category") String category, @RequestParam("file") MultipartFile file,
			@RequestParam("thumbFile") MultipartFile thumbFile) {

		try {

			// check if Max Media Objects would be exceeded
			if( isMaxMediaObjectsReached())
				return createValidationErrors("Limitation.media.maxObjects");
			
			// check validation and send back errors
			if (name == null || name.isEmpty())
				return createValidationErrors("NotEmpty.media.name");
			if (mediaRepo.findByName(name) != null)
				return createValidationErrors("Duplicate.media.name");
			if (category == null || category.isEmpty())
				return createValidationErrors("NotEmpty.media.category");
			// ---------------------------------

			Media newMedia = new Media();
			FileProperties fileProperties = new FileProperties();
			FileProperties thumbFileProperties = new FileProperties();

			// check file and thumbnail
			if (file == null)
				return createValidationErrors("NotEmpty.media.file");
			if (!Arrays.stream(Media.allowedMediaFormats).anyMatch(file.getContentType()::equals))
				return createValidationErrors("NotValid.media.fileFormat");
			if (thumbFile == null)
				return createValidationErrors("NotEmpty.media.thumbFile");
			if (!Arrays.stream(Media.allowedThumbnailFormats).anyMatch(thumbFile.getContentType()::equals))
				return createValidationErrors("NotValid.media.thumbFileFormat");

			// check if Max Storage would be exceeded
			if( isMaxStorageSizeReached(file.getSize() + thumbFile.getSize() ))
				return createValidationErrors("Limitation.media.maxStorage");
			
			// then store file and thumbnail and generate media type
			if (file.getContentType().equals("text/html")) {
				fileProperties = fileStorageService.storeMediaFile("", "/apps/feed", file);
				newMedia.setType("App");
				thumbFileProperties = fileStorageService.storeMediaFile("", "/thumbnails/apps/feed", thumbFile);

			} else {
				fileProperties = fileStorageService.storeMediaFile("", "/medias/" + category, file);
				thumbFileProperties = fileStorageService.storeMediaFile("", "/thumbnails/medias/" + category, thumbFile);

				if (Arrays.stream(Media.allowedThumbnailFormats).anyMatch(file.getContentType()::equals))
					newMedia.setType("Image");
				else if (file.getContentType().equals("video/mp4"))
					newMedia.setType("Video");
				else if (file.getContentType().equals("audio/mp3"))
					newMedia.setType("Audio");
			}

			// populate media object
			newMedia.setName(name);
			newMedia.setCategory(category);
			newMedia.setVerified(false);

			newMedia.setFormat(fileProperties.getFormat());
			newMedia.setSize(fileProperties.getSize());
			newMedia.setUrl(fileProperties.getUrl());
			newMedia.setThumbUrl(thumbFileProperties.getUrl());

			mediaRepo.save(newMedia);

			Resource<Media> resource = new Resource<Media>(newMedia);

			return ResponseEntity.ok(resource);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping("/medias/updates")
	public @ResponseBody ResponseEntity<?> saveMedia(@RequestParam("id") Long id, @RequestParam("name") String name,
			@RequestParam("category") String category, @RequestParam("verified") Boolean verified,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "thumbFile", required = false) MultipartFile thumbFile) {

		try {

			// first, check validation and send back errors
			if (id == null)
				return createValidationErrors("NotEmpty.media.id");

			Optional<Media> oldMedia = mediaRepo.findById(id);
			if (!oldMedia.isPresent())
				return createValidationErrors("NotFound.media.id");

			if (name == null || name.isEmpty())
				return createValidationErrors("NotEmpty.media.name");
			if (!name.equals(oldMedia.get().getName()) && mediaRepo.findByName(name) != null)
				return createValidationErrors("Duplicate.media.name");

			if (category == null || category.isEmpty())
				return createValidationErrors("NotEmpty.media.category");
			// ---------------------------------

			// then do the patch with files update

			oldMedia.get().setVerified(verified);

			if (file != null) {				
				FileProperties fileProperties = new FileProperties();

				// check if Max Storage would be exceeded
				if( isMaxStorageSizeReached(file.getSize()))
					return createValidationErrors("Limitation.media.maxStorage");
				
				// check file formats
				if (!Arrays.stream(Media.allowedMediaFormats).anyMatch(file.getContentType()::equals))
					return createValidationErrors("NotValid.media.fileFormat");

				// store file
				if (file.getContentType().equals("text/html")) {
					fileProperties = fileStorageService.storeMediaFile("", "/apps/feed", file);
					oldMedia.get().setType("App");
				} else {
					fileProperties = fileStorageService.storeMediaFile("", "/medias/" + category, file);

					if (Arrays.stream(Media.allowedThumbnailFormats).anyMatch(file.getContentType()::equals))
						oldMedia.get().setType("Image");
					else if (file.getContentType().equals("video/mp4"))
						oldMedia.get().setType("Video");
					else if (file.getContentType().equals("audio/mp3"))
						oldMedia.get().setType("Audio");
				}

				// delete old file
				fileStorageService.deleteMediaFile(oldMedia.get().getUrl());

				oldMedia.get().setFormat(fileProperties.getFormat());
				oldMedia.get().setSize(fileProperties.getSize());
				oldMedia.get().setUrl(fileProperties.getUrl());
				oldMedia.get().setVerified(false);

			}

			if (thumbFile != null) {
				FileProperties thumbFileProperties = new FileProperties();

				// check if Max Storage would be exceeded
				if( isMaxStorageSizeReached(thumbFile.getSize()))
					return createValidationErrors("Limitation.media.maxStorage");

				// check thumbnail format
				if (!Arrays.stream(Media.allowedThumbnailFormats).anyMatch(thumbFile.getContentType()::equals))
					return createValidationErrors("NotValid.media.thumbFileFormat");

				// store thumbnail
				if (oldMedia.get().getType().equals("App"))
					thumbFileProperties = fileStorageService.storeMediaFile("", "/thumbnails/apps/feed", thumbFile);
				else
					thumbFileProperties = fileStorageService.storeMediaFile("", "/thumbnails/medias/" + category, thumbFile);

				// delete old thumbnail
				fileStorageService.deleteMediaFile(oldMedia.get().getThumbUrl());

				oldMedia.get().setThumbUrl(thumbFileProperties.getUrl());

			}

			oldMedia.get().setName(name);
			oldMedia.get().setCategory(category);

			mediaRepo.save(oldMedia.get());

			Resource<Media> resource = new Resource<Media>(oldMedia.get());
			return ResponseEntity.ok(resource);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@DeleteMapping("/medias/{mediaId}")
	public @ResponseBody ResponseEntity<?> deleteMedia(@PathVariable Long mediaId) {

		try {

			Optional<Media> media = mediaRepo.findById(mediaId);
			if (media.isPresent()) {
				//Never delete a Media that has linked Reports
				List<Record> records = recordRepo.findByMediaId(mediaId);
				if(records!=null && !records.isEmpty())
					return createValidationErrors("NotPermitted.media.reports");
				
				mediaRepo.deleteMediaSpotLinks(media.get().getId());
				mediaRepo.deleteMediaRecordLinks(media.get().getId());
				mediaRepo.delete(media.get());
				fileStorageService.deleteMediaFile(media.get().getUrl());
				fileStorageService.deleteMediaFile(media.get().getThumbUrl());
			}
			return ResponseEntity.ok().build();

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	// method for creating error messages to be sent back
	private ResponseEntity<Object> createValidationErrors(String errorType) {

		HttpStatus httpStatus = HttpStatus.NOT_ACCEPTABLE;
		String message = "Please review you request!";
		List<String> errors = new ArrayList<String>();

		switch (errorType) {
		case "NotEmpty.media.category":
			message = "You must specify a media category!";
			errors.add("\"category\": \"NotEmpty.media.category\"");
			break;
		case "NotEmpty.media.name":
			message = "You must specify a name for your list!";
			errors.add("\"name\": \"NotEmpty.media.name\"");
			break;
		case "Duplicate.media.name":
			message = "A media with Duplicate name exists, please change the name!";
			errors.add("\"name\": \"Duplicate.media.name\"");
			break;
		case "NotEmpty.media.id":
			message = "You must specify a media Id!";
			errors.add("\"id\": \"NotEmpty.media.id\"");
			break;
		case "NotFound.media.id":
			message = "The media you want to edit was not found!";
			errors.add("\"id\": \"NotFound.media.id\"");
			break;
		case "NotEmpty.media.file":
			message = "You must specify a media file! (accepted formats: jpeg, jpg, png, mp4, mp3, html)";
			errors.add("\"file\": \"NotValid.media.file\"");
			break;
		case "NotValid.media.fileFormat":
			message = "Media file format not accepted! (accepted formats: jpeg, jpg, png, mp4, mp3, html)";
			errors.add("\"fileFormat\": \"NotValid.media.fileFormat\"");
			break;
		case "NotEmpty.media.thumbFile":
			message = "You must specify a media thumbnail file! (accepted formats: jpeg, jpg, png, gif)";
			errors.add("\"thumbFile\": \"NotValid.media.thumbFile\"");
			break;
		case "NotValid.media.thumbFileFormat":
			message = "Media thumbnail file format not accepted! (accepted formats: jpeg, jpg, png, gif)";
			errors.add("\"thumbFileFormat\": \"NotValid.media.thumbFileFormat\"");
			break;
		case "Limitation.media.maxStorage":
			message = "Max Media Storage exceeded, max is now set to: " + Math.round(adminSettingsProperties.getMaxStorageSize()/1000000) + "MB";
			errors.add("\"id\": \"Limitation.media.maxStorage\"");
			break;
		case "Limitation.media.maxObjects":
			message = "Max Media objects exceeded, max is set to: " + adminSettingsProperties.getMaxMedias();
			errors.add("\"id\": \"Limitation.media.maxObjects\"");
			break;
		case "NotPermitted.media.reports":
			message = "Can't delete Media because linked Reports exist. Please review them first!";
			errors.add("\"reports\": \"NotPermitted.media.reports\"");
			break;
		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors), httpHeaders, httpStatus);
	}

	// method for checking if Max Storage setting is reached
	private boolean isMaxStorageSizeReached(Long fileSize) {

		if (adminSettingsProperties.getMaxStorageSize() < fileStorageService.getUploadsDirectorySize() + fileSize)
			return true;
		else
			return false;
	}

	// method for checking if Max Media Objects setting is reached
	private boolean isMaxMediaObjectsReached() {

		if (adminSettingsProperties.getMaxMedias() < mediaRepo.count() + 1)
			return true;
		else
			return false;
	}

}
