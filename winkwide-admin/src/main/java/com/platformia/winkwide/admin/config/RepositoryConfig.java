package com.platformia.winkwide.admin.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.entity.Report;
import com.platformia.winkwide.core.entity.Spot;

@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    	config.exposeIdsFor(Account.class);
    	config.exposeIdsFor(Display.class);
    	config.exposeIdsFor(Playlist.class);
    	config.exposeIdsFor(Spot.class);
        config.exposeIdsFor(Media.class);
        config.exposeIdsFor(Program.class);
        config.exposeIdsFor(Report.class);
        
        //config.setReturnBodyOnCreate(true);
        //config.setReturnBodyOnUpdate(true);
    }
    
}