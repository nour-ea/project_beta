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


@Component
public class DisplayValidator implements Validator {

	@Autowired
	private GenericHibernateDao<Display> displayDAO;

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
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.display.brand");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "brand", "NotEmpty.display.brand");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "size", "NotEmpty.display.size");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "shopCoverage", "NotEmpty.display.shopCoverage");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mac", "NotEmpty.display.mac");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smart", "NotEmpty.display.smart");

		// Check mac adress format
		if (display.getMac() != null) {

			if (!Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$").matcher(display.getMac()).matches()) {
				errors.rejectValue("mac", "Pattern.display.mac");
			} else {
 			
				//Request db with a filter on display mac address to check if it already exists
				displayDAO.setEntityClass(Display.class);
				ArrayList<DaoFilter> macFilters = new ArrayList<DaoFilter>();
				macFilters.add(new DaoFilter("eq", "orderAsc", "mac", display.getMac(), null));
				ArrayList<Display> dbDisplays = (ArrayList<Display>) displayDAO.findAll(macFilters);
				
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