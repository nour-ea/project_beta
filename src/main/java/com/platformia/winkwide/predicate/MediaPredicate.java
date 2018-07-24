package com.platformia.winkwide.predicate;

import org.apache.commons.lang.StringUtils;

import com.platformia.winkwide.entity.Media;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class MediaPredicate {
	 
    private SearchCriteria criteria;
 
    public BooleanExpression getPredicate() { 
       
    		PathBuilder<Media> entityPath = new PathBuilder<>(Media.class, "media");
 
        if (StringUtils.isNumeric(criteria.getValue().toString())) {
            NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
            int value = Integer.parseInt(criteria.getValue().toString());
            switch (criteria.getOperation()) {
                case ":":
                    return path.eq(value);
                case ">":
                    return path.goe(value);
                case "<":
                    return path.loe(value);
            }
        } 
        else {
            StringPath path = entityPath.getString(criteria.getKey());
            if (criteria.getOperation().equalsIgnoreCase(":")) {
                return path.containsIgnoreCase(criteria.getValue().toString());
            }
        }
        return null;
    }
}