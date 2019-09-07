package com.platformia.winkwide.core.validator;

import java.util.Collection;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.repository.DisplayRepository;


@Component("beforeCreateDisplayValidator")
public class DisplayValidator implements Validator {

	@Autowired
	private DisplayRepository repository;

	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Display.class;
	}

	@Override
	public void validate(Object obj, Errors errors) {
		Display display = (Display) obj;

		// Check the fields of display.
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.display.name");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "area", "NotEmpty.display.area");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.display.address");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "brand", "NotEmpty.display.brand");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "size", "NotEmpty.display.size");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mac", "NotEmpty.display.mac");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smart", "NotEmpty.display.smart");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "averageAudience", "NotEmpty.display.averageAudience");

		// Check mac adress format
		if (display.getMac() != null) {

			if (!Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$").matcher(display.getMac()).matches()) {
				errors.rejectValue("mac", "Pattern.display.mac");
			} else {
 			
				//Request db with a filter on display mac address to check if it already exists
				Page<Display> dbDisplaysPage = repository.findByCustomFilters(null, null, null, null, display.getMac(), null, null, null, null, null, null, null, null, null, null, null, null);
				Collection<Display> dbDisplays = dbDisplaysPage.getContent();
				
				//Create request case
				if(display.getId()==null) {
					if (dbDisplays != null && !dbDisplays.isEmpty()) {
						// A Display with that MAC address already exists.
						errors.rejectValue("mac", "Duplicate.display.mac");
					}
				}
				//Update request case
				else {
					if (dbDisplays != null && !dbDisplays.isEmpty()) {
						// A Display with that MAC address already exists.
						for (Display d : dbDisplays) {
						 if(d.getId()!=d.getId())
							 errors.rejectValue("mac", "Duplicate.display.mac");
						}
					}
				}
				
			}
		}

	}

}