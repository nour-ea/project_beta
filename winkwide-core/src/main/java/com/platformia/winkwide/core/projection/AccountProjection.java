package com.platformia.winkwide.core.projection;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.core.entity.Account;

@Projection(types = { Account.class }) 
public interface AccountProjection {

	   	String getFirstName();
	    
	    String getLastName();
	    
	    String getUserRole();
	    
}