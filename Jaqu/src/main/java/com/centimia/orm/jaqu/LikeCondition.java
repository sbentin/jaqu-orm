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

import com.centimia.orm.jaqu.annotation.Entity;

/**
 * Special to deal with cases where the user wants to explicitly say which kind of like he needs without stating it himself on the input.
 * @author shai
 */
class LikeCondition<A> implements Token {

	A x,y;
	LikeMode	mode;
	
	public LikeCondition(A x, A y, LikeMode mode) {
		this.x = x;
		this.y = y;
		this.mode = mode;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.Token#appendSQL(com.centimia.orm.jaqu.SQLStatement, com.centimia.orm.jaqu.Query)
	 */
	@SuppressWarnings("resource")
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, x, false, null);
		stat.appendSQL(" LIKE ");
        // check if a relation type
        if (y != null && y.getClass().getAnnotation(Entity.class) != null)
        	query.appendSQL(stat, query.getDb().factory.getPrimaryKey(y), false, null);
        else {
        	switch(mode) {
        		case ANYWHERE: stat.appendSQL("'%"); stat.appendSQL((String)y); stat.appendSQL("%'"); break;
        		case END: stat.appendSQL("'" + (String)y); stat.appendSQL("%'"); break;
        		case START: stat.appendSQL("'%"); stat.appendSQL((String)y + "'"); break;
        		case EXACT: query.appendSQL(stat, y, false, null); break;
        	}
        }
	}
}
