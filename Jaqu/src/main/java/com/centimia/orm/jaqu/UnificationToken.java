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
