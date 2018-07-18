package com.platformia.winkwide.handler;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import com.platformia.winkwide.entity.Media;

@Component
@RepositoryEventHandler(Media.class)
public class MediaEventHandler{

	  @HandleBeforeCreate
	  public void handleMediaSave(Media m) {
	    //System.out.println("My Handler says : we are trying to save media with name : " + m.getName());
	  }
}
