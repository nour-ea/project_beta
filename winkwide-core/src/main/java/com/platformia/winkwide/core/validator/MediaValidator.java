package com.platformia.winkwide.core.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.core.entity.Media;

@Component("beforeCreateMediaValidator")
public class MediaValidator implements Validator {

	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Media.class;
	}

	@Override
	public void validate(Object obj, Errors errors) {

		// Check the fields of media.
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.media.name");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "category", "NotEmpty.media.category");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "NotEmpty.media.type");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "format", "NotEmpty.media.format");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "url", "NotEmpty.media.url");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "thumbUrl", "NotEmpty.media.thumUrl");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "size", "NotEmpty.media.size");
	}

}