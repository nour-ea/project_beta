package com.platformia.winkwide.core.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.utils.SecurityUtils;

@Component
@RepositoryEventHandler(Account.class)
public class AccountEventHandler{

	  @HandleBeforeCreate
	  public void handleAccountSave(@Validated Account account) {
		  	//Setting encrypted password
			/*System.out.println("(Service Side Event Handler) Creating account with userName: " + account.getUserName());
			account.setEncrytedPassword(SecurityUtils.encrytPassword(account.getPassword()));
			
			//Nulling clear password
			account.setPassword(null);
			account.setConfirmPassword(null);
			
			//Setting Role
			account.setUserRole(Account.ROLE_CLIENT);
			System.out.println("success in crypto & Role setting");*/
			
	  }

	  //After create
		// Send email link for email validation
		// @sendValidationemail
		//---
		//---		  

}
