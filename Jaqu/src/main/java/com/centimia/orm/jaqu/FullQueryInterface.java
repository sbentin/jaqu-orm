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
/**
 * The use of query interfaces is to divide different query abilities in different stages of the query.
 *
 * @author Shai Bentin
 * @param <T>
 */
public interface FullQueryInterface<T> extends QueryInterface<T> {
	/**
	 * Select only distinct results in the table
	 *
	 * @return List<T>
	 */
	public abstract List<T> selectDistinct();

	/**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     *
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return List<U>
     */
	public abstract <U> List<U> selectDistinctRightHandJoin(U tableClass);

	/**
     * A convenience method to a field of the object representing the right hand side of the join relationship only. Based on a single field only
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     *
     * @param tableClass - the object descriptor of the type needed on return
     * @throws JaquError - when not in join query
     * @return List<U>
     */
	public <U, Z> List<Z> selectDistinctRightHandJoin(U tableClass, Z x);

	/**
	 * Perform the update requested by the specific where clause
	 * <b>Note</b> Since update executes without objects the multi reEntrent cache is cleared
	 * and objects taken from the db before will no longer be the same instance if fetched again</b>
	 *
	 * @return int - number of lines updated.
	 */
	public abstract int update();

	/**
	 * Returns distinct results of type X using a query on object Z
	 *
	 * @param <X> - The type of object returned after the specific object is built from results.
	 * @param <Z> - The type of the Object used for describing the query
	 * @param x - A descriptor instance of type Z
	 * @return List<X>
	 */
	public abstract <X, Z> List<X> selectDistinct(Z x);

	/**
	 * returns the query of an sql
	 * @return String
	 */
	public String getDistinctSQL();

	/**
	 * returns the query of a distinct select based on the given object
	 * @param z
	 * @return String
	 */
	public <Z> String getDistinctSQL(Z z);
}