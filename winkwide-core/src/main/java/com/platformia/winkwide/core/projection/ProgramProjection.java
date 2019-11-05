package com.platformia.winkwide.core.projection;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.core.entity.Program;

@Projection(types = { Program.class }) 
public interface ProgramProjection {

	   	Long getDisplayId();
	   	
	   	String getName();
	    
	   	LocalDateTime getStartTime();
	   	
	   	LocalDateTime getEndTime();
	   	
	    List<Long> getMediaIds();
	    
}