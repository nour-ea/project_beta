package com.platformia.winkwide.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

		@RequestMapping("/")
		public String welcome() {
			return "index";
		}
		
		@RequestMapping("/crudTable")
		public String crudTable() {
			return "crudTable";
		}
		
		@RequestMapping("/403")
		public String accessDenied() {
			return "403";
		}
	
	   // GET: Show Products Page
	   @RequestMapping(value = { "/productsList" }, method = RequestMethod.GET)
	   public String productsList(Model model) {
	 
	      return "products";
	   }   
	
	   // GET: Show Login Page
	   @RequestMapping(value = { "/admin/login" }, method = RequestMethod.GET)
	   public String login(Model model) {
	 
	      return "login";
	   }
	   
	   // GET: Show Create Account Page
	   @RequestMapping(value = { "/admin/createAccount" }, method = RequestMethod.GET)
	   public String createAccount(Model model) {
	 
	      return "createAccount";
	   }
	   
	   // GET: Show Account Info Page
	   @RequestMapping(value = { "/admin/accountInfo" }, method = RequestMethod.GET)
	   public String accountInfo(Model model) {
	 
	      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	      System.out.println(userDetails.getPassword());
	      System.out.println(userDetails.getUsername());
	      System.out.println(userDetails.isEnabled());
	 
	      model.addAttribute("userDetails", userDetails);
	      return "accountInfo";
	   }

}
