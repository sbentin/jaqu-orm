/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
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
