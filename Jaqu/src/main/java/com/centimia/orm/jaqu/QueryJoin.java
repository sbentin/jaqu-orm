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
 * This class represents a query with a join.
 */
public class QueryJoin<T> {

    private Query<T> query;
    private SelectTable<T> join;

    QueryJoin(Query<T> query, SelectTable<T> join) {
        this.query = query;
        this.join = join;
    }

    public <A> QueryJoinCondition<T, A> on(A x) {
        return new QueryJoinCondition<T, A>(query, join, x);
    }
}