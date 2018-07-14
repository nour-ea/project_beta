package com.platformia.winkwide.form;

import lombok.Data;

@Data
public class MediaForm {
	
	private Long id;
	
    private String name;
    
    private String mediaType;
    
    private String format;
    
    private String url;
 
    private int size;
	
    private boolean verified;
    
}
