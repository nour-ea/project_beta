package com.platformia.winkwide.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.dao.GenericHibernateDao;
import com.platformia.winkwide.entity.Display;
import com.platformia.winkwide.exception.ApiError;
import com.platformia.winkwide.form.DisplayForm;
import com.platformia.winkwide.validator.DisplayValidator;

@RestController
public class DisplayRESTController {

	@Autowired
	private GenericHibernateDao<Display> displayDAO;

	// initiate DAO Classes
	@PostConstruct
	public void init() {
		displayDAO.setEntityClass(Display.class);
	}

	@Autowired
	private DisplayValidator displayValidator;

	// Set a form Validator
	@InitBinder
	protected void initBinder(WebDataBinder dataBinder) {
		// Form target
		Object target = dataBinder.getTarget();
		if (target == null) {
			return;
		}
		System.out.println("Target=" + target);

		if (target.getClass() == DisplayForm.class) {
			dataBinder.setValidator(displayValidator);
		}
	}

	
	//VIEW All URL : /displays
	@RequestMapping(value = "/displays1")
	public List<Display> getdisplays(Pageable pageable){
				
		return displayDAO.findAll(pageable);
	}
	
	//VIEW ONE URL : /display/{displayId}
	@RequestMapping(value = "/display/{displayId}")
	@ResponseBody
	public Display getdisplay(@PathVariable("displayId") Long displayId){
		
		return displayDAO.findOne(displayId);
	}
	
	
	// CREATE URL : /display
	@RequestMapping(value = "/display", method = RequestMethod.POST)
	public ApiError createDisplay(@Validated @RequestBody DisplayForm displayForm, BindingResult result) {

		HttpStatus httpStatus;
		String message = "default message de rien du tout";
		List<String> errors = new ArrayList<String>();
		
		// Validate result
		if (!result.hasErrors()) {
			System.out.println("(Service Side) Creating 	display with name: " + displayForm.getName());
			Display newdisplay = new Display(displayForm);
			displayDAO.create(newdisplay);
			//send confirmation
			httpStatus = HttpStatus.OK;
			message = "display with name : " + displayForm.getName() + " successfully created! " ;
			
		} else {
			//send back list of errors
			httpStatus = HttpStatus.BAD_REQUEST;
			message = "Review your request please !";
			for (FieldError error : result.getFieldErrors()) {
		        errors.add(error.getField() + ": " + error.getCode());
		    }
		}
		return new ApiError(httpStatus, message, errors);
	}
	
	// EDIT URL : /display
	@RequestMapping(value = "/display", method = RequestMethod.PUT)
	public ApiError updateDisplay(@Validated @RequestBody DisplayForm displayForm, BindingResult result) {

		HttpStatus httpStatus;
		String message = "default message de rien du tout";
		List<String> errors = new ArrayList<String>();
		
		// Validate result
		if (!result.hasErrors()) {
			System.out.println("(Service Side) Updating display with id: " + displayForm.getId() + " and name: " + displayForm.getName());
			Display display = displayDAO.findOne(displayForm.getId());
			display.update(displayForm);
			displayDAO.update(display);
			//send confirmation
			httpStatus = HttpStatus.OK;
			message = "display with id: " + displayForm.getId() + " and name: " + displayForm.getName() + " successfully updated! ";
			
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
	
	
	
	// DELETE URL : /display/{displayId}
	@RequestMapping(value = "/display/{displayId}", method = RequestMethod.DELETE)
	public ApiError deleteDisplay(@PathVariable("displayId") Long displayId) {
			
		HttpStatus httpStatus;
		String message = "default message de rien du tout";
		List<String> errors = new ArrayList<String>();
		
		System.out.println("(Service Side) Deleting display with displayId: " + displayId);
		try {
			displayDAO.deleteById(displayId);
			//send confirmation
			httpStatus = HttpStatus.OK;
			message = "display with id: " + displayId + " successfully deleted! ";
		} catch (Exception e) {
			//send back list of errors
			httpStatus = HttpStatus.BAD_REQUEST;
			message = "Review your request please !" ;
	        errors.add(e.getMessage());
			e.printStackTrace();
		}
			
		return new ApiError(httpStatus, message, errors);
	}

}
