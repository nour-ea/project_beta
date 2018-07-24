package com.platformia.winkwide.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

@RestController
public class SchemaRestController {

	//Get the schema for different entities
	@RequestMapping(value = "/schema/{entity}")
	public Object getEntitySchema(@PathVariable("entity") String entity){
		
		JsonSchema schema = null;
		
		//change entity first letter to Capital
 
		//extract schema info from entity class
		try {
		    Class<?> entityClass = Class.forName("com.platformia.winkwide.entity." + entity);

		    	for (Field field : entityClass.getDeclaredFields())
		    	if(field.getName()!="id" && field.getName()!="serialVersionUID" && isNotJSONIgnore(field.getAnnotations()) )
		    	schema.add( new SchemaElement( field.getName(), field.getType().getSimpleName() ) );
		    
		 } catch (Exception e) {
		        e.printStackTrace();
		}
		
		return schema;
	}
	
}