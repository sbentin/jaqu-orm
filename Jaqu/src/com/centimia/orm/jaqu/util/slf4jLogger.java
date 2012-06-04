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
public class slf4jLogger implements Logger {
	
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("com.centimia.orm.jaqu.log");

	public void debug(String statement) {
		logger.debug(statement);		
	}

	public void info(String statement) {
		logger.info(statement);
	}

	public boolean isDebugEnabled(){
		return logger.isDebugEnabled();
	}
}
