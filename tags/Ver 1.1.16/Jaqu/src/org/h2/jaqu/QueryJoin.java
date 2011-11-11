/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;

/**
 * This class represents a query with a join.
 */
//## Java 1.5 begin ##
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
//## Java 1.5 end ##
