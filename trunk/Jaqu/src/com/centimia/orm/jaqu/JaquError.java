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
 * 15/07/2011		shai				 create
 */
package com.centimia.orm.jaqu;

/**
 * 
 * @author shai
 */
public class JaquError extends RuntimeException {

	private static final long serialVersionUID = 2818663786498065024L;

	/**
	 * 
	 */
	JaquError() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	JaquError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	JaquError(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	JaquError(Throwable cause) {
		super(cause);
	}
}
