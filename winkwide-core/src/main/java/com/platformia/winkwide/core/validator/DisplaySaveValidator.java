package com.platformia.winkwide.core.validator;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.repository.DisplayRepository;


@Component("beforeSaveDisplayValidator")
public class DisplaySaveValidator implements Validator {

	@Autowired
	private DisplayRepository repository;
	
	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Display.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		System.out.println("(beforeSaveDisplayValidator) Validating display");

		Display display = (Display) target;
		
		// Check if there is a duplicate display name
		if (display.getName() != null) {
			System.out.println("Display name: "+ display.getName());
			Display dbdisplay = repository.findByName(display.getName());
			if (dbdisplay != null) //if found duplicate name
				if(dbdisplay.getId() != null && dbdisplay.getId() != dbdisplay.getId()) 
					errors.rejectValue("name", "sorry this name  is already used (Duplicate.display.name)");
		}
		
		// Check if there is a duplicate display MAC address
		if (display.getMac() != null) {
			Display dbDisplay = repository.findByMac(display.getMac());
			if (dbDisplay != null) // if a duplicate MAC address 
				if (display.getId() != null && display.getId() != dbDisplay.getId())
					errors.rejectValue("mac", "Duplicate Mac address, please choose another one (Duplicate.display.mac).");			
		}
		
		//basic validation common to Creation and Update
		this.basicValidation(target, errors);
		
		System.out.println("(beforeSaveDisplayValidator) End of Validating display");
	}
	
	
	//basic validation common to Creation and Update
	public void basicValidation(Object target, Errors errors) {
		Display display = (Display) target;
		
		// Check the fields of display.
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.display.name");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "category", "NotEmpty.display.category");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "area", "NotEmpty.display.area");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "NotEmpty.display.phone");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.display.address");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "brand", "NotEmpty.display.brand");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "size", "NotEmpty.display.size");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mac", "NotEmpty.display.mac");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smart", "NotEmpty.display.smart");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "weekdayAudience", "NotEmpty.display.weekdayAudience");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "holidayAudience", "NotEmpty.display.holidayAudience");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "weekdayVisitors", "NotEmpty.display.weekdayVisitors");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "holidayVisitors", "NotEmpty.display.holidayVisitors");
		
		// Check mac adress format
		if (display.getMac() != null) {

			if (!Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$").matcher(display.getMac()).matches()) {
				errors.rejectValue("mac", "Wrong Mac address Pattern! Should be something like XX:XX:XX:XX:XX:XX (Pattern.display.mac).");
			}
		}
		

	}
	

}