package com.platformia.winkwide.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.platformia.winkwide.entity.Media;

@Component("beforeCreateMediaValidator")
public class MediaValidator implements Validator {

	// The classes are supported by this validator.
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == Media.class;
	}

	@Override
	public void validate(Object obj, Errors errors) {

		Media media = (Media) obj;
		// Check the fields of media.
		if(media.getMediaType().isEmpty()) errors.rejectValue("mediaType", "NotEmpty.media.mediaType");
/*		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.media.name");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mediaType", "NotEmpty.media.mediaType");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "format", "NotEmpty.media.format");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "url", "NotEmpty.media.url");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "size", "NotEmpty.media.size");*/

	}

}