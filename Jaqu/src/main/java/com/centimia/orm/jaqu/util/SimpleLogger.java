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
package com.centimia.orm.jaqu.util;

/**
 * @author shai
 *
 */
public class SimpleLogger implements Logger {
	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.centimia.orm.jaqu.logger");

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(java.util.logging.Level.FINEST);
	}

	@Override
	public void debug(String statement) {
		logger.finest(statement);	
	}

	@Override
	public void info(String statement) {
		logger.info(statement);
	}

	@Override
	public void error(String statement) {
		logger.severe(statement);
	} 	
}
