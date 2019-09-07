package com.platformia.winkwide.core.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.repository.AccountRepository;
import com.platformia.winkwide.core.utils.MachineAccountProperties;
import com.platformia.winkwide.core.utils.SecurityUtils;

@Component
@RepositoryEventHandler(Display.class)
@EnableConfigurationProperties({ MachineAccountProperties.class })
public class DisplayEventHandler {

	@Autowired
	AccountRepository accountRepo;

	@Autowired
	MachineAccountProperties machineAccountProperties;

	@HandleAfterCreate
	public void handleDisplaySave(@Validated Display display) {
		// Create an Account with default password
		try {
			Account machineAccount = new Account(display.getId().toString(), display.getId().toString(), display.getId().toString(), null, null,
					SecurityUtils.encrytPassword(machineAccountProperties.getDefaultPassword()), true, true, 
					Account.ROLE_MACHINE);
			
			accountRepo.save(machineAccount);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
