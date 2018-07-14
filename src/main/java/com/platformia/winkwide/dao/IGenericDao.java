package com.platformia.winkwide.dao;

import java.io.Serializable;
import java.util.List;

public interface IGenericDao<T extends Serializable> {
	 
	   //Open requests
	
	   T findOne(final long id);
	   
	   T findOne(String id); 
	 
	   List<T> findAll();
	    
	   void create(final T entity);
	 
	   void update(final T entity);
	 
	   void delete(final T entity);
	 
	   void deleteById(final long entityId);
	   
	   void deleteById(final String entityId);
	   
	}
