/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *  
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 22/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu;

/**
 * @author shai
 *
 */
public class ConditionBetween<A> implements Token {

    A x, y, z;

	public ConditionBetween(A x, A y, A z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/* (non-Javadoc)
	 * @see org.h2.jaqu.Token#appendSQL(org.h2.jaqu.SQLStatement, org.h2.jaqu.Query)
	 */
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, x);
		stat.appendSQL(" BETWEEN (");
		query.appendSQL(stat, y);
		stat.appendSQL(" AND ");
		query.appendSQL(stat, z);
		stat.appendSQL(")");
	}

}
