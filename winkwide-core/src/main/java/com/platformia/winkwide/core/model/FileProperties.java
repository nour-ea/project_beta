package com.platformia.winkwide.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileProperties {

	private String name;
	private String format;
	private long size;
	private String url;
	
}
