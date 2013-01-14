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
 * 29/06/2010		shai				 create
 */
package com.centimia.orm.jaqu;

import com.centimia.orm.jaqu.util.StatementBuilder;

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
	 * @see com.centimia.orm.jaqu.Token#appendSQL(com.centimia.orm.jaqu.SQLStatement, com.centimia.orm.jaqu.Query)
	 */
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, x);
		stat.appendSQL(" ");
        stat.appendSQL(compareType.getString());
        StatementBuilder buff = new StatementBuilder(" (");
        for (A item: y) {
        	buff.appendExceptFirst(", ");
        	if (item instanceof String || item.getClass().isEnum()) {
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
