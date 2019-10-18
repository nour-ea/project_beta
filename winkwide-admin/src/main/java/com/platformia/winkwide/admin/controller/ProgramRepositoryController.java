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
import com.platformia.winkwide.core.utils.AdminSettingsProperties;

@RepositoryRestController
public class ProgramRepositoryController {

	private final ProgramRepository programRepo;
	private final DisplayRepository displayRepo;
	private final PlaylistRepository playlistRepo;

	@Autowired
	AdminSettingsProperties adminSettingsProperties;
	
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

			//check if Max Spots per Display is reached
			/*String maxDisplayConstraints = getMaxDisplayConstraints(program, displays);
			if( !maxDisplayConstraints.isEmpty())
				return createValidationErrors( maxDisplayConstraints);*/

						
			//create new program
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
		case "Limitation.program.maxDisplaySpots":
			message = "Max Spots per Display is exceeded! the maximum is set to: " + adminSettingsProperties.getMaxDisplaySpots();
			errors.add("\"id\": \"Limitation.program.maxDisplaySpots\"");
			break;
		case "Limitation.program.maxDisplayLoopTime":
			message = "Max Loop Time per Display is exceeded! The maximum is set to: " + adminSettingsProperties.getMaxDisplayLoopTime();
			errors.add("\"id\": \"Limitation.program.maxDisplayLoopTime\"");
			break;

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors), httpHeaders, httpStatus);
	}
	
	
	
	
	// method for checking if Max Spots per Display or Max Loop Time per Display is reached
	/*private String getMaxDisplayConstraints(Program program, ArrayList<Display> displays) {

		//Cutting time into section to study overlap constrains on display spots and loop time ;)
		
		ArrayList<Program> overlapPrograms = new ArrayList<Program>();
		
		ArrayList<Date> sectionDates = new ArrayList<Date>();
		ArrayList<Program> sectionOverlapPrograms = new ArrayList<Program>();
		ArrayList<Playlist> sectionOverlapPlaylists = new ArrayList<Playlist>();
		
		int sectionOverlapSpotsNumber = 0;
		long sectionOverlapLoopTime = 0;
		
		for (Display display : displays) {
			overlapPrograms.clear();
			overlapPrograms.add(program);
			//Get all programs overlapping with the currently created/edited one
			overlapPrograms.addAll( programRepo.findByCustomFilters(null, display.getId(), program.getStartTime(), program.getEndTime(), null, null, null).getContent());
			overlapPrograms.addAll( programRepo.findByCustomFilters(null, display.getId(), null, null, program.getStartTime(), program.getEndTime(), null).getContent());
			overlapPrograms.addAll( programRepo.findByCustomFilters(null, display.getId(), new Date(2000, 1, 1), program.getStartTime(), program.getEndTime(), new Date(2100, 1, 1), null).getContent());
			
			//Unique overlapping programs
			overlapPrograms = (ArrayList<Program>) overlapPrograms.stream().distinct().collect(Collectors.toList());
			
			//Build time sections Dates Aggregate Programs start and End Time
			sectionDates.clear();
			for (Program prog : overlapPrograms) {
				sectionDates.add(prog.getStartTime());
				sectionDates.add(prog.getEndTime());				
			}
			
			//and sort them
			sectionDates.sort(new Comparator<Date>() {
				@Override
				public int compare(Date d1, Date d2) {
					return Long.valueOf(d1.getTime()).compareTo(d2.getTime());
				}
			});
			
			//then on each section consider overlapping programs playlists
			for (int i=0; i < sectionDates.size() -1; i++) {
				
				Date sectionStartTime = sectionDates.get(i);
				Date sectionEndTime = sectionDates.get(i+1);
				
				
				//filter overlapPrograms on time section
				sectionOverlapPrograms.clear();
				sectionOverlapPrograms = (ArrayList<Program>) overlapPrograms.stream()
											.filter(prog -> prog.getStartTime().getTime() <= sectionStartTime.getTime() 
															&& prog.getEndTime().getTime() >= sectionEndTime.getTime())
											.collect(Collectors.toList());      
				
				//build section overlapping playlists
				sectionOverlapPlaylists.clear();
				for (Program p : sectionOverlapPrograms)
					sectionOverlapPlaylists.addAll(p.getPlaylists());
					
				//count spots in overlapping playlists
				sectionOverlapSpotsNumber = 0;
				sectionOverlapLoopTime = 0;
				for (Playlist playlist : sectionOverlapPlaylists) {
					sectionOverlapSpotsNumber += playlist.getSpots().size();
					sectionOverlapLoopTime += playlist.getDuration();
				}
				
				//check whether MaxDisplaySpots is exceeded
				if (adminSettingsProperties.getMaxDisplaySpots() < sectionOverlapSpotsNumber)
					return "Limitation.program.maxDisplaySpots";
				
				//check whether MaxDisplayLoopTime is exceeded
				if (adminSettingsProperties.getMaxDisplayLoopTime() < sectionOverlapLoopTime)
					return "Limitation.program.maxDisplayLoopTime";				
				
			}
			
			//empty objects
			overlapPrograms.clear();
			sectionDates.clear();
		}		
		
		return "";
	}*/
}
