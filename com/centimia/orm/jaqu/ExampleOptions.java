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
Created		   Nov 1, 2012			shai

*/
package com.centimia.orm.jaqu;

import java.util.Map;

/**
 * 
 * @author shai
 */
public interface ExampleOptions {
	
	/**
	 * set properties you want to exclude
	 * 
	 * @param excludeProps
	 * @return ExampleOptions (this)
	 */
	public ExampleOptions setExcludeProps(Map<String, String> excludeProps);
	
	/**
	 * get the list of exclude properties (which you don't want to include in the search).
	 * @return Map<String, String>
	 */
	public Map<String, String> getExcludeProps();
	
	/**
	 * add a property name to the exclude property list
	 * @param property
	 * @return ExampleOptions (this)
	 */
	public ExampleOptions addExcludeProp(String property); 
	
	/**
	 * remove a property from the exclude list
	 * @param property
	 * @return ExampleOptions
	 */
	public ExampleOptions removeExcludeProp(String property);
	
	/**
	 * if 'false' null values will be included. Default is 'true'
	 * 
	 * @param exclude
	 * @return ExampleOptions
	 */
	public ExampleOptions setExcludeNulls(boolean exclude); 
	
	/**
	 * return the state of 'null' excludes
	 * @return boolean
	 */
	public boolean getExcludeNulls();
	
	/**
	 * set to 'true' if to exclude 0 value fields
	 * @param exclude
	 * @return ExampleOptions
	 */
	public ExampleOptions setExcludeZeros(boolean exclude);
	
	/**
	 * If true '0' value fields will be disregarded
	 * @return boolean
	 */
	public boolean getExcludeZeros();
	
//	public ExampleOptions setIgnoreCase(boolean ignore);
//	
//	public boolean getIgnoreCase();
	
	/**
	 * set the Like mode for string values.
	 * 
	 * @see {@link LikeMode}
	 * @param mode
	 * @return ExampleOptions
	 */
	public ExampleOptions setLikeMode(LikeMode mode);
	
	/**
	 * retruns the Like mode for string values.
	 * @return LikeMode
	 */
	public LikeMode getLikeMode();
}
