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
package com.centimia.orm.jaqu;

/**
 * Classes implementing this interface can be used as a token in a statement.
 */
interface Token {
    /**
     * Append the SQL to the given statement using the given query.
     *
     * @param stat the statement to append the SQL to
     * @param query the query to use
     */
    <T> void appendSQL(SQLStatement stat, Query<T> query);
}
