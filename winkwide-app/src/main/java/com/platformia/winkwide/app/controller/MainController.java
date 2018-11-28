package com.platformia.winkwide.app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

/*	//Error
	@RequestMapping("/error")
	public String oops() {
		return "oops";
	}*/	
	
	// Website Pages
	//
	
	// Login and Failure Page
	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		return "login";
	}
	
	@RequestMapping("/tv/403")
	public String machineAccessDenied() {
		return "tv/403";
	}	
	
	// Machine App Pages
	//
	@RequestMapping("/tv")
	public String smartTV() {
		return "tv/smartTV";
	}	

}
