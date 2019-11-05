package com.platformia.winkwide.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.Record;
import com.platformia.winkwide.core.exception.ApiError;
import com.platformia.winkwide.core.repository.AccountRepository;
import com.platformia.winkwide.core.repository.DisplayRepository;
import com.platformia.winkwide.core.repository.RecordRepository;
import com.platformia.winkwide.core.utils.AdminSettingsProperties;

@RepositoryRestController
public class DisplayRepositoryController {

	private final DisplayRepository displayRepo;
	private final AccountRepository accountRepo;
	private final RecordRepository recordRepo;

	@Autowired
	public DisplayRepositoryController(DisplayRepository dispRepo, AccountRepository accRepo, RecordRepository rRepo) {
		displayRepo = dispRepo;
		accountRepo = accRepo;
		recordRepo = rRepo;
	}
	
	@Autowired
	AdminSettingsProperties adminSettingsProperties;
	
	@GetMapping("/displays/settings")
	public @ResponseBody ResponseEntity<?> getSettings() {
		
		// return settings
		return ResponseEntity.ok(adminSettingsProperties);
		}

	@DeleteMapping("/displays/{displayId}")
	public @ResponseBody ResponseEntity<?> deleteDisplay(@PathVariable Long displayId) {
		try {
			Optional<Display> display = displayRepo.findById(displayId);
			if (display.isPresent()) {
								
				//Never delete a Display that has linked Reports
				List<Record> records = recordRepo.findByDisplayId(displayId);
				if(records!=null && !records.isEmpty())
					return createValidationErrors("NotPermitted.display.reports");
				
				// delete associated Machine Account
				Account machineAccount = accountRepo.findByUserName(displayId.toString());
				if(machineAccount != null)
					accountRepo.delete(machineAccount);

				// delete links with objects
				displayRepo.deleteDisplayProgramLinks(display.get().getId());
				displayRepo.deleteDisplayRecordLinks(display.get().getId());
				displayRepo.delete(display.get());
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
		case "NotPermitted.display.reports":
			message = "Can't delete Display because linked Reports exist. Please review them first!";
			errors.add("\"reports\": \"NotPermitted.display.reports\"");
			break;
		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors), httpHeaders, httpStatus);
	}

}
