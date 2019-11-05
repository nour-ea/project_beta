package com.platformia.winkwide.core.projection;

import java.time.LocalDateTime;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.core.entity.Record;

@Projection(types = { Record.class }) 
public interface RecordProjection {

   	Long getDisplayId();
    
   	LocalDateTime getStartTime();
   	
   	LocalDateTime getEndTime();
   	
    Long getMediaId();
}