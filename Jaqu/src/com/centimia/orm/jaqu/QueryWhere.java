/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *  
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group, Centimia Inc.
 */
package com.centimia.orm.jaqu;

import java.util.List;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;

/**
 * This class represents a query with a condition.
 * 
 * @param <T> the return type
 */
public class QueryWhere<T> {

	Query<T> query;

	QueryWhere(Query<T> query) {
		this.query = query;
	}

	/**
	 * Perform an 'AND' operator in the query
	 * @param <A> - represents any field type that exists on Object <T> 
	 * @param x - The field (gives field name) that should be attached with 'AND' to the query
	 * @return QueryCondition
	 */
	public <A> QueryCondition<T, A> and(A x) {
		query.addConditionToken(ConditionAndOr.AND);
		return new QueryCondition<T, A>(query, x);
	}

	/**
	 * Perform an 'OR' operator in the query
	 * @param <A> - represents any field type that exists on Object <T> 
	 * @param x- The field (gives field name) that should be attached with 'OR' to the query
	 * @return QueryCondition
	 */
	public <A> QueryCondition<T, A> or(A x) {
		query.addConditionToken(ConditionAndOr.OR);
		return new QueryCondition<T, A>(query, x);
	}

	/**
	 * Use this method specifically to perform an 'AND' operator in the query on enum types
	 * @param alias
	 * @param fieldName
	 * @param comapreType
	 * @param values
	 * @return QueryWhere<T>
	 */
	@SuppressWarnings("unchecked")
	public QueryWhere<T> andEnum(T alias, String fieldName, final CompareType comapreType, final Enum<?> ... values) {
		query.addConditionToken(ConditionAndOr.AND);
		TableDefinition<T> tDef =  (TableDefinition<T>) query.getDb().define(alias.getClass());
    	final FieldDefinition fDef = tDef.getDefinitionForField(fieldName);
    	EnumToken t = new EnumToken(fDef, comapreType, values);
    	query.addConditionToken(t);
    	return this;
	}

	/**
	 * Use this method specifically to perform an 'OR' operator in the query on enum types
	 * @param alias
	 * @param fieldName
	 * @param comapreType
	 * @param values
	 * @return QueryWhere<T>
	 */
	@SuppressWarnings("unchecked")
	public QueryWhere<T> orEnum(T alias, String fieldName, final CompareType comapreType, final Enum<?> ... values) {
		query.addConditionToken(ConditionAndOr.OR);
		TableDefinition<T> tDef =  (TableDefinition<T>) query.getDb().define(alias.getClass());
    	final FieldDefinition fDef = tDef.getDefinitionForField(fieldName);
    	EnumToken t = new EnumToken(fDef, comapreType, values);
    	query.addConditionToken(t);
    	return this;
	}
	
	/**
	 * Performs the select of the query. Returns the results or an empty list. Does not return null.
	 * This select returns a List of a given object that mapping is given to from the result set to the field in that object 
	 * 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return List<X>
	 */
	public <X, Z> List<X> select(Z x) {
		return query.select(x);
	}

	/**
	 * Returns the SQL String to be performed. Use for Debug.
	 * @return String
	 */
	public String getSQL() {
		SQLStatement selectList = new SQLStatement(query.getDb());
		selectList.appendSQL("*");
		return query.prepare(selectList, false).getSQL().trim();
	}

	/**
	 * Performs A select similar to {@link #select(Object)} but with the 'DISTINCT' directive.
	 * Returns results or empty List. Never 'null'
	 * 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return List<X>
	 */
	public <X, Z> List<X> selectDistinct(Z x) {
		return query.selectDistinct(x);
	}

	/**
	 * Returns the 'Z' type object from the first result
	 * 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return a result or null if there is no result
	 */
	public <X, Z> X selectFirst(Z x) {
		List<X> list = query.select(x);
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Perform the built query Select.
	 * 
	 * @return List<T> can be a list of one, many or empty, never 'null'. Can be used in a primary key select, but using
	 *         {@link #selectFirst} is advised.
	 */
	public List<T> select() {
		return query.select();
	}

	/**
	 * Select the first result and return it. Should also be used in a primary key select.
	 * 
	 * @return T result or null if there is no result
	 */
	public T selectFirst() {
		List<T> list = select();
		return list.isEmpty() ? null : list.get(0);
	}

	public List<T> selectDistinct() {
		return query.selectDistinct();
	}

	public <U> List<U> selectRightHandJoin(U table) {
		return query.selectRightHandJoin(table);
	}

	public <U> U selectFirstRightHandJoin(U table) {
		return query.selectFirstRightHandJoin(table);
	}

	public <U> List<U> selectDistinctRightHandJoin(U table) {
		return query.selectDistinctRightHandJoin(table);
	}

	// ## Java 1.5 end ##

	public Query<T> groupBy(Object ... groupBy){
		return query.groupBy(groupBy);
	}
	
	/**
	 * Order by one or more columns in ascending order.
	 * 
	 * @param expressions the order by expressions
	 * @return QueryWhere -  the query
	 */
	public QueryWhere<T> orderBy(Object... expressions) {
		for (Object expr : expressions) {
			OrderExpression<T> e = new OrderExpression<T>(query, expr, false, false, false);
			query.addOrderBy(e);
		}
		return this;
	}

	/**
	 * Order by one or more columns in descending order
	 * @param expr
	 * @return QueryWhere<T> - the query
	 */
	public QueryWhere<T> orderByNullsFirst(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, true, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, true, false);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by one or more columns in ascending order
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryWhere<T> orderByNullsLast(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, false, true);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, false, true);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by in descending order
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryWhere<T> orderByDesc(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, false, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by in descending order nulls will be first
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryWhere<T> orderByDescNullsFirst(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by in descending order nulls will be last
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryWhere<T> orderByDescNullsLast(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, false, true);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, false, true);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Deletes the rows / entities that corresponds to the query.
	 * @return int - number of rows affected
	 */
	public int delete() {
		return query.delete();
	}

	/**
	 * Updates the rows / entities that corresponds to the query.
	 * @return int - number of rows affected
	 */
	public int update() {
		return query.update();
	}

	/**
	 * returns the count of rows / entities that correspond to the query.
	 * @return int - the count
	 */
	public long selectCount() {
		return query.selectCount();
	}

}
