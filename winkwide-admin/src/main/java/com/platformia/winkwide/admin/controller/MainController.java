package com.platformia.winkwide.admin.controller;

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
	@RequestMapping("/")
	public String welcome() {
		return "login";
	}
	
	
	// Login and Failure Page
	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		return "login";
	}
	
	
	// Portal App Pages
	//
	
	@RequestMapping("/portal/403")
	public String portalAccessDenied() {
		return "portal/403";
	}
	
	@RequestMapping("/portal/dashboard")
	public String portal() {
		return "portal/dashboard";
	}

	@RequestMapping("/portal/partners")
	public String partners() {
		return "portal/partners";
	}
	
	@RequestMapping("/portal/displays")
	public String displays() {
		return "portal/displays";
	}

	@RequestMapping("/portal/clients")
	public String clients() {
		return "portal/clients";
	}
	
	@RequestMapping("/portal/campaigns")
	public String campaigns() {
		return "portal/campaigns";
	}
	
	@RequestMapping("/portal/medias")
	public String medias() {
		return "portal/medias";
	}
	
	@RequestMapping("/portal/playlists")
	public String playlists() {
		return "portal/playlists";
	}

	@RequestMapping("/portal/programs")
	public String programs() {
		return "portal/programs";
	}

	@RequestMapping("/portal/records")
	public String reports() {
		return "portal/records";
	}
	
	@RequestMapping("/portal/billing")
	public String billing() {
		return "portal/billing";
	}
	
	@RequestMapping("/portal/settings")
	public String settings() {
		return "portal/settings";
	}

	@RequestMapping("/portal/profile")
	public String accountInfo() {
		return "portal/profile";
	}

	@RequestMapping("/createAccount")
	public String createAccount() {
		return "website/createAccount";
	}

	@RequestMapping("/createCampaign")
	public String createCampaign() {
		return "website/createCampaign";
	}
	
}
