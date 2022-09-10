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

import java.util.HashSet;
import java.util.Map;

/**
 * The current table in query
 * @author shai
 * @param <T>
 */
public interface ISelectTable<T> {

	public enum JOIN_TYPE {
		OUTER_JOIN("OUTER JOIN"),
		LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
		INNER_JOIN("INNER JOIN"), 
		NONE("");
		
		String type;
		
		JOIN_TYPE(String type){
			this.type = type;
		}
	}
	
	/**
	 * Returns the generated ID given to the table within the select<br>
	 * example: the application generates "select T1.a, T1.b from myTable T1...." this method call will return "T1"
	 * 
	 * @return String
	 */
	public String getAs();

	/**
	 * Returns a map of table IDs given to the joint tables. You can access the Alias if you have a pointer to the descriptor.
	 * If you do not have the descriptor consider using {@link#getOrderedJoins} where you get the joines in the order which they were
	 * put in the query.
	 * 
	 * @return Map<Object, String>
	 */
	public Map<Object, String> getJoins();
	
	/**
	 * Returns a HashSet of table IDs and their alias table name given to the joint tables. The ID's are given in the order of the join.
	 * @return HashSet<Alias>
	 */
	public HashSet<Alias> getOrderedJoins();

	/**
	 * Returns the type of join or none if the select table does not represent a joint table.
	 * <b>Note:</b> In a join query the initial table will return a join type of NONE
	 * 
	 * @return JOIN_TYPE
	 */
	public JOIN_TYPE getJoinType();

}