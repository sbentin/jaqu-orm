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

import com.centimia.orm.jaqu.util.Logger;
import com.centimia.orm.jaqu.util.SimpleLogger;
import com.centimia.orm.jaqu.util.slf4jLogger;

/**
 * Utility class to optionally log generated statements to an output stream.<br>
 * Default output stream is System.out.<br>
 * Statement logging is disabled by default.
 * <p>
 * This class also tracks the counts for generated statements by major type.
 *
 */
class InternalLogger {

	private static Logger logger;
	static {
		try {
			InternalLogger.class.getClassLoader().loadClass("org.slf4j.Logger");
			logger = new slf4jLogger();
		}
		catch (Throwable t){
			logger = new SimpleLogger();
		}
	}
	
	static void debug(String info) {
		debug(info, null);
	}
    
	static void debug(String info, Throwable t) {
		if (null != t)
			info = info + ": " + t.getMessage();
		logger.debug(info);
		
		t.printStackTrace();
	}
}
