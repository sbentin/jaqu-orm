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
 * 03/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

/**
 * Allows String based SQL clause inside a condition. It can be a stand alone where or just a condition to 
 * be concatenated with other conditions.<br>
 * <b>Note:</b> Currently is not supported by join queries.
 * <p><pre>
 * use: 
 * select.from(persistable).where(new StringFilter(){...}).select();
 * select.from(persistable).where(new StringFilter(){...}).and(persistable.getSomeField()).is(someValue).select()
 * </pre>
 * 
 * @author shai
 */
public interface StringFilter {
	
	/**
	 * Returns the condition string that should be injected into the SQL.
	 * 
	 * @param selectTable
	 * @return String
	 */
	String getConditionString(ISelectTable<?> selectTable);
	
}
