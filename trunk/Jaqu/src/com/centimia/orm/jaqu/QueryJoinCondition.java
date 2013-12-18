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
 * This class represents a query with join and an incomplete condition.
 *
 * @param <A> the incomplete condition data type
 */
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
    
    public QueryJoinWhere<T> isNot(A y) {
        join.addConditionToken(new Condition<A>(x, y, CompareType.NOT_EQUAL));
        return new QueryJoinWhere<T>(query, join);
    } 
}