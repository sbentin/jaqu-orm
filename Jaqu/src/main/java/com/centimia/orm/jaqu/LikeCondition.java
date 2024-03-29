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
import com.centimia.orm.jaqu.annotation.MappedSuperclass;

/**
 * Special to deal with cases where the user wants to explicitly say which kind of like he needs without stating it himself on the input.
 * @author shai
 */
class LikeCondition<A> implements Token {

	A y;
	Object key;
	LikeMode	mode;

	@SuppressWarnings("rawtypes")
	public LikeCondition(Object x, A y, LikeMode mode) {
		if (x instanceof GenericMask) {
        	this.key = ((GenericMask)x).orig();
        }
        else {
        	this.key = x;
        }
		this.y = y;
		this.mode = mode;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.Token#appendSQL(com.centimia.orm.jaqu.SQLStatement, com.centimia.orm.jaqu.Query)
	 */
	@Override
	@SuppressWarnings("resource")
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		if (null != key && (null != key.getClass().getAnnotation(Entity.class) || null != key.getClass().getAnnotation(MappedSuperclass.class))) {
			Object pk = query.getDb().factory.getPrimaryKey(key);
			if (null == pk)
				query.appendSQL(stat, key, false, null);
			else
				query.appendSQL(stat, pk, false, null);
		}
		else
			query.appendSQL(stat, key, false, null);
		stat.appendSQL(" LIKE ");
        // check if a relation type
        if (y != null && (null != y.getClass().getAnnotation(Entity.class) || null != y.getClass().getAnnotation(MappedSuperclass.class))) {
        	Object pk = query.getDb().factory.getPrimaryKey(y);
	    	if (null == pk)
	    		query.appendSQL(stat, y, false, null);
	    	else
	    		query.appendSQL(stat, query.getDb().factory.getPrimaryKey(y), false, null);
        }
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
