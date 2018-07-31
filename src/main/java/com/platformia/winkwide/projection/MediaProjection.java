package com.platformia.winkwide.projection;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.entity.Media;

@Projection(types = { Media.class }) 
public interface MediaProjection {

	   	String getName();
	    
	    String getMediaType();
	    
	    String getFormat();
	    
	    String getUrl();
	 
	    int getSize();
}