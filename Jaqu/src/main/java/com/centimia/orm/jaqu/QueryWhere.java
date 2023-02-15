/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 2.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group, Centimia Inc.
 */
package com.centimia.orm.jaqu;

import java.util.List;
import java.util.Map;

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
		return new QueryCondition<>(query, x);
	}

	/**
	 * wraps the entity object and changes the condition to support the primary key.
	 * Useful in cases when the object holds an entity relation but you do not want to create the relation in the
	 * fluent query.
	 *
	 * @see {@link Db#asPrimaryKey(Object, Class))}
	 * @param <K>
	 * @param <A>
	 * @param mask
	 * @return QueryCondition
	 */
	public <K, A> QueryCondition<T, A> and(GenericMask<K, A> mask) {
		query.addConditionToken(ConditionAndOr.AND);
		return new QueryCondition<>(query, mask, mask.mask());
	}

	/**
	 * Perform an 'OR' operator in the query
	 * @param <A> - represents any field type that exists on Object <T>
	 * @param x- The field (gives field name) that should be attached with 'OR' to the query
	 * @return QueryCondition
	 */
	public <A> QueryCondition<T, A> or(A x) {
		query.addConditionToken(ConditionAndOr.OR);
		return new QueryCondition<>(query, x);
	}

	/**
	 * wraps the entity object and changes the condition to support the primary key.
	 * Useful in cases when the object holds an entity relation but you do not want to create the relation in the
	 * fluent query.
	 *
	 * @see {@link Db#asPrimaryKey(Object, Class))}
	 * @param <K>
	 * @param <A>
	 * @param mask
	 * @return QueryCondition
	 */
	public <K, A> QueryCondition<T, A> or(GenericMask<K, A> mask) {
		query.addConditionToken(ConditionAndOr.OR);
		return new QueryCondition<>(query, mask, mask.mask());
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
	 * wraps everything following with "("<br>
	 * <b>Must follow with matching "endWrap</b>
	 *
	 * @return QueryWhere&lt;T&gt;
	 */
	public QueryWhere<T> wrap() {
    	query.addConditionToken(new Token() {
			@Override
			public <K> void appendSQL(SQLStatement stat, Query<K> query) {
				stat.appendSQL("(");
			}
		});
    	return this;
    }

	/**
	 * ends a previous wrap with ")"
	 * @return QueryWhere&lt;T&gt;
	 */
	public QueryWhere<T> endWrap() {
    	query.addConditionToken(new Token() {
			@Override
			public <K> void appendSQL(SQLStatement stat, Query<K> query) {
				stat.appendSQL(")");
			}
		});
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
	 * Returns a List of the main "from" type based on a Union between the two queries.<br>
	 * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> union(Query<U> unionQuery) {
		return query.union(unionQuery);
	}

	/**
	 * Returns a List of the main "from" type based on a Union between the two queries.<br>
	 * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> union(QueryWhere<U> unionQuery) {
		return query.union(unionQuery);
	}

	/**
	 * Returns a List of the main "from" type based on a Union between the two queries.<br>
	 * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> union(QueryJoinWhere<U> unionQuery) {
		return query.union(unionQuery);
	}

	/**
	 * same as {@link #union(QueryJoinWhere)} but returns distinct results of both queries.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> unionDistinct(QueryJoinWhere<U> unionQuery) {
		return query.unionDistinct(unionQuery);
	}

	/**
	 * same as {@link #union(QueryWhere)} but returns distinct results of both queries.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> unionDistinct(QueryWhere<U> unionQuery) {
		return query.unionDistinct(unionQuery);
	}

	/**
	 * same as {@link #union(Query)} but returns distinct results of both queries.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> unionDistinct(Query<U> unionQuery) {
		return query.unionDistinct(unionQuery);
	}

	/**
	 * same as {@link #union(QueryJoinWhere, Object)} but returns distinct results of both queries.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U, X> List<X> unionDistinct(QueryJoinWhere<U> unionQuery, X x) {
		return query.unionDistinct(unionQuery, x);
	}

	/**
     * Returns a list of the given type (x). The type must be a new type, not one of the table's fields.
     * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 *
	 * @param unionQuery
	 * @param x - the type to return
	 * @return List<X>
	 */
	public <U, X> List<X> union(QueryJoinWhere<U> unionQuery, X x) {
		return query.union(unionQuery, x);
	}

	/**
	 * same as {@link #union(QueryWhere, Object)} but returns distinct results of both queries.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U, X> List<X> unionDistinct(QueryWhere<U> unionQuery, X x) {
		return query.unionDistinct(unionQuery, x);
	}

	/**
     * Returns a list of the given type (x). The type must be a new type, not one of the table's fields.
     * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 *
	 * @param unionQuery
	 * @param x - the type to return
	 * @return List<X>
	 */
	public <U, X, Z> List<X> union(QueryWhere<U> unionQuery, Z x) {
		return query.union(unionQuery, x);
	}

	/**
	 * same as {@link #union(Query, Object)} but returns distinct results of both queries.
	 *
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U, X> List<X> unionDistinct(Query<U> unionQuery, X x) {
		return query.unionDistinct(unionQuery, x);
	}

	/**
     * Returns a list of the given type (x). The type must be a new type, not one of the table's fields.
     * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 *
	 * @param unionQuery
	 * @param x - the type to return
	 * @return List<X>
	 */
	public <U, X> List<X> union(Query<U> unionQuery, X x) {
		return query.union(unionQuery, x);
	}

	/**
	 * Performs A select similar to {@link #select(Object)} but with the 'DISTINCT' directive.
	 * Returns results or empty List. Never 'null'
	 *
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
	 * Returns a map of all the query's results where key is one field in the select and value is another.<br>
	 * It is also available in join queries. You can have a key from any table within the join and a value from any table as well.<br>
	 * <b>Note:</b> If keys are not unique they will override.<br>
	 * example:
	 * <pre>
	 * Db sb = [new session];
	 * Table t = [tableDescriptor]
	 * Map<Long, String> results = db.from(t).where(t.[getSomeField()]).is)[someValue]....selectAsMap(t.getA(), t.getB());
	 * </pre>
	 *
	 * @param key
	 * @param value
	 * @return Map<K, V>
	 */
	public <K, V> Map<K, V> selectAsMap(K key, V value){
		return query.selectAsMap(key, value);
	}

	/**
	 * same as {@link #selectAsMap(Object, Object)} but returns only distinct results.
	 *
	 * @see #selectAsMap(Object, Object)
	 * @param key
	 * @param value
	 * @return Map<K, V>
	 */
	public <K, V> Map<K, V> selectDistinctAsMap(K key, V value){
		return query.selectDistinctAsMap(key, value);
	}

	/**
	 * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
	 * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
	 *
	 * @param tableClass - the object descriptor of the type needed on return
	 * @throws JaquError - when not in join query
	 * @return List<U>
	 */
	public <U> List<U> selectRightHandJoin(U table) {
		return query.selectRightHandJoin(table);
	}

	/**
	 * A convenience method to a field of get the object representing the right hand side of the join relationship only. Based on a single field
	 * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
	 *
	 * @param tableClass - the object descriptor of the type needed on return
	 * @throws JaquError - when not in join query
	 * @return List<Z>
	 */
	public <U, Z> List<Z> selectRightHandJoin(U table, Z x) {
		return query.selectRightHandJoin(table, x);
	}

    /**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     *
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return U
     */
	public <U> U selectFirstRightHandJoin(U table) {
		return query.selectFirstRightHandJoin(table);
	}

	/**
     * A convenience method to get a field of the object representing the right hand side of the join relationship only. Based on a single field
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     *
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return Z
     */
	public <U, Z> Z selectFirstRightHandJoin(U table, Z x) {
		return query.selectFirstRightHandJoin(table, x);
	}

    /**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     *
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return List<U>
     */
	public <U> List<U> selectDistinctRightHandJoin(U table) {
		return query.selectDistinctRightHandJoin(table);
	}

	/**
     * A convenience method to get a field of the object representing the right hand side of the join relationship only. Based on a single field
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     *
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return List<Z>
     */
	public <U, Z> List<Z> selectDistinctRightHandJoin(U table, Z x) {
		return query.selectDistinctRightHandJoin(table, x);
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
	 * adds a limit to the query
	 * @param limitNum
	 * @return QueryInterface<T>
	 */
	public QueryInterface<T> limit(int limitNum) {
		return query.limit(limitNum);
	}

	/**
	 * Order by one or more columns in ascending order.
	 *
	 * @param expressions the order by expressions
	 * @return QueryWhere -  the query
	 */
	public QueryWhere<T> orderBy(Object... expressions) {
		for (Object expr : expressions) {
			OrderExpression<T> e = new OrderExpression<>(query, expr, false, false, false);
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
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], false, true, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], false, true, false);
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
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], false, false, true);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], false, false, true);
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
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], true, false, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], true, true, false);
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
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], true, true, false);
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
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], true, false, true);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(query, expr[length - 1], true, false, true);
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
