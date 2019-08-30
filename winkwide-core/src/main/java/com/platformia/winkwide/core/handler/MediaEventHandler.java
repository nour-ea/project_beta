package com.platformia.winkwide.core.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import com.platformia.winkwide.core.entity.Media;

@Component
@RepositoryEventHandler(Media.class)
public class MediaEventHandler{

	  @HandleBeforeCreate
	  public void handleMediaSave(Media m) {
	    //System.out.println("My Handler says : we are trying to save media with name : " + m.getName());
		// use this to limit data storage for a Client ?
	  }
}
