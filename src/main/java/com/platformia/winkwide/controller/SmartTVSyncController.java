package com.platformia.winkwide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.platformia.winkwide.repository.DisplayRepository;
import com.platformia.winkwide.repository.MediaRepository;
import com.platformia.winkwide.repository.ProgramRepository;
import com.platformia.winkwide.repository.ReportRepository;

@RepositoryRestController
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

	@GetMapping("/sync/programs")
	public @ResponseBody ResponseEntity<?> getPrograms() {
		
		//identify display
		
		//get programs & their media url lists
		
		//return programs
		
		//set display lastProgramSync time and status

		return null;
	}
	
	@PostMapping("/sync/reports")
	public @ResponseBody ResponseEntity<?> updateReports() {

		//identify display
		
		//set reports
		
		//return success/error message
		
		//set display lastReportSync time and status
		
		
		return null;
	}

}
