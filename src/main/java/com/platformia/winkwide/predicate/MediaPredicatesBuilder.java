package com.platformia.winkwide.predicate;

import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;

public class MediaPredicatesBuilder {

		private List<SearchCriteria> params;
	 
	    public MediaPredicatesBuilder() {
	        params = new ArrayList<>();
	    }
	 
	    public MediaPredicatesBuilder with(
	      String key, String operation, Object value) {
	   
	        params.add(new SearchCriteria(key, operation, value));
	        return this;
	    }
	 
	    public BooleanExpression build() {
	        if (params.size() == 0) {
	            return null;
	        }
	 
	        List<BooleanExpression> predicates = new ArrayList<>();
	        MediaPredicate predicate;
	        for (SearchCriteria param : params) {
	            predicate = new MediaPredicate(param);
	            BooleanExpression exp = predicate.getPredicate();
	            if (exp != null) {
	                predicates.add(exp);
	            }
	        }
	 
	        BooleanExpression result = predicates.get(0);
	        for (int i = 1; i < predicates.size(); i++) {
	            result = result.and(predicates.get(i));
	        }
	        return result;
	    }
	}