package com.platformia.winkwide.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.platformia.winkwide.entity.Display;
import com.platformia.winkwide.repository.DisplayRepository;

@RepositoryRestController
public class DisplayRepositoryController {

    private final DisplayRepository repository;

    @Autowired
    public DisplayRepositoryController(DisplayRepository repo) { 
        repository = repo;
    }
    

   @DeleteMapping("/displays/{displayId}") 
    public @ResponseBody ResponseEntity<?> deleteDisplay(@PathVariable Long displayId) {
        	
        Optional<Display> display = repository.findById(displayId);    
        if(display.isPresent()) {
        	repository.deleteDisplayProgramLinks(display.get().getId());
        	repository.deleteDisplayReportLinks(display.get().getId());
        	repository.delete(display.get());
        }
        return ResponseEntity.ok().build();
            	    	        
    }
 
}
