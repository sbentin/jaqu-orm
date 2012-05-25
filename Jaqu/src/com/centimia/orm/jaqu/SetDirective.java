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
 * Initial Developer: H2 Group, Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 29/01/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import com.centimia.orm.jaqu.annotation.Entity;

/**
 * Use for Update Set directive in an update query.
 * 
 * @author Shai Bentin
 */
public class SetDirective<A> implements Token {

	A x, value;
	
	SetDirective(A x, A value) {
		this.x = x;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.Token#appendSQL(com.centimia.orm.jaqu.SQLStatement, com.centimia.orm.jaqu.Query)
	 */
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, x);
		stat.appendSQL(" = ");
		// for relationship support
		if (value != null && value.getClass().getAnnotation(Entity.class) != null) {
			query.getDb().merge(value);
			query.appendSQL(stat, query.getDb().factory.getPrimaryKey(value));
		}
		else
			query.appendSQL(stat, value);
	}
}
