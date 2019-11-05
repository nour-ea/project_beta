package com.platformia.winkwide.admin.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import com.platformia.winkwide.core.entity.Account;
import com.platformia.winkwide.core.entity.Area;
import com.platformia.winkwide.core.entity.Bill;
import com.platformia.winkwide.core.entity.Display;
import com.platformia.winkwide.core.entity.DisplayCategory;
import com.platformia.winkwide.core.entity.Holiday;
import com.platformia.winkwide.core.entity.Media;
import com.platformia.winkwide.core.entity.MediaCategory;
import com.platformia.winkwide.core.entity.Playlist;
import com.platformia.winkwide.core.entity.Program;
import com.platformia.winkwide.core.entity.Record;
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
        config.exposeIdsFor(Record.class);
        config.exposeIdsFor(Bill.class);

        config.exposeIdsFor(Area.class);
        config.exposeIdsFor(DisplayCategory.class);
        config.exposeIdsFor(MediaCategory.class);
        config.exposeIdsFor(Holiday.class);
        
        //config.setReturnBodyOnCreate(true);
        //config.setReturnBodyOnUpdate(true);
    }
    
}