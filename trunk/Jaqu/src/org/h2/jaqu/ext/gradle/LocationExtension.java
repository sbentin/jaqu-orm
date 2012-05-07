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
Created		   May 6, 2012			shai

*/
package org.h2.jaqu.ext.gradle;

/**
 * @author shai
 *
 */
public class LocationExtension {

	public String outputDir = null;
	
	public LocationExtension() {
		
	}
	
	public LocationExtension(String dir) {
		this.outputDir = dir;
	}
}
