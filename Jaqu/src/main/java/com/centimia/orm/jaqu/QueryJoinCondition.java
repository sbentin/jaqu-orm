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
 * This class represents a query with join and an incomplete condition.
 *
 * @param <A> the incomplete condition data type
 */
public class QueryJoinCondition<T, A> {

    private Query<T> query;
    private SelectTable<T> join;
    private Object x;

    <K> QueryJoinCondition(Query<T> query, SelectTable<T> join, GenericMask<K, A> x) {
        this.query = query;
        this.join = join;
        this.x = x;
    }

    QueryJoinCondition(Query<T> query, SelectTable<T> join, A x) {
        this.query = query;
        this.join = join;
        this.x = x;
    }

    public QueryJoinWhere<T> is(A y) {
   		join.addConditionToken(new Condition<>(x, y, CompareType.EQUAL));
        return new QueryJoinWhere<>(query, join);
    }

    public QueryJoinWhere<T> isNot(A y) {
   		join.addConditionToken(new Condition<>(x, y, CompareType.NOT_EQUAL));
        return new QueryJoinWhere<>(query, join);
    }
    
    public QueryJoinWhere<T> bigger(A y) {
        join.addConditionToken(new Condition<>(x, y, CompareType.BIGGER));
        return new QueryJoinWhere<>(query, join);
    }

    public QueryJoinWhere<T> biggerEqual(A y) {
        join.addConditionToken(new Condition<>(x, y, CompareType.BIGGER_EQUAL));
        return new QueryJoinWhere<>(query, join);
    }

    public QueryJoinWhere<T> smaller(A y) {
        join.addConditionToken(new Condition<>(x, y, CompareType.SMALLER));
        return new QueryJoinWhere<>(query, join);
    }

    public QueryJoinWhere<T> smallerEqual(A y) {
        join.addConditionToken(new Condition<>(x, y, CompareType.SMALLER_EQUAL));
        return new QueryJoinWhere<>(query, join);
    }

    /**
     * Like allowes the 'LIKE' query. depending on the query string given. If '%' is used in the 'pattern' it will effect the result.
     *
     * @param pattern the pattern to check against.
     * @return QueryWhere<T>
     */
    public QueryJoinWhere<T> like(A pattern) {
        join.addConditionToken(new Condition<>(x, pattern, CompareType.LIKE));
        return new QueryJoinWhere<>(query, join);
    }

    /**
     * Although @{link {@link QueryJoinWhere#like(Object, SelectTable)} allows the use of '%' within the
     * pattern sometime the user will want to specify it without changing the original pattern or might
     * have no control over the pattern. This method lets the user specify the Like pattern he needs.
     *
     * @param pattern
     * @param mode
     * @return QueryWhere<T>
     */
    public QueryJoinWhere<T> like(A pattern, LikeMode mode) {
        join.addConditionToken(new LikeCondition<>(x, pattern, mode));
        return new QueryJoinWhere<>(query, join);
    }
}