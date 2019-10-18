package com.platformia.winkwide.core.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.repository.AccountRepository;

@Component("beforeCreateAccountValidator")
public class AccountCreateValidator implements Validator {

	@Autowired
	private AccountSaveValidator accountSaveValidator;
	
	
	@Autowired
	private AccountRepository repository;

	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Account.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		System.out.println("(beforeCreateAccountValidator) Validating account");

		// Make sure password is there and confirmation is correct
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.account.password");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotEmpty.account.confirmPassword");

		// check if there is a duplicate account userName
		Account account = (Account) target;
		if (account.getUserName() != null) {
			System.out.println("Account userName: " + account.getUserName());
			Account dbAccount = repository.findByUserName(account.getUserName());
			if (dbAccount != null)// if found duplicate userName
				errors.rejectValue("userName", "sorry this email is already used (Duplicate.account.userName)");
		}

		//basic validation common to Creation and Update
		accountSaveValidator.basicValidation(target, errors);
		
		System.out.println("(beforeCreateAccountValidator) End of Validating account");
	}

}