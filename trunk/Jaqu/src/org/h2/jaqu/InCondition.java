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
 * 29/06/2010		shai				 create
 */
package org.h2.jaqu;

import org.h2.jaqu.util.StatementBuilder;

/**
 * 
 * @author shai
 *
 */
public class InCondition<A> implements Token {

	CompareType compareType;
    A x;
    A[] y;
    
	InCondition(A x, A[] y, CompareType compareType) {
		this.compareType = compareType;
		this.x = x;
		this.y = y;
	}

	/* (non-Javadoc)
	 * @see org.h2.jaqu.Token#appendSQL(org.h2.jaqu.SQLStatement, org.h2.jaqu.Query)
	 */
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, x);
		stat.appendSQL(" ");
        stat.appendSQL(compareType.getString());
        StatementBuilder buff = new StatementBuilder(" (");
        for (A item: y) {
        	buff.appendExceptFirst(", ");
        	if (item instanceof String) {
        		buff.append("'" + item.toString() + "'");
        	}
        	else {
        		buff.append(item.toString());
        	}
        }
        buff.append(")");
        stat.appendSQL(buff.toString());
	}

}
