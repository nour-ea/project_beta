package com.platformia.winkwide.core.service;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<String> {
	  
    @Override
    public Optional<String> getCurrentAuditor() {
        /*
        if you are using spring security, you can get the currently logged username with following code segment.
      */    	
      return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
    }
 
}