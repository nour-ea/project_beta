package com.platformia.winkwide.core.projection;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.core.entity.Record;

@Projection(types = { Record.class }) 
public interface RecordProjection {

   	Long getDisplayId();
    
   	Date getStartTime();
   	
   	Date getEndTime();
   	
    Long getMediaId();
}