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
Created		   Aug 3, 2013			shai

*/
package com.centimia.orm.jaqu;

/**
 * @author shai
 *
 */
class UnificationToken implements Token {

	enum UNIFICATION_MODE {UNION, INTERESCT};
	
	private final UNIFICATION_MODE mode;
	private final String queryString;
	
	public UnificationToken(String queryString, UNIFICATION_MODE mode) {
		this.queryString = queryString;
		this.mode = mode;		
	}
	
	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.Token#appendSQL(com.centimia.orm.jaqu.SQLStatement, com.centimia.orm.jaqu.Query)
	 */
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		if (null != queryString && queryString.length() > 0) {
			stat.appendSQL(" ");
			stat.appendSQL(mode.name());
			stat.appendSQL(" ");
			stat.appendSQL(queryString);
		}
	}
}
