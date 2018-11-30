package com.platformia.winkwide.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.repository.DisplayRepository;
import com.platformia.winkwide.core.repository.MediaRepository;
import com.platformia.winkwide.core.repository.ProgramRepository;
import com.platformia.winkwide.core.repository.ReportRepository;

//@RepositoryRestController
@RestController
public class SmartTVSyncController {

	private final DisplayRepository displayRepo;
	private final MediaRepository mediaRepo;
	private final ProgramRepository programRepo;
	private final ReportRepository reportRepo;

	@Autowired
	public SmartTVSyncController(DisplayRepository dRepo, MediaRepository mRepo, ProgramRepository pRepo,  ReportRepository rRepo) {
		displayRepo = dRepo;
		mediaRepo = mRepo;
		programRepo = pRepo;
		reportRepo = rRepo;
	}

	@GetMapping("sync/programs/{displayId}")
	public @ResponseBody ResponseEntity<?> getPrograms(@PathVariable("displayId") String displayId) {
		
		try {
			
			//identify display
			Long dispId = new Long(displayId);
			
			//get programs & their media url lists
			Display display = displayRepo.getOne(dispId);
			List<Program> programs = display.getPrograms();
			
			//removing the infinite object encapsulation problem
			for (Program program : programs) {
				program.setDisplay(null);
				for (Media media : program.getMedias()) {
					media.setPrograms(null);
				}
			}
			
			//set display lastProgramSync time and status
			
			
			//return programs	        
	        return ResponseEntity.ok(programs); 
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}


	}
	
	@PostMapping("sync/reports")
	public @ResponseBody ResponseEntity<?> updateReports() {

		//identify display
		
		//set reports
		
		//return success/error message
		
		//set display lastReportSync time and status
		
		
		return null;
	}

}
