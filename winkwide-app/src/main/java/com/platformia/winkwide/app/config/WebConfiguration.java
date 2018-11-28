package com.platformia.winkwide.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.platformia.winkwide.app.WinkWideApp;
 
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
 

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Load file: validation.properties
        messageSource.setBasename("classpath:validation");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
        //comment
    }
    
    //configure file path retrieval outside jar
  
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	

   	    ApplicationHome home = new ApplicationHome();
    	
   	    System.out.println(home.getDir());
   	    System.out.println(home.getSource());

        registry
          .addResourceHandler("/uploads/**")
          .addResourceLocations("file:"+home.getDir()+"/uploads/");
     }
  
}