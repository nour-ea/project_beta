package com.platformia.winkwide.form;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProgramEntryForm {

	private Long id;
	
	private java.util.Date startTime;
	
	private java.util.Date endTime;
	
    private Long displayId;
    
    private String mediaLoop;
 
    private BigDecimal utilization;
	
    
}
