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

import com.centimia.orm.jaqu.annotation.Entity;

/**
 * Special to deal with cases where the user wants to explicitly say which kind of like he needs without stating it himself on the input.
 * @author shai
 */
public class LikeCondition<A> implements Token {

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
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, x);
		stat.appendSQL(" ");
        stat.appendSQL("LIKE");
        stat.appendSQL(" ");
        // check if a relation type
        if (y != null && y.getClass().getAnnotation(Entity.class) != null)
        	query.appendSQL(stat, query.getDb().factory.getPrimaryKey(y));
        else {
        	switch(mode) {
        		case ANYWHERE: stat.appendSQL("%"); query.appendSQL(stat, y); stat.appendSQL("%"); break;
        		case END: query.appendSQL(stat, y); stat.appendSQL("%"); break;
        		case START: stat.appendSQL("%"); query.appendSQL(stat, y); break;
        		case EXACT: query.appendSQL(stat, y); break;
        	}
        }
	}
}
