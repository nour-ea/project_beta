package com.platformia.winkwide.core.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "machine")
public class MachineAccountProperties  {

		private String defaultPassword;
	    
	}
