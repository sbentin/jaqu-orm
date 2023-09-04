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

/*
 * ISSUE 		DATE 			AUTHOR
 * ------- 		------ 			--------
 * Created 		Jul 7, 2013 	shai
 */
package com.centimia.orm.jaqu;

/**
 * Used in primary key queries where the PK is a known definit.
 *
 * @param <A>
 * @param <T>
 * @param <T>
 * @author shai
 */
public class PkQueryCondition<T, A> extends QueryCondition<T, A>{

	PkQueryCondition(Query<T> query, GenericMask<T, A> x, Class<A> mask) {
		super(query, x, x.mask());
	}

	PkQueryCondition(Query<T> query, A x) {
		super(query, x);
	}

	@Override
	public QueryWhere<T> is(A y) {
		query.addConditionToken(new PkCondition<>(x, y, CompareType.EQUAL));
		return new QueryWhere<>(query);
	}

	@Override
	public QueryWhere<T> isNot(A y) {
		query.addConditionToken(new PkCondition<>(x, y, CompareType.NOT_EQUAL));
		return new QueryWhere<>(query);
	}

	@Override
	public QueryWhere<T> in(A[] y) {
		query.addConditionToken(new PkInCondition<>(x, y, CompareType.IN));
		return new QueryWhere<>(query);
	}

	@Override
	public QueryWhere<T> notIn(A[] y) {
		query.addConditionToken(new PkInCondition<>(x, y, CompareType.NOT_IN));
		return new QueryWhere<>(query);
	}

	@Override
	public QueryWhere<T> bigger(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.BIGGER));
        return new QueryWhere<>(query);
    }

    @Override
	public QueryWhere<T> biggerEqual(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.BIGGER_EQUAL));
        return new QueryWhere<>(query);
    }

    @Override
	public QueryWhere<T> smaller(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.SMALLER));
        return new QueryWhere<>(query);
    }

    @Override
	public QueryWhere<T> smallerEqual(A y) {
        query.addConditionToken(new Condition<>(x, y, CompareType.SMALLER_EQUAL));
        return new QueryWhere<>(query);
    }

    /**
     * Like allowes the 'LIKE' query. dependeing on the query string given. If '%' is used in the 'pattern' it will effect the result.
     *
     * @param pattern the pattern to check against.
     * @return QueryWhere<T>
     */
    @Override
	public QueryWhere<T> like(A pattern) {
        query.addConditionToken(new Condition<>(x, pattern, CompareType.LIKE));
        return new QueryWhere<>(query);
    }

    /**
     * Not supported for pk condition
     *
     * @param pattern
     * @param mode
     * @return QueryWhere<T>
     */
    @Override
	public QueryWhere<T> like(A pattern, LikeMode mode) {
        throw new JaquError("'Like' not supported for Pk conditions!!!");
    }

    @Override
	public QueryWhere<T> isNotNull() {
    	throw new JaquError("'isNotNull' not supported for Pk conditions!!!");
    }

    @Override
	public QueryWhere<T> isNull() {
    	throw new JaquError("'isNull' not supported for Pk conditions!!!");
    }

    @Override
	public QueryBetween<T, A> between(A y){
    	throw new JaquError("'between' not supported for Pk conditions!!!");
    }
}