package com.platformia.winkwide.app.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.entity.Report;
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
	public SmartTVSyncController(DisplayRepository dRepo, MediaRepository mRepo, ProgramRepository pRepo,
			ReportRepository rRepo) {
		displayRepo = dRepo;
		mediaRepo = mRepo;
		programRepo = pRepo;
		reportRepo = rRepo;
	}

	@GetMapping("sync/programs/{displayId}")
	public @ResponseBody ResponseEntity<?> getPrograms(@PathVariable("displayId") String displayId) {

		try {

			// identify display
			Long dispId = new Long(displayId);
			// Display display = displayRepo.getOne(dispId);

			// get programs & their media url lists (ONLY Current and Futur Programs)
			@SuppressWarnings("deprecation")
			List<Program> programs = programRepo
					.findByCustomFilters(dispId, null, null, new Date(), new Date(2100, 1, 1), null).getContent();

			// removing the infinite object encapsulation problem (setting programs and
			// reports to null
			for (Program program : programs) {
				program.setDisplay(null);
				for (Media media : program.getMedias()) {
					media.setPrograms(null);
					media.setReports(null);
				}
			}

			// set display lastProgramsSync time and status (SUCCESS CASE)
			// --------------

			// return programs
			return ResponseEntity.ok(programs);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("sync/reports/{displayId}")
	public @ResponseBody ResponseEntity<?> updateReports(@PathVariable("displayId") String displayId,
			@RequestBody ArrayList<Report> reports) {

		try {

			// identify display
			Long dispId = new Long(displayId);
			Display display = displayRepo.getOne(dispId);

			// set reports
			for (Report report : reports) {
				report.setDisplay(display);
				try {
					reportRepo.save(report);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}

			// set display lastReportSync time and status (SUCCESS CASE)
			// ----------

			// return success message
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

}
