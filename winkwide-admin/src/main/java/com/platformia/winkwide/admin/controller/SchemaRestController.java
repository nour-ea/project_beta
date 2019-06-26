package com.platformia.winkwide.admin.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.core.model.EntitySchemaElement;

@RestController
public class SchemaRestController {

	//Get the schema for different entities
	@RequestMapping(value = "api/schema/{entity}")
	public List<EntitySchemaElement> getEntitySchema(@PathVariable("entity") String entity){
		
		ArrayList<EntitySchemaElement> schema = new ArrayList<EntitySchemaElement>();
		
		//change entity first letter to Uppercase
		if(entity.length()>0)
			entity = toFirstUpperCase(entity);
		//extract schema info from entity class
		try {
		    Class<?> entityClass = Class.forName("com.platformia.winkwide.core.entity." + entity);

		    	for (Field field : entityClass.getDeclaredFields())
		    		//if(field.getName()!="id" && field.getName()!="serialVersionUID" && isNotJSONIgnore(field.getAnnotations()) )
		    		if( 						  field.getName()!="serialVersionUID" && isNotJSONIgnore(field.getAnnotations()) )
		    			schema.add(new EntitySchemaElement( field.getName(), toFirstUpperCase(field.getName()),  field.getType().getSimpleName() ) );
		    	
		 } catch (Exception e) {
		        e.printStackTrace();
		}
		
		return schema;
	}

	private boolean isNotJSONIgnore(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if(annotation.annotationType().getSimpleName().equals("JsonIgnore")) 
				return false;
		}
		return true;
	}
	
	private String toFirstUpperCase (String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
}