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

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.centimia.orm.jaqu.ISelectTable.JOIN_TYPE;
import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.util.Utils;

/**
 * This class represents a query.
 *
 * @param <T> the return type
 */
public class Query<T> implements QueryInterface<T> {

    private static final String TO_GET_A_SUBSET = "To get a subset of the fields or a mix of the fields using mapping use the"
    		+ " 'select(Z)' or 'selectFirst(Z)' or 'selectDistinct(Z)' methods";
	private static final String ILLEGAL_STATE_0 = "IllegalState 0 Entity based on %s must be part of a join query!!";
	
	private Db db;
    private SelectTable<T> from;
    private ArrayList<Token> conditions = Utils.newArrayList();
    private ArrayList<Token> setTokens = Utils.newArrayList();
    private ArrayList<SelectTable< ? >> joins = Utils.newArrayList();
    private final IdentityHashMap<Object, SelectColumn<T>> aliasMap = Utils.newIdentityHashMap();
    private ArrayList<OrderExpression<T>> orderByList = Utils.newArrayList();
    private Object[] groupByExpressions;

    Query(Db db) {
        this.db = db;
    }

    @SuppressWarnings("unchecked")
    static <T> Query<T> from(Db db, T alias) {
        Query<T> query = new Query<>(db);
        TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
        query.from = new SelectTable<>(query, alias, JOIN_TYPE.NONE);
        def.initSelectObject(query.from, alias, query.aliasMap);
        return query;
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#selectCount()
	 */
    @Override
    public long selectCount() {
        SQLStatement selectList = new SQLStatement(db);
        selectList.setSQL("COUNT(*)");
        return prepare(selectList, false).executeQuery(rs -> {
        	rs.next();
            return rs.getLong(1);
        });
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#union(java.lang.String)
     */
    @Override
	public <U> List<T> union(Query<U> unionQuery) {
		return union(unionQuery, false);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#unionDistinct(com.centimia.orm.jaqu.Query)
	 */
	@Override
	public <U> List<T> unionDistinct(Query<U> unionQuery) {
		return union(unionQuery, true);
    }

	@Override
	public <U, X> List<X> union(Query<U> unionQuery, X x) {
		return union(unionQuery, x, false);
    }

	@Override
	public <U, X> List<X> unionDistinct(Query<U> unionQuery, X x) {
		return union(unionQuery, x, true);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#union(com.centimia.orm.jaqu.QueryWhere)
	 */
	@Override
	public <U> List<T> union(QueryWhere<U> unionQuery) {
		return union(unionQuery.query, false);
	}

	@Override
	public <U> List<T> unionDistinct(QueryWhere<U> unionQuery) {
		return union(unionQuery.query, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U, X, Z> List<X> union(QueryWhere<U> unionQuery, Z x) {
		return union(unionQuery.query, (X)x, false);
    }

	@Override
	public <U, X> List<X> unionDistinct(QueryWhere<U> unionQuery, X x) {
		return union(unionQuery.query, x, true);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#union(com.centimia.orm.jaqu.QueryJoinWhere)
	 */
	@Override
	public <U> List<T> union(QueryJoinWhere<U> unionQuery) {
		return union(unionQuery.query, false);
	}

	@Override
	public <U> List<T> unionDistinct(QueryJoinWhere<U> unionQuery) {
		return union(unionQuery.query, true);
	}

	@Override
	public <U, X> List<X> union(QueryJoinWhere<U> unionQuery, X x) {
		return union(unionQuery.query, x, false);
    }

	@Override
	public <U, X> List<X> unionDistinct(QueryJoinWhere<U> unionQuery, X x) {
		return union(unionQuery.query, x, true);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#selectAsMap(java.lang.Object, java.lang.Object)
	 */
	@Override
	public <K, V> Map<K, V> selectAsMap(K key, V value){
		return selectSimpleAsMap(key, value, false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#selectDistinctAsMap(java.lang.Object, java.lang.Object)
	 */
	@Override
	public <K, V> Map<K, V> selectDistinctAsMap(K key, V value){
		return selectSimpleAsMap(key, value, true);
	}

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#select()
	 */
	@Override
    public List<T> select() {
        return select(false);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#selectFirst()
	 */
    @Override
    public T selectFirst() {
    	List<T> list = select(false);
        return list.isEmpty() ? list.get(0) : null;
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#selectDistinct()
	 */
    @Override
    public List<T> selectDistinct() {
        return select(true);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#selectFirst(Z)
	 *
	 * Reason for using X and Z generic parameters as opposed to just Z is because externally when using a special Object mapping the instance created is actually a new
	 * anonymous class which is identical to Z but is actually not Z by signature. So using the Casting to X we allow generic strong typing for the user.
	 */
    @SuppressWarnings("unchecked")
    @Override
    public <X, Z> X selectFirst(Z x) {
        List<X> list = (List<X>) select(x);
        return list.isEmpty() ? null : list.get(0);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#getSQL()
	 */
    @Override
    public String getSQL() {
    	return this.getSQL(false);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#getDistinctSQL()
     */
    @Override
	public String getDistinctSQL() {
    	return this.getSQL(true);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#getSQL(java.lang.Object)
	 */
	@Override
	public <Z> String getSQL(Z z) {
    	return getSQL(z, false);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#getDistinctSQL(java.lang.Object)
     */
	@Override
	public <Z> String getDistinctSQL(Z z) {
    	return getSQL(z, false);
    }

	/*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#selectRightHandJoin(java.lang.Object)
     */
	@Override
	public <U> List<U> selectRightHandJoin(U tableClass){
    	return selectRightHandJoin(tableClass, false);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#selectRightHandJoin(java.lang.Object, java.lang.Object)
	 */
	@Override
    public <U, Z> List<Z> selectRightHandJoin(U tableClass, Z x){
    	return selectRightHandJoin(tableClass, false, x);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.FullQueryInterface#selectDistinctRightHandJoin(java.lang.Object)
     */
    @Override
	public <U> List<U> selectDistinctRightHandJoin(U tableClass){
    	return selectRightHandJoin(tableClass, true, null);
    }

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#selectDistinctRightHandJoin(java.lang.Object, java.lang.Object)
	 */
	@Override
	public <U, Z> List<Z> selectDistinctRightHandJoin(U tableClass, Z x){
    	return selectRightHandJoin(tableClass, true, x);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#selectFirstRightHandJoin(java.lang.Object)
     */
	@Override
	public <U> U selectFirstRightHandJoin(U tableClass){
    	List<U> list = selectRightHandJoin(tableClass, false);
    	return list.isEmpty() ? null : list.get(0);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#selectFirstRightHandJoin(java.lang.Object, java.lang.Object)
     */
	@Override
    public <U, Z> Z selectFirstRightHandJoin(U tableClass, Z x){
    	List<Z> list = selectRightHandJoin(tableClass, false, x);
    	return list.isEmpty() ? null : list.get(0);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#selectDistinct(Z)
	 *
	 * Reason for using X and Z generic parameters as opposed to just Z is because externally when using a special Object mapping the instance created is actually a new
	 * anonymous class which is identical to Z but is actually not Z by signature. So using the Casting to X we allow generic strong typing for the user.
 	 */
    @SuppressWarnings("unchecked")
    @Override
	public <X, Z> List<X> selectDistinct(Z x) {
        return select((X)x, true);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#select(Z)
	 *
	 * Reason for using X and Z generic parameters as opposed to just Z is because externally when using a special Object mapping the instance created is actually a new
	 * anonymous class which is identical to Z but is actually not Z by signature. So using the Casting to X we allow generic strong typing for the user.
	 */
    @SuppressWarnings("unchecked")
    @Override
	public <X, Z> List<X> select(Z x) {
        return select((X)x, false);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#delete()
	 */
    @Override
    public int delete() {
    	try {
			TableDefinition<T> def = from.getAliasDefinition();
			SQLStatement stat = new SQLStatement(db);
			if (def.isAggregateParent) {
				// before we delete we must take care of relationships
				stat.appendSQL("SELECT * FROM ");
				from.appendSQL(stat);
				appendWhere(stat);
				if (db.factory.isShowSQL())
					StatementLogger.select(stat.logSQL());

				stat.executeQuery(rs -> {
					while (rs.next()) {
						T item = from.getAliasDefinition().readRow(rs, db);
						for (FieldDefinition fdef : def.getFields()) {
							if (fdef.fieldType.isCollectionRelation()) {
								// this is a relation
								db.deleteParentRelation(fdef, item); // item has relations so it must be a Entity type
							}
						}
						db.multiCallCache.removeReEntrent(item);
					}
					return null;
				});
			}
			stat = new SQLStatement(db);
			// Nasty hack for MYSQL
			if (def.dialect == Dialect.MYSQL)
				stat.appendSQL("DELETE " + from.getAs() + " FROM ");
			else
				stat.appendSQL("DELETE FROM ");
			from.appendSQL(stat);
			appendWhere(stat);
			if (db.factory.isShowSQL())
				StatementLogger.delete(stat.logSQL());
			return stat.executeUpdate();
		}
		finally {
			db.multiCallCache.clearReEntrent();
		}
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#update()
	 */
    @Override
    public int update() {
        try {
			SQLStatement stat = new SQLStatement(db);
			stat.appendSQL("UPDATE ");
			from.appendSQL(stat);
			appendUpdate(stat);
			appendWhere(stat);
			if (stat.getSQL().indexOf("SET") == -1)
				throw new JaquError("IllegalState - To perform update use the set directive after from...!!!");
			if (db.factory.isShowSQL())
				StatementLogger.update(stat.logSQL());
			return stat.executeUpdate();
		}
		finally {
			db.multiCallCache.clearReEntrent();
		}
    }

    /**
	 * wraps everything following with "("<br>
	 * <b>Must follow with matching "endWrap</b>
	 *
	 * @return Query&lt;T&gt;
	 */
    @Override
    public Query<T> wrap() {
    	this.addConditionToken(new Token() {
			@Override
			public <K> void appendSQL(SQLStatement stat, Query<K> query) {
				stat.appendSQL("(");
			}
		});
    	return this;
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#where(com.centimia.orm.jaqu.StringFilter)
	 */
    @Override
	public QueryWhere<T> where(final StringFilter whereCondition){
    	Token conditionCode = new Token() {

			@Override
			@SuppressWarnings("hiding")
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				stat.appendSQL(whereCondition.getConditionString(query.from));
			}
		};
		conditions.add(conditionCode);
		return new QueryWhere<>(this);
    }

    @Override
	public <K, A> QueryCondition<T, A> where(final GenericMask<K, A> mask) {
        return new QueryCondition<>(this, mask, mask.mask());
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#where(A)
	 */
	@Override
    public <A> QueryCondition<T, A> where(A x) {
        return new QueryCondition<>(this, x);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#set(A, A)
	 */
	@Override
    public <A> QuerySet<T, A> set(A x, A v) {
    	if (Collection.class.isAssignableFrom(x.getClass())) {
    		// this is a relation updating is not supported like this
    		throw new JaquError("IllegalState - To update relations use db.update(Object)");
    	}
    	return new QuerySet<>(this, x, v);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#primaryKey(java.lang.Object)
	 */
	@Override
    public QueryCondition<T, Object> primaryKey() {
    	TableDefinition<?> def = from.getAliasDefinition();

    	// def will not be null here
    	List<TableDefinition.FieldDefinition> primaryKeys = def.getPrimaryKeyFields();
    	if (primaryKeys == null || primaryKeys.isEmpty()) {
            throw new JaquError("IllegalState - No primary key columns defined for table %s - no update possible", def.tableName);
        }
    	if (primaryKeys.size() > 1)
    		throw new JaquError("UnsupportedOperation - Entity relationship is not supported for complex primary keys. Found in %s", def.tableName);
    	for (TableDefinition.FieldDefinition field: primaryKeys) {
    		return new PkQueryCondition<>(this, field.getValue(from.getAlias()));
    	}
		return null;
	}

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#whereTrue(java.lang.Boolean)
	 */
	@Override
    public QueryWhere<T> whereTrue(Boolean condition) {
        Token token = new Function("", condition);
        addConditionToken(token);
        return new QueryWhere<>(this);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.QueryInterface#orderBy(java.lang.Object[])
     */
	@Override
	public QueryInterface<T> orderBy(Object ... expressions) {
		for (Object expr : expressions) {
			OrderExpression<T> e = new OrderExpression<>(this, expr, false, false, false);
			this.addOrderBy(e);
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#orderByNullsFirst(java.lang.Object[])
	 */
	@Override
	public QueryInterface<T> orderByNullsFirst(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], false, true, false);
				this.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(this, expr[i], false, false, false);
					this.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], false, true, false);
				this.addOrderBy(e);
				return this;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#orderByNullsLast(java.lang.Object[])
	 */
	@Override
	public QueryInterface<T> orderByNullsLast(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], false, false, true);
				this.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(this, expr[i], false, false, false);
					this.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], false, false, true);
				this.addOrderBy(e);
				return this;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#orderByDesc(java.lang.Object[])
	 */
	@Override
	public QueryInterface<T> orderByDesc(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], true, false, false);
				this.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(this, expr[i], false, false, false);
					this.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], true, true, false);
				this.addOrderBy(e);
				return this;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#orderByDescNullsFirst(java.lang.Object[])
	 */
	@Override
	public QueryInterface<T> orderByDescNullsFirst(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], true, true, false);
				this.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(this, expr[i], false, false, false);
					this.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], true, true, false);
				this.addOrderBy(e);
				return this;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#orderByDescNullsLast(java.lang.Object[])
	 */
	@Override
	public QueryInterface<T> orderByDescNullsLast(Object ... expr) {
		int length = expr.length;
		switch (length) {
			case 0: return this;
			case 1: {
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], true, false, true);
				this.addOrderBy(e);
				return this;
			}
			default: {
				for (int i = 0; i < length - 1; i++) {
					OrderExpression<T> e = new OrderExpression<>(this, expr[i], false, false, false);
					this.addOrderBy(e);
				}
				OrderExpression<T> e = new OrderExpression<>(this, expr[length - 1], true, false, true);
				this.addOrderBy(e);
				return this;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#having(java.lang.Object)
	 */
	@Override
	public <A> QueryCondition<T, A> having(final A x) {
		HavingToken conditionCode = new HavingToken();
		conditions.add(conditionCode);
		return new QueryCondition<>(this, x);
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#having(com.centimia.orm.jaqu.HavingFunctions, java.lang.Object)
	 */
	@Override
	public <A> QueryCondition<T, Long> having(HavingFunctions function, final A x) {
		HavingToken conditionCode = new HavingToken();
		conditions.add(conditionCode);
		conditions.add(new Function(function.name(), x));
		return new QueryCondition<>(this, Function.ignore());
	}

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#groupBy(java.lang.Object)
	 */
	@Override
    public Query<T> groupBy(Object ... groupBy) {
        this.groupByExpressions = groupBy;
        return this;
    }

    /*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.QueryInterface#limit(java.lang.Object)
	 */
    @Override
	public Query<T> limit(int limitNum) {
		LimitToken conditionCode = new LimitToken(limitNum);
		conditions.add(conditionCode);
		return this;
	}

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#innerJoin(U)
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <U> QueryJoin innerJoin(U alias) {
        TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
        SelectTable<T> join = new SelectTable(this, alias, JOIN_TYPE.INNER_JOIN);
        def.initSelectObject(join, alias, aliasMap);
        joins.add(join);
        return new QueryJoin(this, join);
    }

    /* (non-Javadoc)
     * @see com.centimia.orm.jaqu.FullQueryInterface#leftOuterJoin(U)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <U> QueryJoin leftOuterJoin(U alias) {
        TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
        SelectTable<T> join = new SelectTable(this, alias, JOIN_TYPE.LEFT_OUTER_JOIN);
        def.initSelectObject(join, alias, aliasMap);
        joins.add(join);
        return new QueryJoin(this, join);
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.FullQueryInterface#appendSQL(com.centimia.orm.jaqu.SQLStatement, java.lang.Object)
	 */
    void appendSQL(SQLStatement stat, Object x, boolean isEnum, Class<?> enumClass) {
    	if (x == Function.count()) {
            stat.appendSQL("COUNT(*)");
            return;
        }
        if (x == Function.ignore()) {
            return;
        }
        SelectColumn<T> col = null;
        Token token = db.getToken(x);
        if (null == token && isEnum) {
           	// try to get the token according to an enum value
        	Object enumValue = handleAsEnum(enumClass, x);
           	token = db.getToken(enumValue);
         	col = aliasMap.get(enumValue);
        }

        if (token != null) {
            token.appendSQL(stat, this);
            return;
        }

        if (null == col)
        	col = aliasMap.get(x);

        if (col != null) {
            col.appendSQL(stat, from.getAs());
            return;
        }
        stat.appendSQL("?");
        stat.addParameter(x);
    }

    void addConditionToken(Token condition) {
        conditions.add(condition);
    }

    void addUpdateToken(Token setToken) {
    	setTokens.add(setToken);
    }

    void appendWhere(SQLStatement stat) {
    	if (!conditions.isEmpty()) {
            if (!(conditions.get(0) instanceof HavingToken))
            	// if the first token is not a "having" sql clause then we print WHERE
            	stat.appendSQL(" WHERE ");
            for (Token token : conditions) {
                token.appendSQL(stat, this);
                stat.appendSQL(" ");
            }

            // add the discriminator if 'T' is a part of an inheritance tree.
            if (InheritedType.DISCRIMINATOR == from.getAliasDefinition().inheritedType) {
            	stat.appendSQL(" AND " + from.getAs() + "." + from.getAliasDefinition().discriminatorColumn + "='" + from.getAliasDefinition().discriminatorValue + "' ");
            }
        }
    	else {
    		// add the discriminator if 'T' is a part of an inheritance tree.
            if (InheritedType.DISCRIMINATOR == from.getAliasDefinition().inheritedType) {
            	stat.appendSQL(" WHERE " + from.getAs() + "." + from.getAliasDefinition().discriminatorColumn + "='" + from.getAliasDefinition().discriminatorValue + "' ");
            }
    	}
    }

    void appendUpdate(SQLStatement stat) {
        if (!setTokens.isEmpty()) {
            stat.appendSQL(" SET ");
            boolean first = true;
            for (Token token : setTokens) {
            	if (!first)
            		stat.appendSQL(", ");
            	first = false;
                token.appendSQL(stat, this);
                stat.appendSQL(" ");
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    SQLStatement prepare(SQLStatement selectList, boolean distinct) {
        SQLStatement stat = selectList;
        String selectSQL = stat.getSQL();
        stat.setSQL("");
        stat.appendSQL("SELECT ");
        if (distinct) {
            stat.appendSQL("DISTINCT ");
        }
        stat.appendSQL(selectSQL);
        stat.appendSQL(" FROM ");
        from.appendSQL(stat);
        for (SelectTable join : joins) {
            join.appendSQLAsJoin(stat, this);
        }
        appendWhere(stat);
        if (groupByExpressions != null) {
            int havingIdx = stat.getSQL().indexOf("having");
            String havingQuery = null;
            if (havingIdx != -1) {
            	// we need to insert the group by before the having.
            	String currentQuery = stat.getSQL().substring(0, havingIdx);
            	havingQuery = stat.getSQL().substring(havingIdx);
            	stat.setSQL(currentQuery);
            }
        	stat.appendSQL(" GROUP BY ");
            int i = 0;
            for (Object obj : groupByExpressions) {
                if (i++ > 0) {
                    stat.appendSQL(", ");
                }
                appendSQL(stat, obj, obj.getClass().isEnum(), obj.getClass());
                stat.appendSQL(" ");
            }
            if (null != havingQuery)
            	stat.appendSQL(havingQuery);
        }
        if (!orderByList.isEmpty()) {
            stat.appendSQL(" ORDER BY ");
            int i = 0;
            for (OrderExpression o : orderByList) {
                if (i++ > 0) {
                    stat.appendSQL(", ");
                }
                o.appendSQL(stat);
                stat.appendSQL(" ");
            }
        }
        return stat;
    }

    Db getDb() {
        return db;
    }

    boolean isJoin() {
        return !joins.isEmpty();
    }

    SelectColumn<T> getSelectColumn(Object obj) {
        return aliasMap.get(obj);
    }

    void addOrderBy(OrderExpression<T> expr) {
        orderByList.add(expr);
    }

    SelectTable<T> getSelectTable(){
    	return from;
    }

    List<SelectTable<?>> getJoins(){
    	return joins;
    }

    private String getSQL(boolean distinct) {
    	TableDefinition<T> def = from.getAliasDefinition();
        SQLStatement selectList = def.getSelectList(db, from.getAs());
        return prepare(selectList, distinct).logSQL().trim();
    }

    @SuppressWarnings("unchecked")
	private <X> String getSQL(X x, boolean distinct) {
    	Class<X> clazz = (Class<X>) x.getClass();
    	if (clazz.isAnonymousClass())
    		clazz = (Class<X>) clazz.getSuperclass();
    	TableDefinition<X> def = JaquSessionFactory.define(clazz, db, false);
        SQLStatement selectList = def.getSelectList(this, x);
        return prepare(selectList, distinct).logSQL().trim();
    }

    private List<T> select(boolean distinct) {
    	List<T> result = Utils.newArrayList();
        TableDefinition<T> def = from.getAliasDefinition();
        SQLStatement selectList = def.getSelectList(db, from.getAs());
        prepare(selectList, distinct).executeQuery(rs -> {
        	 while (rs.next()) {
                 T item = def.readRow(rs, db);
                 db.addSession(item);
                 result.add(item);
             }
        	 return null;
        });

        return result;
    }

    private <U> List<T> union(Query<U> unionQuery, boolean distinct) {
    	if (null == unionQuery) {
			return this.select();
    	}
    	List<T> result = Utils.newArrayList();
    	TableDefinition<T> def = from.getAliasDefinition();
    	SQLStatement selectList = def.getSelectList(db, from.getAs());

    	TableDefinition<U> unionDef = unionQuery.from.getAliasDefinition();
    	SQLStatement unuionSelectList = unionDef.getSelectList(db, unionQuery.from.getAs());

    	selectList = prepare(selectList, distinct);
    	selectList.executeUnion(unionQuery.prepare(unuionSelectList, distinct), rs -> {
    		while (rs.next()) {
                T item = def.readRow(rs, db);
                db.addSession(item);
                result.add(item);
            }
    		return null;
    	});
    	return result;
    }

    private <U, X> List<X> union(Query<U> unionQuery, X x, boolean distinct) {
    	if (null != x){
			Class<?> clazz = x.getClass();
			if (!Utils.isSimpleType(clazz) && !clazz.isEnum()) {
				if (null == unionQuery)
					return this.select(x);

				if (!Object.class.equals(clazz.getSuperclass()))
		        	clazz = clazz.getSuperclass();
				List<X> result = Utils.newArrayList();
		    	@SuppressWarnings("unchecked")
				TableDefinition<X> def = JaquSessionFactory.define((Class<X>)clazz, db, false);
		    	SQLStatement selectList = def.getSelectList(db, from.getAs());
		    	SQLStatement unuionSelectList = def.getSelectList(db, unionQuery.from.getAs());

		    	selectList = prepare(selectList, distinct);
		    	selectList.executeUnion(unionQuery.prepare(unuionSelectList, distinct), rs -> {
		    		while (rs.next()) {
		                X item = def.readRow(rs, db);
		                result.add(item);
		            }
		    		return null;
		    	});
		    	return result;
			}
		}
		return new ArrayList<>();
    }

	@SuppressWarnings("unchecked")
	private <U> List<U> selectRightHandJoin(U tableClass, boolean distinct) {
    	if (tableClass.getClass().isAnonymousClass())
    		throw new JaquError(TO_GET_A_SUBSET);
    	if (this.joins == null || this.joins.isEmpty())
    		throw new JaquError(ILLEGAL_STATE_0, tableClass.getClass().getName());

    	TableDefinition<U> definition = null;
    	String as = null;
    	for (SelectTable<?> selectTable: joins) {
    		if (selectTable.getAlias() == tableClass) {
    			as = selectTable.getAs();
    			definition = (TableDefinition<U>) selectTable.getAliasDefinition();
    			break;
    		}
    	}
    	if (null == definition)
    		throw new JaquError(ILLEGAL_STATE_0, tableClass.getClass().getName());
    	final TableDefinition<U> def = definition;
    	SQLStatement selectList = def.getSelectList(db, as);
    	List<U> result = Utils.newArrayList();
        prepare(selectList, distinct).executeQuery(rs -> {
        	while (rs.next()) {
                U item = def.readRow(rs, db);
                db.addSession(item);
                result.add(item);
            }
        	return null;
        });

        return result;
    }

	@SuppressWarnings("unchecked")
	private <U, Z> List<Z> selectRightHandJoin(U tableClass, boolean distinct, Z x) {
    	if (tableClass.getClass().isAnonymousClass())
    		throw new JaquError(TO_GET_A_SUBSET);
    	if (this.joins == null || this.joins.isEmpty())
    		throw new JaquError(ILLEGAL_STATE_0, tableClass.getClass().getName());

    	List<Z> result = Utils.newArrayList();
    	if (null != x) {
    		// we want a specific list of a single value from the joined tables
    		Class< ? > clazz = x.getClass();
    		if (Utils.isSimpleType(clazz) || clazz.isEnum()) {
		    	SQLStatement selectList = new SQLStatement(db);
		    	for (SelectTable<?> selectTable: joins) {
		    		if (selectTable.getAlias() == tableClass) {
		    			selectTable.appendSqlColumnFromField(selectList, x);
		    		}
		    	}

	        	prepare(selectList, distinct).executeQuery(rs -> {
	        		while (rs.next()) {
		        		Z value = null;
	                	if (x.getClass().isEnum())
	                		value = (Z)handleAsEnum(x.getClass(), rs.getObject(1));
	                	else {
	                    	Types type = Types.valueOf(x.getClass().getSimpleName().toUpperCase());
	                		value = (Z) db.factory.getDialect().getValueByType(type, rs, 1);
	                	}
	                    result.add(value);
                	}
	        		return null;
	        	});
	        }
	        else
	        	throw new JaquError(TO_GET_A_SUBSET);
    	}
    	return result;
    }

    @SuppressWarnings("unchecked")
    private <Z> List<Z> select(Z x, boolean distinct) {
        Class< ? > clazz = x.getClass();
        if (Utils.isSimpleType(clazz) || clazz.isEnum()) {
            return getSimple(x, distinct);
        }
        if (!Object.class.equals(clazz.getSuperclass()))
        	clazz = clazz.getSuperclass();
        return select((Class<Z>) clazz,  x, distinct);
    }

    @SuppressWarnings("unchecked")
	private <K, V> Map<K, V> selectSimpleAsMap(K key, V value, boolean distinct) {
    	SQLStatement selectList = new SQLStatement(db);
        appendSQL(selectList, key, false, null);
        selectList.appendSQL(", ");
        appendSQL(selectList, value, false, null);
        Map<K, V> result = Utils.newHashMap();
        prepare(selectList, distinct).executeQuery(rs -> {
        	while (rs.next()) {
                try {
                	K theKey = null;
                	V theValue = null;
                	if (key.getClass().isEnum())
                		theKey = (K)handleAsEnum(key.getClass(), rs.getObject(1));
                	else {
                		Types type = Types.valueOf(value.getClass().getSimpleName().toUpperCase());
                		theKey = (K) db.factory.dialect.getValueByType(type, rs, 1);
                	}
                	if (value.getClass().isEnum())
                		theValue = (V)handleAsEnum(value.getClass(), rs.getObject(2));
                	else {
                		Types type = Types.valueOf(value.getClass().getSimpleName().toUpperCase());
                		theValue = (V) db.factory.dialect.getValueByType(type, rs, 2);
                	}
                    result.put(theKey, theValue);
                }
                catch (Exception e) {
                    throw new JaquError(e, e.getMessage());
                }
            }
        	return null;
        });
        return result;
    }

    private <X> List<X> select(Class<X> clazz, X x, boolean distinct) {
        List<X> result = Utils.newArrayList();
        TableDefinition<X> def = JaquSessionFactory.define(clazz, db, false);
        SQLStatement selectList = def.getSelectList(this, x);
        prepare(selectList, distinct).executeQuery(rs -> {
        	 while (rs.next()) {
                 X row = def.readRow(rs, db);
                 result.add(row);
             }
        	 return null;
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private <X> List<X> getSimple(X x, boolean distinct) {
        SQLStatement selectList = new SQLStatement(db);
        appendSQL(selectList, x, false, null);
        List<X> result = Utils.newArrayList();
        prepare(selectList, distinct).executeQuery(rs -> {
        	while (rs.next()) {
                try {
                	X value = null;
                	if (x.getClass().isEnum())
                		value = (X)handleAsEnum(x.getClass(), rs.getObject(1));
                	else {
                		Types type = Types.valueOf(x.getClass().getSimpleName().toUpperCase());
                		value = (X) db.factory.getDialect().getValueByType(type, rs, 1);
                	}
                    result.add(value);
                }
                catch (Exception e) {
                    throw new JaquError(e, e.getMessage());
                }
            }
        	return null;
        });
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private Object handleAsEnum(Class enumClass, Object object) {
    	if (null == object || object.getClass().isEnum())
    		return object;
		if (String.class.isAssignableFrom(object.getClass()))
			return Enum.valueOf(enumClass, (String) object);
		// it must be an int type
		return Utils.newEnum(enumClass, (Integer)object);
	}
}
