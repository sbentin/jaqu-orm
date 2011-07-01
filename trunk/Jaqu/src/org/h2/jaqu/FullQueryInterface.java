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
 * Initial Developer: Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 02/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu;

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
	 * In a join query return distinct results from the right hand side of the join only
	 * 
	 * @param <U> - the right hand join type.
	 * @param tableClass the right hand descriptor instance.
	 * @return List<U>
	 */
	public abstract <U> List<U> selectDistinctRightHandJoin(U tableClass);

	/**
	 * Perform the update requested by the specific where clause
	 * 
	 * @return int - number of lines updated.
	 */
	public abstract int update();

	/**
	 * Returns distinct results of type X using az query on object Z
	 * 
	 * @param <X> - The type of object returned after the specific object is built from results.
	 * @param <Z> - The type of the Object used for describing the query
	 * @param x - A descriptor instance of type Z
	 * @return List<X>
	 */
	public abstract <X, Z> List<X> selectDistinct(Z x);
}