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

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 02/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import java.util.List;
import java.util.Map;

/**
 * The use of query interfaces is to divide different query abilities in different stages of the query.
 * 
 * @author Shai Bentin
 * @param <T>
 */
public interface QueryInterface<T> {

	/**
	 * Do an SQL "select count(*)" on the table
	 * 
	 * @return long, the count
	 */
	public abstract long selectCount();

	/**
	 * Perform the query select
	 * @return List<T> results
	 */
	public abstract List<T> select();

	/**
	 * Returns the first result of the select performed. 
	 * @return T
	 */
	public abstract T selectFirst();

	/**
	 * Returns the first result of a select for type Z. 
	 * Type Z can be any defined type with mappings from the result.
	 * 
	 * @param <Z>
	 * @param x
	 * @return Z
	 */
	public abstract <X, Z> X selectFirst(Z x);

	/**
	 * Returns a String representing the select assembled from the object query.
	 * 
	 * @return String
	 */
	public abstract String getSQL();

	/**
	 * Performs a delete query.
	 * <b>Note</b> Since delete executes without objects the multi reEntrent cache is cleared 
	 * and objects taken from the db before will no longer be the same instance if fetched again</b>
	 * 
	 * @return int - number of rows deleted
	 */
	public abstract int delete();

	/**
	 * Returns the result of a select for object of type Z from Table T. 
	 * 
	 * @param <Z>
	 * @param x
	 * @return List<Z> results
	 */
	public abstract <X, Z> List<X> select(Z x);
	
	/**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return List<U>
     */
	public abstract <U> List<U> selectRightHandJoin(U tableClass);
	
	/**
     * A convenience method get a field result from an object representing the right hand side of the join relationship only. This is for a single field only
     * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return List<Z>
     */
	public abstract <U, Z> List<Z> selectRightHandJoin(U tableClass, Z x);
	
	/**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     */
	public abstract <U> U selectFirstRightHandJoin(U tableClass);

	/**
     * A convenience method to get a field result from an object representing the right hand side of the join relationship only. This is for a single field only
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     */
	public abstract <U, Z> Z selectFirstRightHandJoin(U tableClass, Z x);
	
	/**
	 * Start a where clause on the SQL query.
	 * 
	 * @param <A>
	 * @param x
	 * @return QueryCondition<T, A>
	 */
	public abstract <A> QueryCondition<T, A> where(A x);

	/**
	 * Allows a simple string where clause
	 * @param <A>
	 * @param whereCondition
	 * @return QueryWhere<T>
	 */
	public abstract <A> QueryWhere<T> where(final StringFilter whereCondition);
	 
	/**
	 * Used on update queries to set the parameters of the update according to the
	 * given object.
	 * 
	 * @param x - field from descriptor object
	 * @param v - value of the same type.
	 * 
	 * @return QuerySet<T, A>
	 */
	public abstract <A> QuerySet<T, A> set(A x, A v);

	/**
	 * Returns a primary key condition. Usually used internally, when the primary key is a definit identifier which is unknown....
	 * Does not support complex primary keys. Use this query only when the primary key to compare with is definit, any other condition 
	 * is not supported
	 * 
	 * @return QueryCondition<T, Object>
	 */
	public abstract QueryCondition<T, Object> primaryKey();

	/**
	 * 
	 * @param condition
	 * @return QueryWhere<T>
	 */
	public abstract QueryWhere<T> whereTrue(Boolean condition);

	/**
	 * Group By ordered objects
	 * @param groupBy
	 * @return FullQueryInterface<T>
	 */
	public abstract FullQueryInterface<T> groupBy(Object... groupBy);


	/**
	 * inner Join another table. (returns only rows that match)
	 *
	 * @param alias an alias for the table to join
	 * @return the joined query
	 */
	public abstract <U> QueryJoin<T> innerJoin(U alias);
	
	/**
	 * Left Outer Join another table. (Return all rows from left table, and matching from rightHandSide)
	 *
	 * @param alias an alias for the table to join
	 * @return the joined query
	 */
	public abstract <U> QueryJoin<T> leftOuterJoin(U alias);

	/**
	 * Order by a number of columns.
	 *
	 * @param expressions the columns
	 * @return the query
	 */
	public abstract QueryInterface<T> orderBy(Object... expressions);

	/**
	 * Descending order by a single given column
	 * 
	 * @param expr
	 * @return QueryInterface
	 */
	public abstract QueryInterface<T> orderByDesc(Object ... expr);

	/**
	 * Order by one or more columns in descending order
	 * @param expr
	 * @return QueryWhere<T> - the query
	 */
	public QueryInterface<T> orderByNullsFirst(Object ... expr);

	/**
	 * Order by one or more columns in ascending order
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryInterface<T> orderByNullsLast(Object ... expr);

	/**
	 * Order by in descending order nulls will be first
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryInterface<T> orderByDescNullsFirst(Object ... expr);

	/**
	 * Order by in descending order nulls will be last
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryInterface<T> orderByDescNullsLast(Object ... expr);

	/**
	 * returns the query of an sql build acording to the given object
	 * @param z
	 * @return String
	 */
	public <Z> String getSQL(Z z);

	/**
	 * Create a having clause based on the column given.
	 * <b>You can only use a single having in a select clause</b>
	 * 
	 * @param x
	 * @return QueryCondition<T, A>
	 */
	public <A> QueryCondition<T, A> having(A x);
	
	/**
	 * having clause with a supported aggregate function
	 * @param function
	 * @param x
	 * @return QueryCondition<T, Long>
	 */
	public <A> QueryCondition<T, Long> having(HavingFunctions function, A x);

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
	public <K, V> Map<K, V> selectAsMap(K key, V value);
	
	/**
	 * same as {@link #selectAsMap(Object, Object)} but returns only distinct results.
	 * 
	 * @see #selectAsMap(Object, Object)
	 * @param key
	 * @param value
	 * @return Map<K, V>
	 */
	public <K, V> Map<K, V> selectDistinctAsMap(K key, V value);
	
	/**
	 * Returns a List of the main "from" type based on a Union between the two queries.<br>
	 * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> union(Query<U> unionQuery);

	/**
	 * Returns a List of the main "from" type based on a Union between the two queries.<br>
	 * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> union(QueryWhere<U> unionQuery);

	/**
	 * Returns a List of the main "from" type based on a Union between the two queries.<br>
	 * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> union(QueryJoinWhere<U> unionQuery);
	
	/**
	 * same as {@link #union(QueryJoinWhere)} but returns distinct results of both queries.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> unionDistinct(QueryJoinWhere<U> unionQuery);
	
	/**
	 * same as {@link #union(QueryWhere)} but returns distinct results of both queries.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> unionDistinct(QueryWhere<U> unionQuery);
	
	/**
	 * same as {@link #union(Query)} but returns distinct results of both queries.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U> List<T> unionDistinct(Query<U> unionQuery);
	
	/**
	 * same as {@link #union(QueryJoinWhere, Object)} but returns distinct results of both queries.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U, X> List<X> unionDistinct(QueryJoinWhere<U> unionQuery, X x);
	
	/**
     * Returns a list of the given type (x). The type must be a new type, not one of the table's fields.
     * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 * 
	 * @param unionQuery
	 * @param x - the type to return
	 * @return List<X>
	 */
	public <U, X> List<X> union(QueryJoinWhere<U> unionQuery, X x);	

	/**
	 * same as {@link #union(QueryWhere, Object)} but returns distinct results of both queries.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U, X> List<X> unionDistinct(QueryWhere<U> unionQuery, X x);

	/**
     * Returns a list of the given type (x). The type must be a new type, not one of the table's fields.
     * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 * 
	 * @param unionQuery
	 * @param x - the type to return
	 * @return List<X>
	 */
	public <U, X, Z> List<X> union(QueryWhere<U> unionQuery, Z x);	

	/**
	 * same as {@link #union(Query, Object)} but returns distinct results of both queries.
	 * 
	 * @param unionQuery
	 * @return List<T>
	 */
	public <U, X> List<X> unionDistinct(Query<U> unionQuery, X x);

	/**
     * Returns a list of the given type (x). The type must be a new type, not one of the table's fields.
     * this query is runs a union query of the two queries.<br>
	 * <b>Note:</b> All union query rules apply here. The queries must return the same amount of columns and have the same column types and names.
	 * 
	 * @param unionQuery
	 * @param x - the type to return
	 * @return List<X>
	 */
	public <U, X> List<X> union(Query<U> unionQuery, X x);
	
	/**
	 * adds a limit to the query
	 * @param limitNum
	 * @return Query<T>
	 */
	public Query<T> limit(int limitNum);
}