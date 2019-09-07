package com.platformia.winkwide.core.validator;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.repository.AccountRepository;
import com.platformia.winkwide.core.utils.SecurityUtils;

@Component("beforeCreateAccountValidator")
public class AccountValidator implements Validator {

	// common-validator library.
	private EmailValidator emailValidator = EmailValidator.getInstance();

	@Autowired
	private AccountRepository repository;

	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Account.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		Account account = (Account) target;

		// Check the fields of Account.
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "NotEmpty.account.userName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "NotEmpty.account.firstName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "NotEmpty.account.lastName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.account.password");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotEmpty.account.confirmPassword");
		if(!account.isConditionsAccepted())
			errors.rejectValue("conditionsAccepted", "NotAccepted.account.conditionsAccepted");

		if (!this.emailValidator.isValid(account.getUserName())) {
			// Invalid email.
			errors.rejectValue("userName", "Pattern.account.email");
		} else if (account.getUserName() != null) {
			Account dbAccount = repository.findByUserName(account.getUserName());
			if (dbAccount != null) {
				// UserName has been used by another account.
				errors.rejectValue("userName", "Duplicate.account.userName");
			}
		}

		// matching password and its confirmation
		if (!errors.hasErrors()) {
			if (!account.getConfirmPassword().equals(account.getPassword())) {
				errors.rejectValue("confirmPassword", "Match.account.confirmPassword");
			}
		}
		
		//if everything is OK - Force some security elements
	  	//Setting encrypted password & Nulling clear password
		System.out.println("(Service Side Event Handler) Creating account with userName: " + account.getUserName());
		account.setEncrytedPassword(SecurityUtils.encrytPassword(account.getPassword()));
		account.setPassword(null);
		account.setConfirmPassword(null);
		
		//Setting Role & default activity
		account.setActive(false);
		account.setUserRole(Account.ROLE_CLIENT);
		System.out.println("success in crypto & Role setting");
		
	}

}