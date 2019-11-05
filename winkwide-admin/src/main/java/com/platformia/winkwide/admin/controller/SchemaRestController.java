package com.platformia.winkwide.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platformia.winkwide.core.model.EntitySchemaElement;
import com.platformia.winkwide.core.utils.SchemaUtils;

@RestController
public class SchemaRestController {

	// Get the schema for different entities
	@RequestMapping(value = "api/schema/{entity}")
	public List<EntitySchemaElement> getEntitySchema(@PathVariable("entity") String entity) {

		ArrayList<EntitySchemaElement> schema = new ArrayList<EntitySchemaElement>();

		try {
			schema = SchemaUtils.getEntitySchema(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return schema;
	}


}