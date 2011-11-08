/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;


/**
 * This class represents a query with an incomplete condition.
 *
 * @param <T> the return type of the query
 * @param <A> the incomplete condition data type
 */
//## Java 1.5 begin ##
public class QueryCondition<T, A> {

    private Query<T> query;
    private A x;

    QueryCondition(Query<T> query, A x) {
        this.query = query;
        this.x = x;
    }

    public QueryWhere<T> is(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.EQUAL));
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> in(A[] y) {
    	query.addConditionToken(new InCondition<A>(x, y, CompareType.IN));
    	return new QueryWhere<T>(query);
    }
    
    public QueryWhere<T> notIn(A[] y) {
    	query.addConditionToken(new InCondition<A>(x, y, CompareType.NOT_IN));
    	return new QueryWhere<T>(query);
    }
    
    public QueryWhere<T> bigger(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.BIGGER));
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> biggerEqual(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.BIGGER_EQUAL));
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> smaller(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.SMALLER));
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> smallerEqual(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.SMALLER_EQUAL));
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> like(A pattern) {
        query.addConditionToken(new Condition<A>(x, pattern, CompareType.LIKE));
        return new QueryWhere<T>(query);
    }
    
    public QueryWhere<T> isNotNull() {
        query.addConditionToken(new Condition<A>(x, null, CompareType.IS_NOT_NULL));
        return new QueryWhere<T>(query);
    }
    
    public QueryWhere<T> isNull() {
        query.addConditionToken(new Condition<A>(x, null, CompareType.IS_NULL));
        return new QueryWhere<T>(query);
    }
    
    public QueryBetween<T, A> between(A y){
    	// here we don't add a condition we will do it after we have all the data for both left and right of the between
    	return new QueryBetween<T, A>(query, x, y);
    }
}
//## Java 1.5 end ##
