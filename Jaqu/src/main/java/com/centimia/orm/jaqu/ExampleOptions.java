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
Created		   Nov 1, 2012			shai

*/
package com.centimia.orm.jaqu;

import java.util.HashSet;

/**
 * Example Options is used in selectByExample in order to clarify and exact the search example Object
 * @author shai
 */
public interface ExampleOptions {
	
	/**
	 * Some times when using a "select by example" some fields in the example object may hold values by default.
	 * Their values will effect the search string sent to the db. Here you can exclude them from the search.
	 * <br>
	 * Use this method to set which fields you wish to exclude
	 * 
	 * @param excludeProps
	 * @return ExampleOptions (this)
	 */
	public ExampleOptions setExcludeProps(HashSet<String> excludeProps);
	
	/**
	 * get the list of exclude properties (which you don't want to include in the search).
	 * @return HashSet&lt;String&gt;
	 */
	public HashSet<String> getExcludeProps();
	
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
	 * @param mode
	 * @see {@link LikeMode}
	 * @return ExampleOptions
	 */
	public ExampleOptions setLikeMode(LikeMode mode);
	
	/**
	 * returns the Like mode for string values.
	 * @return LikeMode
	 */
	public LikeMode getLikeMode();
}
