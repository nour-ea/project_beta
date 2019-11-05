package com.platformia.winkwide.app.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import com.platformia.winkwide.core.entity.MediaCategory;
import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.entity.Record;
import com.platformia.winkwide.core.entity.Spot;
import com.platformia.winkwide.core.repository.DisplayRepository;
import com.platformia.winkwide.core.repository.MediaCategoryRepository;
import com.platformia.winkwide.core.repository.MediaRepository;
import com.platformia.winkwide.core.repository.PlaylistRepository;
import com.platformia.winkwide.core.repository.ProgramRepository;
import com.platformia.winkwide.core.repository.RecordRepository;
import com.platformia.winkwide.core.service.FileStorageService;
import com.platformia.winkwide.core.utils.AppSettingsProperties;

@EnableConfigurationProperties({ AppSettingsProperties.class })
@RestController
public class SmartTVSyncController {

	private final DisplayRepository displayRepo;
	private final MediaRepository mediaRepo;
	private final MediaCategoryRepository mediaCategoryRepo;
	private final PlaylistRepository playlistRepo;
	private final ProgramRepository programRepo;
	private final RecordRepository recordRepo;

	@Autowired
	public SmartTVSyncController(DisplayRepository dRepo, MediaRepository mRepo, MediaCategoryRepository mCatRepo,
			PlaylistRepository plRepo, ProgramRepository pRepo, RecordRepository rRepo) {
		displayRepo = dRepo;
		mediaRepo = mRepo;
		mediaCategoryRepo = mCatRepo;
		playlistRepo = plRepo;
		programRepo = pRepo;
		recordRepo = rRepo;
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

	@GetMapping("sync/mediaCategories")
	public @ResponseBody ResponseEntity<?> getMediaCategories() {

		// return all Media Categories
		ArrayList<MediaCategory> mediaCategories = (ArrayList<MediaCategory>) mediaCategoryRepo.findAll();
				
		return ResponseEntity.ok(mediaCategories);
	}

	@GetMapping("sync/programs")
	public @ResponseBody ResponseEntity<?> getPrograms() {

		try {

			// identify display
			Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
			Display display = displayRepo.getOne(dispId);

			// get programs & their media url lists (ONLY Current and Futur Programs)
			List<Program> programs = programRepo
					.findByCustomFilters(null, dispId, null, null, LocalDateTime.now(), LocalDateTime.of(2100, 1, 1, 0, 0), null).getContent();

			// some sanitization
			for (Program program : programs) {
				// avoiding Lazy Loading versus Jackson serialization problem by setting
				// displays to null
				program.setDisplays(null);

				// removing the infinite object encapsulation problem (setting programs and
				// sposts.playlists & sposts.media.spots to null)
				for (Playlist playlist : program.getPlaylists()) {
					playlist.setPrograms(null);
					for (Spot spot : playlist.getSpots()) {
						spot.setPlaylist(null);
						spot.getMedia().setSpots(null);
					}
				}
			}

			// set display lastProgramsSync time and status (SUCCESS CASE)
			display.setLastSyncTime(LocalDateTime.now());

			// return programs
			return ResponseEntity.ok(programs);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("sync/records")
	public @ResponseBody ResponseEntity<?> updateRecords(@RequestBody ArrayList<Record> records) {

		try {

			// identify display
			Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
			Display display = displayRepo.getOne(dispId);

			// set records displayId (for security) and displayTime
			for (Record record : records) {
				record.setDisplayId(dispId);
				record.setDisplayTime( record.getStartTime().until(record.getEndTime(), ChronoUnit.SECONDS));
				try {
					recordRepo.save(record);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// set display lastRecordSync time and status (SUCCESS CASE)
			display.setLastSyncTime(LocalDateTime.now());
			displayRepo.save(display);

			// return success message
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@GetMapping("sync/display")
	public @ResponseBody ResponseEntity<?> getDisplay() {

		try {

			// identify display
			Long dispId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
			Display display = displayRepo.getOne(dispId);
			Display returnedDisplay = new Display();
			
			returnedDisplay.setId(dispId);
			returnedDisplay.setName(display.getName());

			// check if the mac adress is correct
			// ----
			// ----

			// return programs
			return ResponseEntity.ok(returnedDisplay);

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

			// write logs
			fileStorageService.writeTextInLogFile("display_" + dispId, logs);

			// return success message
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

}
