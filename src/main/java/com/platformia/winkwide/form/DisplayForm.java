package com.platformia.winkwide.form;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DisplayForm {

		private Long id;
		
	    private String name;
	    
	    private String address;
	    
	    private String brand;
	 
	    private int size;
	    
	    private BigDecimal shopCoverage;
	    
	    private String mac;
		
	    private boolean smart;

}
