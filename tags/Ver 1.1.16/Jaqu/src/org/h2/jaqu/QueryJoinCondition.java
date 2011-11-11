/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;

/**
 * This class represents a query with join and an incomplete condition.
 *
 * @param <A> the incomplete condition data type
 */
//## Java 1.5 begin ##
public class QueryJoinCondition<T, A> {

    private Query<T> query;
    private SelectTable<T> join;
    private A x;

    QueryJoinCondition(Query<T> query, SelectTable<T> join, A x) {
        this.query = query;
        this.join = join;
        this.x = x;
    }

    public QueryJoinWhere<T> is(A y) {
        join.addConditionToken(new Condition<A>(x, y, CompareType.EQUAL));
        return new QueryJoinWhere<T>(query, join);
    }
}
//## Java 1.5 end ##
