package com.platformia.winkwide.validator;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.dao.DaoFilter;
import com.platformia.winkwide.dao.GenericHibernateDao;
import com.platformia.winkwide.entity.Display;
import com.platformia.winkwide.form.DisplayForm;

@Component
public class DisplayValidator implements Validator {

	@Autowired
	private GenericHibernateDao<Display> displayDAO;

	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == DisplayForm.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		DisplayForm displayForm = (DisplayForm) target;

		// Check the fields of displayForm.
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.displayForm.name");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.displayForm.brand");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "brand", "NotEmpty.displayForm.brand");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "size", "NotEmpty.displayForm.size");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "shopCoverage", "NotEmpty.displayForm.shopCoverage");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mac", "NotEmpty.displayForm.mac");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smart", "NotEmpty.displayForm.smart");

		// Check mac adress format
		if (displayForm.getMac() != null) {

			if (!Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$").matcher(displayForm.getMac()).matches()) {
				errors.rejectValue("mac", "Pattern.displayForm.mac");
			} else {
 			
				//Request db with a filter on display mac address to check if it already exists
				displayDAO.setEntityClass(Display.class);
				ArrayList<DaoFilter> macFilters = new ArrayList<DaoFilter>();
				macFilters.add(new DaoFilter("eq", "orderAsc", "mac", displayForm.getMac(), null));
				ArrayList<Display> dbDisplays = (ArrayList<Display>) displayDAO.findAll(macFilters);
				
				//Create request case
				if(displayForm.getId()==null) {
					if (dbDisplays != null && !dbDisplays.isEmpty()) {
						// A Display with that MAC address already exists.
						errors.rejectValue("mac", "Duplicate.displayForm.mac");
					}
				}
				//Update request case
				else {
					if (dbDisplays != null && !dbDisplays.isEmpty()) {
						// A Display with that MAC address already exists.
						for (Display display : dbDisplays) {
						 if(display.getId()!=displayForm.getId())
							 errors.rejectValue("mac", "Duplicate.displayForm.mac");
						}
					}
				}
				
			}
		}

	}

}