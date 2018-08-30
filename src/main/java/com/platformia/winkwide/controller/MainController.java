package com.platformia.winkwide.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

	// Login and Failure Page
	@RequestMapping("/login")
	public String login(Model model) {
		return "login";
	}
	
	@RequestMapping("/403")
	public String machineAccessDenied() {
		return "403";
	}

	// Machine App Pages
	//
	@RequestMapping("/")
	public String smartTV() {
		return "smartTV/smartTV";
	}	
	
	
	// Portal App Pages
	//
	
	@RequestMapping("/portal/403")
	public String portalAccessDenied() {
		return "portal/403";
	}
	
	@RequestMapping("/portal")
	public String welcome() {
		return "portal/welcome";
	}

	@RequestMapping("/portal/displays")
	public String displays() {
		return "portal/displays";
	}

	@RequestMapping("/portal/medias")
	public String medias() {
		return "portal/medias";
	}

	@RequestMapping("/portal/programs")
	public String programs() {
		return "portal/programs";
	}

	@RequestMapping("/portal/reports")
	public String reports() {
		return "portal/reports";
	}

	@RequestMapping("/portal/createAccount")
	public String createAccount(Model model) {
		return "portal/createAccount";
	}

	@RequestMapping("/portal/accountInfo")
	public String accountInfo(Model model) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		System.out.println(userDetails.getPassword());
		System.out.println(userDetails.getUsername());
		System.out.println(userDetails.isEnabled());

		model.addAttribute("userDetails", userDetails);
		return "portal/accountInfo";
	}

}
