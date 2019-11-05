package com.platformia.winkwide.core.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

	private String rootDir;
	
	private String uploadDir;
	
	private String logDir;
	
	private String billingDir;
    
}