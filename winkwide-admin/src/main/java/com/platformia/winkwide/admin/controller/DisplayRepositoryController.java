package com.platformia.winkwide.admin.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.repository.AccountRepository;
import com.platformia.winkwide.core.repository.DisplayRepository;

@RepositoryRestController
public class DisplayRepositoryController {

	private final DisplayRepository displayRepo;
	private final AccountRepository accountRepo;

	@Autowired
	public DisplayRepositoryController(DisplayRepository dispRepo, AccountRepository accRepo) {
		displayRepo = dispRepo;
		accountRepo = accRepo;
	}

	@DeleteMapping("/displays/{displayId}")
	public @ResponseBody ResponseEntity<?> deleteDisplay(@PathVariable Long displayId) {
		try {
			Optional<Display> display = displayRepo.findById(displayId);
			if (display.isPresent()) {
				// delete associated Machine Account
				Account machineAccount = accountRepo.findByUserName(displayId.toString());
				if(machineAccount != null)
					accountRepo.delete(machineAccount);

				// delete links with objects
				displayRepo.deleteDisplayProgramLinks(display.get().getId());
				displayRepo.deleteDisplayReportLinks(display.get().getId());
				displayRepo.delete(display.get());
			}
			return ResponseEntity.ok().build();
			
		} catch (Exception e) {
			e.printStackTrace();

			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}

}
