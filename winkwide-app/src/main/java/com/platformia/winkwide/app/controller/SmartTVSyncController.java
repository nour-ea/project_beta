package com.platformia.winkwide.app.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.entity.Report;
import com.platformia.winkwide.core.entity.Spot;
import com.platformia.winkwide.core.repository.DisplayRepository;
import com.platformia.winkwide.core.repository.MediaRepository;
import com.platformia.winkwide.core.repository.PlaylistRepository;
import com.platformia.winkwide.core.repository.ProgramRepository;
import com.platformia.winkwide.core.repository.ReportRepository;
import com.platformia.winkwide.core.service.FileStorageService;
import com.platformia.winkwide.core.utils.AppSettingsProperties;

@EnableConfigurationProperties({ AppSettingsProperties.class })
@RestController
public class SmartTVSyncController {

	private final DisplayRepository displayRepo;
	private final MediaRepository mediaRepo;
	private final PlaylistRepository playlistRepo;
	private final ProgramRepository programRepo;
	private final ReportRepository reportRepo;

	@Autowired
	public SmartTVSyncController(DisplayRepository dRepo, MediaRepository mRepo, PlaylistRepository plRepo,
			ProgramRepository pRepo, ReportRepository rRepo) {
		displayRepo = dRepo;
		mediaRepo = mRepo;
		playlistRepo = plRepo;
		programRepo = pRepo;
		reportRepo = rRepo;
	}
	
	@Autowired
	AppSettingsProperties appSettingsProperties;
	
	@Autowired
	private FileStorageService fileStorageService;

	@GetMapping("sync/settings")
	public @ResponseBody ResponseEntity<?> getSettings() {
		
		// return settings
		return ResponseEntity.ok(appSettingsProperties);
		}
	
	@GetMapping("sync/programs")
	public @ResponseBody ResponseEntity<?> getPrograms() {

		try {

			// identify display
			//Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
			Long dispId = Long.parseLong("6970173894");
			Display display = displayRepo.getOne(dispId);

			// get programs & their media url lists (ONLY Current and Futur Programs)
			@SuppressWarnings("deprecation")
			List<Program> programs = programRepo
					.findByCustomFilters(null, dispId, null, null, new Date(), new Date(2100, 1, 1), null).getContent();

 
			//some sanitization
			for (Program program : programs) {
				// avoiding Lazy Loading versus Jackson serialization problem by setting displays to null
				program.setDisplays(null);
				
				// removing the infinite object encapsulation problem (setting programs and sposts.playlists & sposts.media.spots to null)
				for (Playlist playlist : program.getPlaylists()) {
					playlist.setPrograms(null);
					for (Spot spot : playlist.getSpots()) {
						spot.setPlaylist(null);
						spot.getMedia().setSpots(null);
						spot.getMedia().setReports(null);
						
					}
				}
			}

			// set display lastProgramsSync time and status (SUCCESS CASE)
			display.setLastSyncTime(new Date());

			// return programs
			return ResponseEntity.ok(programs);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("sync/reports")
	public @ResponseBody ResponseEntity<?> updateReports(@RequestBody ArrayList<Report> reports) {

		try {

			// identify display
			Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

			// set reports
			for (Report report : reports) {
				report.setDisplay(displayRepo.getOne((dispId)));
				try {
					reportRepo.save(report);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// set display lastReportSync time and status (SUCCESS CASE)
			Display display = displayRepo.getOne((dispId));
			display.setLastSyncTime(new Date());
			displayRepo.save(display);

			// return success message
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@GetMapping("sync/displayId")
	public @ResponseBody ResponseEntity<?> getDisplayId() {

		try {

			// identify display
			Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
			// Display display = displayRepo.getOne(dispId);

			// check if the mac adress is correct
			// ----
			// ----

			// return programs
			return ResponseEntity.ok(dispId);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}
	
	@PostMapping("sync/logs")
	public @ResponseBody ResponseEntity<?> saveLogs(@RequestBody String logs) {
		
		try {
			// identify display
			Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
			
			//write logs
			fileStorageService.writeTextInLogFile("display_" + dispId, logs);
			
			// return success message
			return new ResponseEntity<>(HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
		
	}

}
