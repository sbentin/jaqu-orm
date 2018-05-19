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
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   May 7, 2012			shai

*/
package com.centimia.orm.jaqu.ext.common;

/**
 * @author shai
 *
 */
public class BuildStats {

	int success;
	int failure;
	int ignored;
	
	public BuildStats(int success, int failure, int ignored) {
		this.success = success;
		this.failure = failure;
		this.ignored = ignored;
	}

	/**
	 * @return the number of files we tried to enhance and succeeded
	 */
	public int getSuccess() {
		return this.success;
	}

	/**
	 * @return the number of files which we try to enhance and failed
	 */
	public int getFailure() {
		return this.failure;
	}
	
	/**
	 * @return the number of files which were not touched at all
	 */
	public int getIgnored() {
		return this.failure;
	}
}
