package com.platformia.winkwide.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.platformia.winkwide.entity.Media;
import com.platformia.winkwide.exception.ApiError;
import com.platformia.winkwide.repository.MediaRepository;
import com.platformia.winkwide.service.FileStorageService;

@RepositoryRestController
public class MediaRepositoryController {

    private final MediaRepository repository;

    @Autowired
    public MediaRepositoryController(MediaRepository repo) { 
        repository = repo;
    }
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/medias/uploads") 
    public @ResponseBody ResponseEntity<?> createMedia(
												@RequestParam("name") String name, 
												@RequestParam("mediaType") String mediaType,
												@RequestParam("file") MultipartFile file) {
		
    	//first, check and send back errors       		
    	if(mediaType==null || mediaType.isEmpty())
			return createValidationErrors("NotEmpty.media.mediaType");
    	else if(repository.findByName(name) != null)
			return createValidationErrors("Duplicate.media.name");
    	
    	Media newMedia = new Media();
    	newMedia.setName(name);
    	newMedia.setMediaType(mediaType);
    	
    	if(file!=null)
    		newMedia = fileStorageService.storeFile(name, mediaType, file);
    		
        repository.save(newMedia);
    	    	
        Resource<Media> resource = new Resource<Media>(newMedia); 
        
        return ResponseEntity.ok(resource); 
    }
    
    @PostMapping("/medias/updates") 
    public @ResponseBody ResponseEntity<?> saveMedia(
												@RequestParam("name") String name, 
												@RequestParam("mediaType") String mediaType,
												@RequestParam(value = "file", required=false) MultipartFile file) {
        	

    	
        Media oldMedia = repository.findByName(name);
        
    	//first, check and send back errors       		
    	if(mediaType==null || mediaType.isEmpty())
			return createValidationErrors("NotEmpty.media.mediaType");
    	else if(oldMedia == null)
			return createValidationErrors("NotFound.media.name");
        
    	//then do the patch
    	Media newMedia = oldMedia;
    	newMedia.setMediaType(mediaType);
    	
    	if(file!=null){
    		newMedia = fileStorageService.storeFile(name, mediaType, file);
    		repository.delete(oldMedia);
    		fileStorageService.deleteFile(oldMedia.getUrl());
    	}
        
        repository.save(newMedia);
        
        Resource<Media> resource = new Resource<Media>(newMedia); 
        return ResponseEntity.ok(resource);
        
    }
    
   @DeleteMapping("/medias/{mediaId}") 
    public @ResponseBody ResponseEntity<?> deleteMedia(@PathVariable Long mediaId) {
        	
        Optional<Media> oldMedia = repository.findById(mediaId);    
        if(oldMedia.isPresent()) {
        	repository.delete(oldMedia.get());
        	fileStorageService.deleteFile(oldMedia.get().getUrl());
        }
        return ResponseEntity.ok().build();
            	    	        
    }
   
   //method for creating error messages to be sent back
   private ResponseEntity<Object> createValidationErrors(String errorType) {
	   
  		HttpStatus httpStatus = HttpStatus.NOT_ACCEPTABLE;
		String message = "Please review you request!" ;
		List<String> errors = new ArrayList<String>();
		
	   switch (errorType) {
				case "NotEmpty.media.mediaType":
					message = "You must specify a Media Type!" ;
					errors.add("\"mediaType\": \"NotEmpty.media.mediaType\"");
					break;
				case "Duplicate.media.name":
					message = "A media with Duplicate name exists, please change the name!" ;
					errors.add("\"name\": \"Duplicate.media.name\"");
					break;
				case "NotFound.media.name":
					message = "The media you want to edit was not found, please change the name!" ;
					errors.add("\"name\": \"NotFound.media.name\"");
					break;

				}
  		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors),
				httpHeaders, httpStatus);
   }
   
	
}
