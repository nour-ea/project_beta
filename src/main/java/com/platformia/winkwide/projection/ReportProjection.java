package com.platformia.winkwide.projection;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.entity.Report;

@Projection(types = { Report.class }) 
public interface ReportProjection {

   	Long getDisplayId();
    
   	Date getStartTime();
   	
   	Date getEndTime();
   	
    Long getMediaId();
}