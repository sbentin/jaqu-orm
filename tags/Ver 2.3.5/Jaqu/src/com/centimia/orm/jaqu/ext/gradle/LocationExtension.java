/*
 * Copyright (c) 2007-2016 Centimia Ltd.
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
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   May 6, 2012			shai

*/
package com.centimia.orm.jaqu.ext.gradle;

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
