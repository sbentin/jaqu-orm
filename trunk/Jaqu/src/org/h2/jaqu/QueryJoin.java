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
public class QueryJoin {

    private QueryInterface<?> query;
    private SelectTable< ? > join;

    QueryJoin(QueryInterface<?> query, SelectTable< ? > join) {
        this.query = query;
        this.join = join;
    }

    public <A> QueryJoinCondition<A> on(A x) {
        return new QueryJoinCondition<A>(query, join, x);
    }
}
//## Java 1.5 end ##
