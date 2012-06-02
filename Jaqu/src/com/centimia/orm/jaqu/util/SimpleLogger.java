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
public class SimpleLogger implements Logger {
	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.centimia.orm.jaqu.logger");

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.util.Logger#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return logger.isLoggable(java.util.logging.Level.FINEST);
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.util.Logger#debug(java.lang.String)
	 */
	public void debug(String statement) {
		logger.finest(statement);	
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.util.Logger#info(java.lang.String)
	 */
	public void info(String statement) {
		logger.info(statement);
	} 	
}
