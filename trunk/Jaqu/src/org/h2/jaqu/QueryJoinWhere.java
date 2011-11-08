package org.h2.jaqu;

import java.util.List;

/**
 * 
 * @author shai
 */
public class QueryJoinWhere<T> {

	Query<T> query;
	SelectTable<T> join;

	QueryJoinWhere(Query<T> query, SelectTable<T> join) {
		this.query = query;
		this.join = join;
	}

	/**
	 * Perform an 'AND' operator in the query
	 * 
	 * @param <A> - represents any field type that exists on Object <T>
	 * @param x - The field (gives field name) that should be attached with 'AND' to the query
	 * @return QueryCondition
	 */
	public <A> QueryJoinCondition<T, A> and(A x) {
		join.addConditionToken(ConditionAndOr.AND);
		return new QueryJoinCondition<T, A>(query, join, x);
	}

	/**
	 * Perform an 'OR' operator in the query
	 * 
	 * @param <A> - represents any field type that exists on Object <T>
	 * @param x - The field (gives field name) that should be attached with 'OR' to the query
	 * @return QueryCondition
	 */
	public <A> QueryJoinCondition<T, A> or(A x) {
		join.addConditionToken(ConditionAndOr.OR);
		return new QueryJoinCondition<T, A>(query, join, x);
	}

    /**
     * Opens a where clause after join.
     * 
     * @param <A>
     * @param x
     * @return QueryCondition<T, A>
     */
    public <A> QueryCondition<T, A> where(A x) {
        return new QueryCondition<T, A>(query, x);
    }
    
    /**
	 * inner Join another table. (returns only rows that match)
	 *
	 * @param alias an alias for the table to join
	 * @return the joined query
	 */
    @SuppressWarnings("unchecked")
    public <U> QueryJoin<T> innerJoin(U alias) {
        return query.innerJoin(alias);
    }
    
    /**
	 * Left Outer Join another table. (Return all rows from left table, and matching from rightHandSide)
	 *
	 * @param alias an alias for the table to join
	 * @return the joined query
	 */
    @SuppressWarnings("unchecked")
    public <U> QueryJoin<T> leftOuterJoin(U alias) {
       return query.leftOuterJoin(alias);
    }
    
	/**
	 * Perform the built query Select.
	 * 
	 * @return List<T> can be a list of one, many or empty, never 'null'. Can be used in a primary key select, but using
	 *         {@link #selectFirst} is advised.
	 */
	public List<T> select() {
		return query.select();
	}

	/**
	 * Select the first result and return it. Should also be used in a primary key select.
	 * 
	 * @return T result or null if there is no result
	 */
	public T selectFirst() {
		List<T> list = select();
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * Select only distinct results in the table
	 * 
	 * @return List<T>
	 */
	public List<T> selectDistinct() {
		return query.selectDistinct();
	}
	
	/**
	 * Performs the select of the query. Returns the results or an empty list. Does not return null. This select returns a List of a given
	 * object that mapping is given to from the result set to the field in that object
	 * 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return List<X>
	 */
	public <X, Z> List<X> select(Z x) {
		return query.select(x);
	}

	/**
	 * Returns the SQL String to be performed. Use for Debug.
	 * 
	 * @return String
	 */
	public String getSQL() {
		SQLStatement selectList = new SQLStatement(query.getDb());
		selectList.appendSQL("*");
		return query.prepare(selectList, false).getSQL().trim();
	}

	/**
	 * Performs A select similar to {@link #select(Object)} but with the 'DISTINCT' directive. Returns results or empty List. Never 'null'
	 * 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return List<X>
	 */
	public <X, Z> List<X> selectDistinct(Z x) {
		return query.selectDistinct(x);
	}

	/**
	 * Returns the 'Z' type object from the first result
	 * 
	 * @param <X>
	 * @param <Z>
	 * @param x
	 * @return a result or null if there is no result
	 */
	public <X, Z> X selectFirst(Z x) {
		List<X> list = query.select(x);
		return list.isEmpty() ? null : list.get(0);
	}

	/**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
	 * 
	 * @param <U>
	 * @param table - the object representing the other side
	 * @return List<U>
	 */
	public <U> List<U> selectRightHandJoin(U table) {
		return query.selectRightHandJoin(table);
	}

	/**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
	 * 
	 * @param <U>
	 * @param table
	 * @return U single object table of the right hand side.
	 */
	public <U> U selectFirstRightHandJoin(U table) {
		return query.selectFirstRightHandJoin(table);
	}

	/**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
	 * @param <U>
	 * @param table
	 * @return List<U>
	 */
	public <U> List<U> selectDistinctRightHandJoin(U table) {
		return query.selectDistinctRightHandJoin(table);
	}

	/**
	 * Group By ordered objects
	 * 
	 * @param groupBy
	 * @return Query<T>
	 */
	public QueryJoinWhere<T> groupBy(Object... groupBy) {
		this.query = query.groupBy(groupBy);
		return this;
	}

	/**
	 * Order by one or more columns in ascending order.
	 * 
	 * @param expressions the order by expressions
	 * @return QueryWhere - the query
	 */
	public QueryJoinWhere<T> orderBy(Object... expressions) {
		for (Object expr : expressions) {
			OrderExpression<T> e = new OrderExpression<T>(query, expr, false, false, false);
			query.addOrderBy(e);
		}
		return this;
	}

	/**
	 * Order by one or more columns in descending order
	 * 
	 * @param expr
	 * @return QueryWhere<T> - the query
	 */
	public QueryJoinWhere<T> orderByNullsFirst(Object... expr) {
		int length = expr.length;
		switch (length) {
			case 0:
				return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, true, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, true, false);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by one or more columns in ascending order
	 * 
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryJoinWhere<T> orderByNullsLast(Object... expr) {
		int length = expr.length;
		switch (length) {
			case 0:
				return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, false, true);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], false, false, true);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by in descending order
	 * 
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryJoinWhere<T> orderByDesc(Object... expr) {
		int length = expr.length;
		switch (length) {
			case 0:
				return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, false, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by in descending order nulls will be first
	 * 
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryJoinWhere<T> orderByDescNullsFirst(Object... expr) {
		int length = expr.length;
		switch (length) {
			case 0:
				return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, true, false);
				query.addOrderBy(e);
				return this;
			}
		}
	}

	/**
	 * Order by in descending order nulls will be last
	 * 
	 * @param expr
	 * @return QueryWhere<T>
	 */
	public QueryJoinWhere<T> orderByDescNullsLast(Object... expr) {
		int length = expr.length;
		switch (length) {
			case 0:
				return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, false, true);
				query.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<T>(query, expr[i], false, false, false);
					query.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<T>(query, expr[length - 1], true, false, true);
				query.addOrderBy(e);
				return this;
			}
		}
	}
}
