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
		if (null != excludeFields)
			this.fields.addAll(Arrays.asList(excludeFields));
	}

	@Override
	public ExampleOptions setExcludeProps(HashSet<String> excludeProps) {
		this.fields = new HashSet<>(excludeProps);
		return this;
	}

	@Override
	public HashSet<String> getExcludeProps() {
		return this.fields;
	}

	@Override
	public ExampleOptions addExcludeProp(String property) {
		this.fields.add(property);
		return this;
	}

	@Override
	public ExampleOptions removeExcludeProp(String property) {
		this.fields.remove(property);
		return this;
	}

	@Override
	public ExampleOptions setExcludeNulls(boolean exclude) {
		this.excludeNulls = exclude;
		return this;
	}

	@Override
	public boolean getExcludeNulls() {
		return this.excludeNulls;
	}

	@Override
	public ExampleOptions setExcludeZeros(boolean exclude) {
		this.excludeZeros = exclude;
		return this;
	}

	@Override
	public boolean getExcludeZeros() {
		return excludeZeros;
	}

	@Override
	public ExampleOptions setLikeMode(LikeMode mode) {
		this.likeMode = mode;
		return this;
	}

	@Override
	public LikeMode getLikeMode() {
		return this.likeMode;
	}
}
