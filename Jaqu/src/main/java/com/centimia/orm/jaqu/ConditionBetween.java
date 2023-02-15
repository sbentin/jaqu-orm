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
 * Update Log
 *
 *  Date			User				Comment
 * ------			-------				--------
 * 22/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

/**
 * @author shai
 *
 */
class ConditionBetween<A> implements Token {

	Object key;
    A y, z;

	@SuppressWarnings("rawtypes")
	public ConditionBetween(Object x, A y, A z) {
		if (x instanceof GenericMask) {
			this.key = ((GenericMask)x).orig();
		}
		else
			this.key = x;
		this.y = y;
		this.z = z;
	}

	/* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.Token#appendSQL(com.centimia.orm.jaqu.SQLStatement, com.centimia.orm.jaqu.Query)
	 */
	@Override
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, key, false, null);
		stat.appendSQL(" BETWEEN ");
		query.appendSQL(stat, y, false, null);
		stat.appendSQL(" AND ");
		query.appendSQL(stat, z, false, null);
	}
}
