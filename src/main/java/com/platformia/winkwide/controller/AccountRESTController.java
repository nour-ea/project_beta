package com.platformia.winkwide.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.dao.GenericHibernateDao;
import com.platformia.winkwide.entity.Account;
import com.platformia.winkwide.exception.ApiError;
import com.platformia.winkwide.form.AccountForm;
import com.platformia.winkwide.validator.AccountValidator;

@RestController
public class AccountRESTController {

	@Autowired
	private GenericHibernateDao<Account> accountDAO;

	// initiate DAO Classes
	@PostConstruct
	public void init() {
		accountDAO.setEntityClass(Account.class);
	}

	@Autowired
	private AccountValidator accountValidator;

	// Set a form Validator
	@InitBinder
	protected void initBinder(WebDataBinder dataBinder) {
		// Form target
		Object target = dataBinder.getTarget();
		if (target == null) {
			return;
		}
		System.out.println("Target=" + target);

		if (target.getClass() == AccountForm.class) {
			dataBinder.setValidator(accountValidator);
		}
	}

	// CREATE URL : /account
	@RequestMapping(value = "/account", method = RequestMethod.POST)
	public ApiError createAccount(@Validated @RequestBody AccountForm accountForm, BindingResult result) {

		HttpStatus httpStatus;
		String message = "default message de rien du tout";
		List<String> errors = new ArrayList<String>();
		
		// Validate result
		if (!result.hasErrors()) {
			System.out.println("(Service Side) Creating 	account with userName: " + accountForm.getUserName());
			Account newAccount = new Account(accountForm, Account.ROLE_SELLER);
			accountDAO.create(newAccount);
			// Send email link for email validation
			// @sendValidationemail
			//---
			//---
			//send confirmation
			httpStatus = HttpStatus.OK;
			message = "Account with userName : " + accountForm.getUserName() + " successfully created! " ;
			
		} else {
			//send back list of errors
			httpStatus = HttpStatus.BAD_REQUEST;
			message = "Review your request please !" ;
			for (FieldError error : result.getFieldErrors()) {
		        errors.add(error.getField() + ": " + error.getCode());
		    }
		}
		return new ApiError(httpStatus, message, errors);
	}
	
/*	//VIEW All URL : /accounts
	@RequestMapping(value = "/accounts")
	public List<Account> getdisplays(Pageable pageable){
				
		List<Account> list = accountDAO.findAll(pageable);
		return list;
	}
	*/


	// EDIT URL : /account or /account.xml or /account.json
	/*@RequestMapping(value = "/account", //
			method = RequestMethod.PUT, //
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public ApiError updateAccount(@Validated @RequestBody AccountForm accountForm) {

		System.out.println("(Service Side) Editing password for account with userName: " + accountForm.getUserName());
		Account account = accountDAO.findOne(accountForm.getUserName());
		if (account != null) {
			account.setEncrytedPassword(SecurityUtils.encrytePassword(accountForm.getPassword()));
			accountDAO.update(account);
		} else {
			System.out.println("(Service Side) account with userName: " + accountForm.getUserName() + "  not found");

		}
		return null;
	}*/

}
