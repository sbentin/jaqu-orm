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
 * This class represents a query with an incomplete condition.
 *
 * @param <T> the return type of the query
 * @param <A> the incomplete condition data type
 */
public class QueryCondition<T, A> {

    protected Query<T> query;
    protected A x;

    QueryCondition(Query<T> query, A x) {
        this.query = query;
        this.x = x;
    }

    public QueryWhere<T> is(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.EQUAL));
        return new QueryWhere<T>(query);
    }
    
    public QueryWhere<T> isNot(A y) {
        query.addConditionToken(new Condition<A>(x, y, CompareType.NOT_EQUAL));
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

    /**
     * Like allowes the 'LIKE' query. dependeing on the query string given. If '%' is used in the 'pattern' it will effect the result.
     * 
     * @param pattern the pattern to check against.
     * @return QueryWhere<T>
     */
    public QueryWhere<T> like(A pattern) {
        query.addConditionToken(new Condition<A>(x, pattern, CompareType.LIKE));
        return new QueryWhere<T>(query);
    }
    
    /**
     * Although @{link {@link QueryCondition#like(Object)} allows the use of '%' within the 
     * pattern sometime the user will want to specify it without changing the original pattern or might 
     * have no control over the pattern. This method lets the user specify the Like pattern he needs.
     * 
     * @param pattern
     * @param mode
     * @return QueryWhere<T>
     */
    public QueryWhere<T> like(A pattern, LikeMode mode) {
        query.addConditionToken(new LikeCondition<A>(x, pattern, mode));
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