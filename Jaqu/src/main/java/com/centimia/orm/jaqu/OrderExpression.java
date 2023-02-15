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
package com.centimia.orm.jaqu;

/**
 * An expression to order by in a query.
 *
 * @param <T> the query data type
 */
class OrderExpression<T> {
    private Query<T> query;
    private Object expression;
    private boolean desc;
    private boolean nullsFirst;
    private boolean nullsLast;

    OrderExpression(Query<T> query, Object expression, boolean desc, boolean nullsFirst, boolean nullsLast) {
        this.query = query;
        this.expression = expression;
        this.desc = desc;
        this.nullsFirst = nullsFirst;
        this.nullsLast = nullsLast;
    }

	@SuppressWarnings("resource")
	void appendSQL(SQLStatement stat) {
		if (1 >= query.getDb().factory.dialect.ordinal()) {
			query.appendSQL(stat, expression, false, null);
			// H2, ORACLE dialects support this
			if (desc) {
				stat.appendSQL(" DESC");
				// default null ordering in descending is null last so we only need to check null first
				if (nullsFirst) {
					stat.appendSQL(" NULLS FIRST");
				}
			}
			else {
				stat.appendSQL(" ASC");
				// default null ordering in ascending is null first so we only need to check null last
				if (nullsLast) {
					stat.appendSQL(" NULLS LAST");
				}
			}
		}
		else {
			// the others will support something a bit more complex. (CASE WHEN ate.user_id IS NULL THEN 0 ELSE 1 END)
			if (desc) {
				if (nullsFirst)
					stat.appendSQL("(CASE WHEN " + expression + " IS NULL THEN 0 ELSE 1 END), ");

				query.appendSQL(stat, expression, false, null);
				stat.appendSQL(" DESC");
			}
			else {
				if (nullsLast)
					stat.appendSQL("(CASE WHEN " + expression + " IS NULL THEN 0 ELSE 1 END) DESC, ");

				query.appendSQL(stat, expression, false, null);
				stat.appendSQL(" ASC");
			}
		}
	}
}