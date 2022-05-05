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
Created		   Nov 2, 2012			shai

*/
package com.centimia.orm.jaqu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * A general pattern exampleOptions implementation
 * @author shai
 */
public class GeneralExampleOptions implements ExampleOptions {

	protected HashSet<String> fields;
	protected boolean	excludeNulls = true;
	protected boolean	excludeZeros = true;
	protected LikeMode	likeMode = LikeMode.EXACT;
	
	/**
	 * Construct with a list of excluded fields
	 * @param excludeFields
	 */
	public GeneralExampleOptions(String[] excludeFields) {
		this.fields = new HashSet<>();
		this.fields.addAll(Arrays.asList(excludeFields));
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setExcludeProps(java.util.HashSet)
	 */
	public ExampleOptions setExcludeProps(HashSet<String> excludeProps) {
		this.fields = new HashSet<String>(excludeProps);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getExcludeProps()
	 */
	public HashSet<String> getExcludeProps() {
		return this.fields;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#addExcludeProp(java.lang.String)
	 */
	public ExampleOptions addExcludeProp(String property) {
		this.fields.add(property);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#removeExcludeProp(java.lang.String)
	 */
	public ExampleOptions removeExcludeProp(String property) {
		this.fields.remove(property);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setExcludeNulls(boolean)
	 */
	public ExampleOptions setExcludeNulls(boolean exclude) {
		this.excludeNulls = exclude;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getExcludeNulls()
	 */
	public boolean getExcludeNulls() {
		return this.excludeNulls;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setExcludeZeros(boolean)
	 */
	public ExampleOptions setExcludeZeros(boolean exclude) {
		this.excludeZeros = exclude;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getExcludeZeros()
	 */
	public boolean getExcludeZeros() {
		return excludeZeros;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setLikeMode(com.centimia.orm.jaqu.LikeMode)
	 */
	public ExampleOptions setLikeMode(LikeMode mode) {
		this.likeMode = mode;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getLikeMode()
	 */
	public LikeMode getLikeMode() {
		return this.likeMode;
	}
	
	protected Hashtable<String, String> toMap(String[] fields){
		Hashtable<String, String> toMap = new Hashtable<String, String>();
		if (null != fields && 0 < fields.length) {
			for (String field: fields) {
				toMap.put(field, field);
			}
		}
		return toMap;
	}
}
