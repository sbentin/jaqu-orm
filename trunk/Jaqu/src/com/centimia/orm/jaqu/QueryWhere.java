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
	 * Create a having clause based on the column given.
	 * <b>You can only use a single having in a select clause</b>
	 * 
	 * @param x
	 * @return QueryCondition<T, A>
	 */
	public <A> QueryCondition<T, A> having(final A x) {
		return query.having(x);
	}
	
	/**
	 * having clause with a supported aggregate function
	 * @param function
	 * @param x
	 * @return QueryCondition<T, Long>
	 */
	public <A> QueryCondition<T, Long> having(HavingFunctions function, final A x) {		
		return query.having(function, x);
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
		return query.getSQL();
	}

	/**
	 * @see {@link Query#getDistinctSQL()}
	 * @return String
	 */
	public String getDistinctSQL() {
		return query.getDistinctSQL();
	}
	
	/**
	 * @see {@link Query#getSQL(Object)
	 * @param z
	 * @return String
	 */
	public <Z> String getSQL(Z z) {
		return query.getSQL(z);
	}
	
	/**
	 * @see {@link Query#getDistinctSQL(Object)
	 * @param z
	 * @return String
	 */
	public <Z> String getDistinctSQL(Z z) {
		return query.getDistinctSQL(z);
	}
	
	/**
	 * Intersects the query with the inner query returning all that match both selects.
	 * All regular rules apply, make sure both queries return the same result set type.
	 * 
	 * @param intersectQuery
	 * @return List<T>
	 */
	public List<T> intersect(String intersectQuery){
		return query.intersect(intersectQuery);
	}
	
	/**
	 * unions the query with the inner query returning all that match either one of the selects.
	 * All regular rules apply, make sure both queries return the same result set type.
	 * 
	 * @param intersectQuery
	 * @return List<T>
	 */
	public List<T> union(String intersectQuery){
		return query.union(intersectQuery);
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

	/**
	 * Select only distinct results in the table
	 * 
	 * @return List<T>
	 */
	public List<T> selectDistinct() {
		return query.selectDistinct();
	}

	/**
	 * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
	 * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
	 * 
	 * @param tableClass - the object descriptor of the type needed on return
	 * @throws JaquError - when not in join query
	 */
	public <U> List<U> selectRightHandJoin(U table) {
		return query.selectRightHandJoin(table);
	}
	
    /**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     */
	public <U> U selectFirstRightHandJoin(U table) {
		return query.selectFirstRightHandJoin(table);
	}

    /**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     */
	public <U> List<U> selectDistinctRightHandJoin(U table) {
		return query.selectDistinctRightHandJoin(table);
	}

	/**
	 * Group By ordered objects
	 * @param groupBy
	 * @return FullQueryInterface<T>
	 */
	public QueryInterface<T> groupBy(Object ... groupBy){
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
