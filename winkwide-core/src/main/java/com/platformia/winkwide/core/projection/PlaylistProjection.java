package com.platformia.winkwide.core.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.entity.Spot;

@Projection(types = { Playlist.class }) 
public interface PlaylistProjection {

	   	String getName();
	    
	    String getDuration();
	    
	    List<Program> getPrograms();
	    
	    List<Spot> getSpots();
}