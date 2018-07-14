package com.platformia.winkwide.dao;

import java.io.Serializable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class GenericHibernateDao< T extends Serializable >
  extends AbstractHibernateDao< T > implements IGenericDao< T >{
	
}