package com.platformia.winkwide.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.platformia.winkwide.core.exception.ApiError;
import com.platformia.winkwide.core.exception.FileStorageException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ RepositoryConstraintViolationException.class, ConstraintViolationException.class })
	@ResponseBody
	public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
		RepositoryConstraintViolationException nevEx = (RepositoryConstraintViolationException) ex;


		//send back list of errors
		HttpStatus httpStatus = HttpStatus.NOT_ACCEPTABLE;
		String message = "Review your request please !  " ;
		List<String> errors = new ArrayList<String>();
		
		for (FieldError error : nevEx.getErrors().getFieldErrors()) {
	        errors.add("\""+ error.getField() + "\": \"" + error.getCode() + "\"");
	    }
		
		if(errors.size()>0)
			message = " " + nevEx.getErrors().getFieldErrors().get(0).getCode();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors),
				httpHeaders, httpStatus);
	}
	
	@ExceptionHandler({ FileStorageException.class })
	@ResponseBody
	public ResponseEntity<Object> handleFileStorageException(Exception ex, WebRequest request) {
		FileStorageException nevEx = (FileStorageException) ex;


		//send back list of errors
		HttpStatus httpStatus = HttpStatus.FAILED_DEPENDENCY;
		String message = "Review your request please !" ;
		List<String> errors = new ArrayList<String>();
	        errors.add("FileSorageException: " + nevEx.getMessage());

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<Object>(new ApiError(httpStatus, message, errors),
				httpHeaders, httpStatus);
	}

	/*
	 * // BindException: This exception is thrown when fatal binding errors occur.
	 * // MethodArgumentNotValidException: This exception is thrown when argument //
	 * annotated with @Valid failed validation:
	 * 
	 * @Override protected ResponseEntity<Object>
	 * handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders
	 * headers, HttpStatus status, WebRequest request) List<String> errors = new
	 * ArrayList<String>(); for (FieldError error :
	 * ex.getBindingResult().getFieldErrors()) { errors.add(error.getField() + ": "
	 * + error.getDefaultMessage()); } for (ObjectError error :
	 * ex.getBindingResult().getGlobalErrors()) { errors.add(error.getObjectName() +
	 * ": " + error.getDefaultMessage()); }
	 * 
	 * ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,
	 * ex.getLocalizedMessage(), errors); return handleExceptionInternal(ex,
	 * apiError, headers, apiError.getStatus(), request); }
	 * 
	 * // MissingServletRequestPartException: This exception is thrown when when the
	 * // part of a multipart request not found //
	 * MissingServletRequestParameterException: This exception is thrown when //
	 * request missing parameter:
	 * 
	 * @Override protected ResponseEntity<Object>
	 * handleMissingServletRequestParameter(MissingServletRequestParameterException
	 * ex, HttpHeaders headers, HttpStatus status, WebRequest request) { String
	 * error = ex.getParameterName() + " parameter is missing";
	 * 
	 * ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,
	 * ex.getLocalizedMessage(), error); return new ResponseEntity<Object>(apiError,
	 * new HttpHeaders(), apiError.getStatus()); }
	 * 
	 * // ConstrainViolationException: This exception reports the result of
	 * constraint // violations
	 * 
	 * @ExceptionHandler({ ConstraintViolationException.class }) public
	 * ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException
	 * ex, WebRequest request) { List<String> errors = new ArrayList<String>();
	 * 
	 * errors.add(ex.getConstraintName() + " " + ex.getErrorCode() + ": " +
	 * ex.getMessage());
	 * 
	 * ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,
	 * ex.getLocalizedMessage(), errors); return new
	 * ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus()); }
	 * 
	 * // Default handlers
	 * 
	 * @ExceptionHandler({ Exception.class }) public ResponseEntity<Object>
	 * handleAll(Exception ex, WebRequest request) { ApiError apiError = new
	 * ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(),
	 * "error occurred"); return new ResponseEntity<Object>(apiError, new
	 * HttpHeaders(), apiError.getStatus()); }
	 */

}
