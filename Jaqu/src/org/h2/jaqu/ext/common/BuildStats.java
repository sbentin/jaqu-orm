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
package org.h2.jaqu.ext.common;

/**
 * @author shai
 *
 */
public class BuildStats {

	int success;
	int failure;
	
	public BuildStats(int success, int failure) {
		this.success = success;
		this.failure = failure;
	}

	/**
	 * @return the success
	 */
	public int getSuccess() {
		return this.success;
	}

	/**
	 * @return the failure
	 */
	public int getFailure() {
		return this.failure;
	}
	
}
