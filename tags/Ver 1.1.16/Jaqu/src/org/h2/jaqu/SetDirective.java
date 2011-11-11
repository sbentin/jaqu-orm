/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 29/01/2010		Shai Bentin			 create
 */
package org.h2.jaqu;

import org.h2.jaqu.annotation.Entity;

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
	 * @see org.h2.jaqu.Token#appendSQL(org.h2.jaqu.SQLStatement, org.h2.jaqu.Query)
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
