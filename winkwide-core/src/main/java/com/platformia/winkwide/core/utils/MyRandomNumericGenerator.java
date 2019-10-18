package com.platformia.winkwide.core.utils;

import java.io.Serializable;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class MyRandomNumericGenerator implements IdentifierGenerator {

	    public static final String generatorName = "myRandomNumericGenerator";

	    @Override
	    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object object) throws HibernateException {
	        return new Long(RandomStringUtils.randomNumeric(5));
	    }
}
