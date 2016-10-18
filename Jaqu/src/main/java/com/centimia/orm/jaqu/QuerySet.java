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

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 29/01/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

/**
 * Represents the 'SET' coomand type within an update query.
 * 
 * @author Shai Bentin
 */
public class QuerySet<T, F> {
	private Query<T> query;
    private F x;
    private F value;
    
	QuerySet(Query<T> query, F x, F v) {
		this.query = query;
		this.x = x;
		this.value = v;
	}
	
	/*
	 * Starts a where clause for an update query. Based on the field A of the Table T
	 * @param <A>
	 * @param a
	 * @return QueryCondition<T, A>
	 */
	public <A> QueryCondition<T, A> where(A a){
		query.addUpdateToken(new SetDirective<F>(x, value));
		return query.where(a);
	}
    
	/*
	 * Starts a where clause for an update query. Based on a simple string where
	 * 
	 * @param <A>
	 * @param whereCondition
	 * @return QueryWhere<T>
	 */
	<A> QueryWhere<T> where(final StringFilter whereCondition){
    	Token conditionCode = new Token() {
			
			@SuppressWarnings("hiding")
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				stat.appendSQL(whereCondition.getConditionString(query.getSelectTable()));			
			}
		};
		query.addConditionToken(conditionCode);
		return new QueryWhere<T>(query);
    }
	
	/*
	 * Use to add more field sets in an update query.
	 * 
	 * @param <A>
	 * @param x
	 * @param v
	 * @return QuerySet<T, A>
	 */
	public <A> QuerySet<T, A> and(A x, A v){
		query.addUpdateToken(new SetDirective<F>(this.x, value));
		return new QuerySet<T, A>(query, x, v);
	}
}
