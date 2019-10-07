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

import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.exception.ApiError;
import com.platformia.winkwide.core.repository.DisplayRepository;
import com.platformia.winkwide.core.repository.PlaylistRepository;
import com.platformia.winkwide.core.repository.ProgramRepository;

@RepositoryRestController
public class ProgramRepositoryController {

	private final ProgramRepository programRepo;
	private final DisplayRepository displayRepo;
	private final PlaylistRepository playlistRepo;

	@Autowired
	public ProgramRepositoryController(ProgramRepository progRepo, DisplayRepository dispRepo,
			PlaylistRepository playRepo) {
		programRepo = progRepo;
		displayRepo = dispRepo;
		playlistRepo = playRepo;
	}

	@PostMapping("/programs/creates")
	public @ResponseBody ResponseEntity<?> createProgram(@RequestBody Program program) {

		try {

			// first, check validation and send back errors
			if (program == null)
				return createValidationErrors("NotEmpty.program.object");
			if (program.getName() == null || program.getName().isEmpty())
				return createValidationErrors("NotEmpty.program.name");
			if (programRepo.findByName(program.getName()) != null)
				return createValidationErrors("Duplicate.program.name");
			if (program.getDisplays() == null || program.getDisplays().isEmpty())
				return createValidationErrors("NotEmpty.program.displays");
			if (program.getPlaylists() == null || program.getPlaylists().isEmpty())
				return createValidationErrors("NotEmpty.program.playlists");
			// --------------------------------------------

			Program newProgram = new Program();

			ArrayList<Display> displays = new ArrayList<Display>();
			ArrayList<Playlist> playlists = new ArrayList<Playlist>();

			for (Display d : program.getDisplays()) {
				Display display = displayRepo.getOne(d.getId());
				displays.add(display);
			}

			for (Playlist p : program.getPlaylists()) {
				Playlist playlist = playlistRepo.getOne(p.getId());
				playlists.add(playlist);
			}

			newProgram.setName(program.getName());
			newProgram.setStartTime(program.getStartTime());
			newProgram.setEndTime(program.getEndTime());
			newProgram.setDisplays(displays);
			newProgram.setPlaylists(playlists);

			programRepo.save(newProgram);

			Resource<Program> resource = new Resource<Program>(newProgram);

			return ResponseEntity.ok(resource);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping("/programs/updates")
	public @ResponseBody ResponseEntity<?> saveProgram(@RequestBody Program program) {

		try {

			// first, check validation and send back errors
			if (program == null)
				return createValidationErrors("NotEmpty.program.object");
			if (program.getId() == null)
				return createValidationErrors("NotEmpty.program.id");
			
			Optional<Program> oldProgram = programRepo.findById(program.getId());
			if(!oldProgram.isPresent())
				return createValidationErrors("NotFound.program.id");
			
			if (program.getName() == null || program.getName().isEmpty())
				return createValidationErrors("NotEmpty.program.name");
			if( !program.getName().equals(oldProgram.get().getName()) 
					&& programRepo.findByName(program.getName()) != null)
				return createValidationErrors("Duplicate.program.name");
			
			if (program.getDisplays() == null || program.getDisplays().isEmpty())
				return createValidationErrors("NotEmpty.program.displays");
			if (program.getPlaylists() == null || program.getPlaylists().isEmpty())
				return createValidationErrors("NotEmpty.program.playlists");
			// --------------------------------------------

			ArrayList<Display> displays = new ArrayList<Display>();
			ArrayList<Playlist> playlists = new ArrayList<Playlist>();

			for (Display d : program.getDisplays()) {
				Display display = displayRepo.getOne(d.getId());
				displays.add(display);
			}

			for (Playlist p : program.getPlaylists()) {
				Playlist playlist = playlistRepo.getOne(p.getId());
				playlists.add(playlist);
			}

			oldProgram.get().setName(program.getName());
			oldProgram.get().setStartTime(program.getStartTime());
			oldProgram.get().setEndTime(program.getEndTime());
			oldProgram.get().setDisplays(displays);
			oldProgram.get().setPlaylists(playlists);

			programRepo.save(oldProgram.get());

			Resource<Program> resource = new Resource<Program>(oldProgram.get());

			return ResponseEntity.ok(resource);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@DeleteMapping("/programs/{programId}")
	public @ResponseBody ResponseEntity<?> deleteProgram(@PathVariable Long programId) {
		try {
			
			// first, check validation and send back errors
			/*if (programId == null)
				return createValidationErrors("NotEmpty.program.id");
			else if (playlistRepo.findById(programId) == null)
				return createValidationErrors("NotFound.program.id");*/
			// ---------------------------------
			
			Optional<Program> program = programRepo.findById(programId);
			if (program.isPresent()) {

				// delete links with objects
				programRepo.deleteProgramDisplayLinks(program.get().getId());
				programRepo.deleteProgramPlaylistLinks(program.get().getId());
				programRepo.delete(program.get());
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
		case "NotEmpty.program.object":
			message = "No program object in your request!";
			errors.add("\"program\": \"NotEmpty.program.object\"");
			break;
		case "NotEmpty.program.name":
			message = "You must specify a name for your list!";
			errors.add("\"name\": \"NotEmpty.program.name\"");
			break;
		case "Duplicate.program.name":
			message = "A program with Duplicate name exists, please change the name!";
			errors.add("\"name\": \"Duplicate.program.name\"");
			break;
		case "NotEmpty.program.displays":
			message = "You must specify at least one display for your list!!";
			errors.add("\"displays\": \"NotEmpty.program.displays\"");
			break;
		case "NotEmpty.program.playlists":
			message = "You must specify at least one playlist for your list!!";
			errors.add("\"playlists\": \"NotEmpty.program.playlists\"");
			break;
		case "NotEmpty.program.id":
			message = "You must specify a program Id!";
			errors.add("\"id\": \"NotEmpty.program.id\"");
			break;
		case "NotFound.program.id":
			message = "The program you want to edit was not found!";
			errors.add("\"id\": \"NotFound.program.id\"");
			break;

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors), httpHeaders, httpStatus);
	}

}
