package com.platformia.winkwide.admin.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Spot;
import com.platformia.winkwide.core.exception.ApiError;
import com.platformia.winkwide.core.repository.MediaRepository;
import com.platformia.winkwide.core.repository.PlaylistRepository;
import com.platformia.winkwide.core.repository.SpotRepository;

@RepositoryRestController
public class PlaylistRepositoryController {

	private final PlaylistRepository playlistRepo;
	private final SpotRepository spotRepo;
	private final MediaRepository mediaRepo;

	@Autowired
	public PlaylistRepositoryController(PlaylistRepository pRepo, SpotRepository sRepo, MediaRepository mRepo) {
		playlistRepo = pRepo;
		spotRepo = sRepo;
		mediaRepo = mRepo;
	}

	@PostMapping("/playlists/creates")
	public @ResponseBody ResponseEntity<?> createPlaylist(@RequestBody Playlist playlist) {

		try {

			// first, check validation and send back errors
			if (playlist == null)
				return createValidationErrors("NotEmpty.playlist.object");
			if (playlist.getName() == null || playlist.getName().isEmpty())
				return createValidationErrors("NotEmpty.playlist.name");
			if (playlistRepo.findByName(playlist.getName()) != null)
				return createValidationErrors("Duplicate.playlist.name");
			if (playlist.getSpots() == null || playlist.getSpots().isEmpty())
				return createValidationErrors("NotEmpty.playlist.spots");
			// --------------------------------------------

			Playlist newPlaylist = new Playlist();
			ArrayList<Spot> spots = new ArrayList<Spot>();
			Long playlistDuration = new Long(0);

			for (Spot s : playlist.getSpots()) {
				Optional<Media> media = mediaRepo.findById(s.getMedia().getId());

				Spot spot = new Spot();
				spot.setDuration(s.getDuration());
				spot.setPlayOrder(s.getPlayOrder());
				spot.setMedia(media.get());
				spots.add(spot);

				playlistDuration += spot.getDuration();
			}

			newPlaylist.setName(playlist.getName());
			newPlaylist.setDuration(playlistDuration);
			newPlaylist.setSpots(spots);

			playlistRepo.save(newPlaylist);

			for (Spot s : spots)
				s.setPlaylist(newPlaylist);
			spotRepo.saveAll(spots);

			Resource<Playlist> resource = new Resource<Playlist>(newPlaylist);

			return ResponseEntity.ok(resource);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping("/playlists/updates")
	public @ResponseBody ResponseEntity<?> savePlaylist(@RequestBody Playlist playlist) {

		try {

			// first, check validation and send back errors
			if (playlist == null)
				return createValidationErrors("NotEmpty.playlist.object");
			if (playlist.getId() == null)
				return createValidationErrors("NotEmpty.playlist.id");

			Optional<Playlist> oldPlaylist = playlistRepo.findById(playlist.getId());
			if (!oldPlaylist.isPresent())
				return createValidationErrors("NotFound.playlist.id");
			
			if (playlist.getName() == null || playlist.getName().isEmpty())
				return createValidationErrors("NotEmpty.playlist.name");
			if( !playlist.getName().equals(oldPlaylist.get().getName()) 
					&& playlistRepo.findByName(playlist.getName()) != null)
				return createValidationErrors("Duplicate.playlist.name");
			
			if (playlist.getSpots() == null || playlist.getSpots().isEmpty())
				return createValidationErrors("NotEmpty.playlist.spots");
			// --------------------------------------------

			ArrayList<Spot> oldSpots = (ArrayList<Spot>) spotRepo.findByPlaylistId(playlist.getId());

			ArrayList<Spot> spots = new ArrayList<Spot>();
			Long playlistDuration = new Long(0);

			for (Spot s : playlist.getSpots()) {
				Optional<Media> media = mediaRepo.findById(s.getMedia().getId());

				Spot spot = new Spot();
				spot.setDuration(s.getDuration());
				spot.setPlayOrder(s.getPlayOrder());
				spot.setMedia(media.get());
				spots.add(spot);

				playlistDuration += spot.getDuration();
			}

			spotRepo.deleteAll(oldSpots);

			oldPlaylist.get().setName(playlist.getName());
			oldPlaylist.get().setDuration(playlistDuration);
			oldPlaylist.get().setSpots(spots);

			playlistRepo.save(oldPlaylist.get());

			// saving the relations
			for (Spot s : spots)
				s.setPlaylist(oldPlaylist.get());
			spotRepo.saveAll(spots);

			Resource<Playlist> resource = new Resource<Playlist>(oldPlaylist.get());

			return ResponseEntity.ok(resource);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@DeleteMapping("/playlists/{playlistId}")
	public @ResponseBody ResponseEntity<?> deletePlaylist(@PathVariable Long playlistId) {

		try {
			
			// first, check validation and send back errors
			/*if (playlistId == null)
				return createValidationErrors("NotEmpty.playlist.id");
			else if (playlistRepo.findById(playlistId) == null)
				return createValidationErrors("NotFound.playlist.id");*/
			// ---------------------------------
			
			Optional<Playlist> playlist = playlistRepo.findById(playlistId);
			if (playlist.isPresent()) {
				playlistRepo.deletePlaylistSpotLinks(playlist.get().getId());
				playlistRepo.deletePlaylistProgramLinks(playlist.get().getId());
				playlistRepo.delete(playlist.get());
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
		case "NotEmpty.playlist.object":
			message = "No playlist object in your request!";
			errors.add("\"playlist\": \"NotEmpty.playlist.object\"");
			break;
		case "NotEmpty.playlist.name":
			message = "You must specify a name for your list!";
			errors.add("\"name\": \"NotEmpty.playlist.name\"");
			break;
		case "Duplicate.playlist.name":
			message = "A playlist with Duplicate name exists, please change the name!";
			errors.add("\"name\": \"Duplicate.playlist.name\"");
			break;
		case "NotEmpty.playlist.spots":
			message = "You must specify at least one spot for your list!";
			errors.add("\"spots\": \"NotEmpty.playlist.spots\"");
			break;
		case "NotEmpty.playlist.id":
			message = "You must specify a playlist Id!";
			errors.add("\"id\": \"NotEmpty.playlist.id\"");
			break;
		case "NotFound.playlist.id":
			message = "The playlist you want to edit was not found!";
			errors.add("\"id\": \"NotFound.playlist.id\"");
			break;

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors), httpHeaders, httpStatus);
	}

}
