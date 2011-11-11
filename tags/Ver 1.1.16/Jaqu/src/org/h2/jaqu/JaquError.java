/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 15/07/2011		shai				 create
 */
package org.h2.jaqu;

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
