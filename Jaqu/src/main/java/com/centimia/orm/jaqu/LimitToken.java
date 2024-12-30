/*
 * Copyright (c) 2008-2012 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.orm.jaqu;

/**
 * @author shai
 *
 */
public class LimitToken implements Token {

	private final int limit;

	LimitToken(int limitNum) {
		this.limit = limitNum;
	}

	@SuppressWarnings("resource")
	@Override
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {		
		if (Dialect.ORACLE == query.getDb().factory.getDialect())
			stat.appendSQL("FETCH FIRST " + limit + " ROWS ONLY");
		else
			stat.appendSQL("limit " + limit);
	}

}
