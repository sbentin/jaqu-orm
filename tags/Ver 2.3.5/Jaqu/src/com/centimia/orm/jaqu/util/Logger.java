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
package com.centimia.orm.jaqu.util;

/**
 * @author shai
 *
 */
public interface Logger {

	/**
	 * Output to a given log statements relevant as info
	 * @param statement
	 */
	void info(String statement);

	/**
	 * Output to a given log statements relevant as debug
	 * @param statement
	 */
	void debug(String statement);

	public boolean isDebugEnabled();
}
