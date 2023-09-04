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
 * An OR or an AND condition.
 */
enum ConditionAndOr implements Token {
    AND("AND"),
    OR("OR");

    private String text;

    ConditionAndOr(String text) {
        this.text = text;
    }

    @Override
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        stat.appendSQL(text);
    }

}