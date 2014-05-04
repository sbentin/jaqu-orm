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

import java.util.HashMap;
import java.util.Map;

/**
 * @author shai
 *
 */
class FullExampleOptions implements ExampleOptions {

	public FullExampleOptions() {
		
	}
	
	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setExcludeProps(java.util.List)
	 */
	public ExampleOptions setExcludeProps(Map<String, String> excludeProps) {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getExcludeProps()
	 */
	public Map<String, String> getExcludeProps() {
		return new HashMap<String, String>();
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#addExcludeProp(java.lang.String)
	 */
	public ExampleOptions addExcludeProp(String property) {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#removeExcludeProp(java.lang.String)
	 */
	public ExampleOptions removeExcludeProp(String property) {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setExcludeNulls(boolean)
	 */
	public ExampleOptions setExcludeNulls(boolean exclude) {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getExcludeNulls()
	 */
	public boolean getExcludeNulls() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setExcludeZeros(boolean)
	 */
	public ExampleOptions setExcludeZeros(boolean exclude) {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getExcludeZeros()
	 */
	public boolean getExcludeZeros() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setIgnoreCase(boolean)
	 */
//	public ExampleOptions setIgnoreCase(boolean ignore) {
//		return this;
//	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getIgnoreCase()
	 */
//	public boolean getIgnoreCase() {
//		return false;
//	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#setLikeMode(com.centimia.orm.jaqu.LikeMode)
	 */
	public ExampleOptions setLikeMode(LikeMode mode) {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ExampleOptions#getLikeMode()
	 */
	public LikeMode getLikeMode() {
		return LikeMode.EXACT;
	}

}
