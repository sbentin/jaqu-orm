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

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 02/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import java.util.List;

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
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return X
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
	 * 
	 * @return int - number of rows deleted
	 */
	public abstract int delete();

	/**
	 * Returns the result of a select for object of type Z from Table T. 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return List<X> results
	 */
	public abstract <X, Z> List<X> select(Z x);
	
	/**
	 * enables the select of the object representing the right hand side of an inner or outer join.
	 * @param <U>
	 * @param tableClass
	 * @return List<U>
	 */
	public abstract <U> List<U> selectRightHandJoin(U tableClass);
	
	/**
	 * Utility method to return the first result of a right hand side of an inner or outer join.
	 * @param <U>
	 * @param tableClass
	 * @return U
	 */
	public abstract <U> U selectFirstRightHandJoin(U tableClass);

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

	public abstract List<T> union(String unionQuery);

	public abstract List<T> intersect(String intersectQuery);
}