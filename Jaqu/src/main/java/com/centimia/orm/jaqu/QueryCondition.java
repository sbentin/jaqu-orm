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
    protected Object x;

	QueryCondition(Query<T> query, GenericMask<?, A> x, Class<A> maskedType) {
        this.query = query;
        this.x = x;
    }

    QueryCondition(Query<T> query, A x) {
        this.query = query;
        this.x = x;
    }

    public QueryWhere<T> is(A y) {
   		query.addConditionToken(new Condition<>(x, y, CompareType.EQUAL));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> isNot(A y) {
    	query.addConditionToken(new Condition<>(x, y, CompareType.NOT_EQUAL));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> in(A[] y) {
    	query.addConditionToken(new InCondition<>(x, y, CompareType.IN));
    	return new QueryWhere<>(query);
    }

    public QueryWhere<T> notIn(A[] y) {
    	query.addConditionToken(new InCondition<>(x, y, CompareType.NOT_IN));
    	return new QueryWhere<>(query);
    }

    public QueryWhere<T> bigger(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.BIGGER));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> biggerEqual(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.BIGGER_EQUAL));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> smaller(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.SMALLER));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> smallerEqual(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.SMALLER_EQUAL));
        return new QueryWhere<>(query);
    }

    /**
     * Like allows the 'LIKE' query. depending on the query string given. If '%' is used in the 'pattern' it will effect the result.
     *
     * @param pattern the pattern to check against.
     * @return QueryWhere<T>
     */
    public QueryWhere<T> like(A pattern) {
        query.addConditionToken(new Condition<>(x, pattern, CompareType.LIKE));
        return new QueryWhere<>(query);
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
        query.addConditionToken(new LikeCondition<>(x, pattern, mode));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> isNotNull() {
        query.addConditionToken(new Condition<A>(x, null, CompareType.IS_NOT_NULL));
        return new QueryWhere<>(query);
    }

    public QueryWhere<T> isNull() {
        query.addConditionToken(new Condition<A>(x, null, CompareType.IS_NULL));
        return new QueryWhere<>(query);
    }

    @SuppressWarnings("unchecked")
	public QueryBetween<T, A> between(A y){
    	// here we don't add a condition we will do it after we have all the data for both left and right of the between
    	if (x instanceof GenericMask)
    		return new QueryBetween<>(query, (GenericMask<T, A>)x, y);
    	return new QueryBetween<>(query, (A)x, y);
    }
    
    /**
     * Opens a '(' parenthesis
     * @param &lt;A&gt;
     * @return QueryCondition&lt;T, A&gt;
     */
    @SuppressWarnings({ "unchecked", "hiding" })
	public <A> QueryCondition<T, A> wrap() {
    	query.addConditionToken(new Token() {
			@Override
			public <K> void appendSQL(SQLStatement stat, Query<K> query) {
				stat.appendSQL("(");
			}
		});
    	return new QueryCondition<>(this.query, (A)this.x);
    }
}