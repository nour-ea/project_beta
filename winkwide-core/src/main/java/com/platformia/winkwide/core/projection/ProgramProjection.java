package com.platformia.winkwide.core.projection;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.core.entity.Program;

@Projection(types = { Program.class }) 
public interface ProgramProjection {

	   	Long getDisplayId();
	    
	   	Date getStartTime();
	   	
	   	Date getEndTime();
	   	
	    List<Long> getMediaIds();
	    
}