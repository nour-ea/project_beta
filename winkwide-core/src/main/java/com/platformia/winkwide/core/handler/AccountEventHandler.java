package com.platformia.winkwide.core.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.repository.AccountRepository;
import com.platformia.winkwide.core.utils.SecurityUtils;

@Component
@RepositoryEventHandler(Account.class)
public class AccountEventHandler {

	@Autowired
	private AccountRepository repository;

	/*
	@HandleBeforeCreate
	@HandleBeforeSave
	public void handleAccountSave(@Valid Account account) {

		Account dbAccount = repository.findByUserName(account.getUserName());
		if (dbAccount == null) { // it's a create operation	
			System.out.println("(Service Side Event Handler) Creating account with userName: " + account.getUserName());
			// Setting Role & default activity
			account.setActive(false);
			account.setUserRole(Account.ROLE_CLIENT);

		} else // it's an update operation
			System.out.println("(Service Side Event Handler) Updating account with userName: " + account.getUserName());

		// Setting encrypted password & Nulling clear password
		account.setEncrytedPassword(SecurityUtils.encrytPassword(account.getPassword()));
		account.setPassword(null);
		account.setConfirmPassword(null);
		System.out.println("success in setting account");

	}
	*/
	
	// After create
	// Send email link for email validation
	// @sendValidationemail
	// ---
	// ---

}
