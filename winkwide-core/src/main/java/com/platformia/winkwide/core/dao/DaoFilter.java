package com.platformia.winkwide.core.dao;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DaoFilter {

	private String operator; // eq, gt, like, ilike, between, isNull, isNotNull, isEmpty, isNotEmpty,
	private String order; // order.desc, order.asc
	private String column;
	private String value1;
	private String value2;

	public DaoFilter(String operator, String order, String column, String value1, String value2) {
		super();
		this.operator = operator;
		this.order = order;
		this.column = column;
		this.value1 = value1;
		this.value2 = value2;
	}

	// handling cases : eq, gt, st, like, ilike, between, isNull, isNotNull,
	// isEmpty, isNotEmpty
	public Criterion getCriterion() {
		Criterion criterion = null;

		if (operator != null && !operator.isEmpty()) {

			if (value1 != null && !value1.isEmpty()) {
				switch (operator) {
				case "eq":
					criterion = Restrictions.eq(column, value1);
				case "gt":
					criterion = Restrictions.gt(column, value1);
				case "st":
					criterion = Restrictions.gt(column, value1);
				case "like":
					criterion = Restrictions.like(column, value1);
				case "ilike":
					criterion = Restrictions.ilike(column, value1);
				}
				if (value2 != null && !value2.isEmpty()) {
					switch (operator) {
					case "between":
						criterion = Restrictions.between(column, value1, value2);
					}
				}
			}

			switch (operator) {
			case "isNull":
				criterion = Restrictions.isNull(column);

			case "isNotNull":
				criterion = Restrictions.isNotNull(column);

			case "isEmpty":
				criterion = Restrictions.isEmpty(column);

			case "isNotEmpty":
				criterion = Restrictions.isNotEmpty(column);
			}
		}

		return criterion;
	}

	// handling cases : order.desc, order.asc
	public Order getCriteriaOrder() {
		Order criteriaOrder = null;

		if (order != null && !order.isEmpty()) {
			switch (order) {
			case "orderAsc":
				criteriaOrder = Order.asc(column);

			case "orderDesc":
				criteriaOrder = Order.asc(column);
			}
		}

		return criteriaOrder;
	}

	// Getters & Setters

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	// toString
	public String toString() {
		return "DaoFilter : " + "operator: " + operator + ", order: " + order + ", column: " + column + ", value1: "
				+ value1 + ", value2: " + value2;
	}

}
