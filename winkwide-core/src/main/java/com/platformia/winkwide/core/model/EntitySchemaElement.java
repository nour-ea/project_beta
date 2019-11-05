package com.platformia.winkwide.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntitySchemaElement {

	private String name;
	private String capitalizedName;
	private String title;
	private String type;
	private boolean optional;
	
}
