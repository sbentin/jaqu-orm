/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 * Contributor: Centimia Inc.
 */
package org.h2.jaqu;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import org.h2.jaqu.TableDefinition.FieldDefinition;
import org.h2.jaqu.bytecode.ClassReader;
import org.h2.jaqu.util.JdbcUtils;
import org.h2.jaqu.util.StatementLogger;
import org.h2.jaqu.util.Utils;

/**
 * This class represents a query.
 *
 * @param <T> the return type
 */
public class Query<T> implements FullQueryInterface<T> {

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
        Query<T> query = new Query<T>(db);
        TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
        query.from = new SelectTable<T>(query, alias, false);
        def.initSelectObject(query.from, alias, query.aliasMap);
        return query;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#selectCount()
	 */
    public long selectCount() {
        SQLStatement selectList = new SQLStatement(db);
        selectList.setSQL("COUNT(*)");
        ResultSet rs = prepare(selectList, false).executeQuery();
        try {
            rs.next();
            long value = rs.getLong(1);
            return value;
        } 
        catch (SQLException e) {
            throw new RuntimeException(e);
        } 
        finally {
            JdbcUtils.closeSilently(rs);
        }
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#select()
	 */
    public List<T> select() {
        return select(false);
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#selectFirst()
	 */
    public T selectFirst() {
    	List<T> list = select(false);
        return list.isEmpty() ? list.get(0) : null;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#selectDistinct()
	 */
    public List<T> selectDistinct() {
        return select(true);
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#selectFirst(Z)
	 */
    @SuppressWarnings("unchecked")
    public <X, Z> X selectFirst(Z x) {
        List<X> list = (List<X>) select(x);
        return list.isEmpty() ? null : list.get(0);
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#getSQL()
	 */
    public String getSQL() {
        SQLStatement selectList = new SQLStatement(db);
        selectList.setSQL("*");
        return prepare(selectList, false).getSQL().trim();
    }

    private List<T> select(boolean distinct) {
    	List<T> result = Utils.newArrayList();
        TableDefinition<T> def = from.getAliasDefinition();
        SQLStatement selectList = def.getSelectList(db, from.getAs());
        ResultSet rs = prepare(selectList, distinct).executeQuery();
        try {
            while (rs.next()) {
                T item = from.newObject();
                from.getAliasDefinition().readRow(item, rs, db);
                db.addSession(item);
                result.add(item);
            }
        } 
        catch (SQLException e) {
            throw new RuntimeException(e);
        } 
        finally {
            JdbcUtils.closeSilently(rs);
        }
        return result;
    }
    
   /**
    * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
    * Returns a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
    * 
    * @param tableClass - the object descriptor of the type needed on return
    * @throws IllegalStateException - when not in join query
    */
    @SuppressWarnings("unchecked")
	public <U> List<U> selectRightHandJoin(U tableClass){
    	Class<U> clazz = (Class<U>) tableClass.getClass();
    	return selectRightHandJoin(clazz, false);
    }
    
    /**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns a list of distinct results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws IllegalStateException - when not in join query
     */
    @SuppressWarnings("unchecked")
	public <U> List<U> selectDistinctRightHandJoin(U tableClass){
    	Class<U> clazz = (Class<U>) tableClass.getClass();
    	return selectRightHandJoin(clazz, true);
    }
    
    /**
     * A convenience method to get the object representing the right hand side of the join relationship only (without the need to specify the mapping between fields)
     * Returns the first result of a list of results, of the given type. The given type must be a part of a join query or an exception will be thrown
     * 
     * @param tableClass - the object descriptor of the type needed on return
     * @throws IllegalStateException - when not in join query
     */
    @SuppressWarnings("unchecked")
	public <U> U selectFirstRightHandJoin(U tableClass){
    	Class<U> clazz = (Class<U>) tableClass.getClass();
    	List<U> list = selectRightHandJoin(clazz, false);
    	return list.isEmpty() ? null : list.get(0);
    }
    
    private <U> List<U> selectRightHandJoin(Class<U> tableClass, boolean distinct) {
    	if (tableClass.isAnonymousClass())
    		throw new RuntimeException("To get a subset of the fields or a a mix of the fields using mapping use the 'select(Z)' or 'selectFirst(Z)' or 'selectDistinct(Z)' methods");
    	if (this.joins == null || this.joins.isEmpty())
    		throw new IllegalStateException("Entity based on " + tableClass.getName() + " must be part of a join query!!");
    	
    	String as = null;
    	for (SelectTable<?> selectTable: joins) {
    		if (selectTable.getAlias().getClass().isAssignableFrom(tableClass))
    			as = selectTable.getAs();
    	}
    	List<U> result = Utils.newArrayList();
        TableDefinition<U> def = db.define(tableClass);
        SQLStatement selectList = def.getSelectList(db, as);
        ResultSet rs = prepare(selectList, distinct).executeQuery();
        try {
            while (rs.next()) {
                U item = tableClass.newInstance();
                def.readRow(item, rs, db);
                db.addSession(item);
                result.add(item);
            }
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        } 
        finally {
            JdbcUtils.closeSilently(rs);
        }
        return result;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#delete()
	 */
    public int delete() {
    	TableDefinition<T> def = from.getAliasDefinition();
        SQLStatement stat = new SQLStatement(db);
        if (def.isAggregateParent) {
        	// before we delete we must take care of relationships
	        stat.appendSQL("SELECT * FROM ");
	        from.appendSQL(stat);
	        appendWhere(stat);
	        if (db.factory.isShowSQL())
	        	StatementLogger.delete(stat.getSQL());
	        ResultSet rs = stat.executeQuery();
	        try {
	            while (rs.next()) {
	                T item = from.newObject();
	                from.getAliasDefinition().readRow(item, rs, db);
	                for (FieldDefinition fdef: def.getFields()) {
	                	if (fdef.fieldType.ordinal() > 1) {
	                		// this is a relation
	                		db.deleteParentRelation(fdef, item); // item has relations so it must be a Entity type
	                	}
	                }
	            }
	        } 
	        catch (SQLException e) {
	            throw new RuntimeException(e);
	        } 
	        finally {
	            JdbcUtils.closeSilently(rs);
	        }
        }
        stat = new SQLStatement(db);
        // Nasty hack for MYSQL
        if (def.DIALECT == Dialect.MYSQL)
        	stat.appendSQL("DELETE " + from.getAs() + " FROM ");
        else
        	stat.appendSQL("DELETE FROM ");
        from.appendSQL(stat);
        appendWhere(stat);
        if (db.factory.isShowSQL())
        	StatementLogger.delete(stat.getSQL());
        return stat.executeUpdate();
    }
    
    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#update()
	 */
    public int update() {
        SQLStatement stat = new SQLStatement(db);
        stat.appendSQL("UPDATE ");
        from.appendSQL(stat);
        appendUpdate(stat);
        appendWhere(stat);
        if (stat.getSQL().indexOf("SET") == -1)
        	throw new IllegalArgumentException("To perform update use the set directive after from...!!!");
        if (db.factory.isShowSQL())
        	StatementLogger.update(stat.getSQL());
        return stat.executeUpdate();
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#selectDistinct(Z)
	 */
    public <X, Z> List<X> selectDistinct(Z x) {
        return select(x, true);
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#select(Z)
	 */
    public <X, Z> List<X> select(Z x) {
        return select(x, false);
    }

    @SuppressWarnings("unchecked")
    private <X, Z> List<X> select(Z x, boolean distinct) {
        Class< ? > clazz = x.getClass();
        if (Utils.isSimpleType(clazz)) {
            return getSimple((X) x, distinct);
        }
        clazz = clazz.getSuperclass();
        return select((Class<X>) clazz, (X) x, distinct);
    }

    private <X> List<X> select(Class<X> clazz, X x, boolean distinct) {
        List<X> result = Utils.newArrayList();
        TableDefinition<X> def = JaquSessionFactory.define(clazz, db, false);
        SQLStatement selectList = def.getSelectList(this, x);
        ResultSet rs = prepare(selectList, distinct).executeQuery();
        try {
            while (rs.next()) {
                X row = Utils.newObject(clazz);
                def.readRow(row, rs, null);
                result.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeSilently(rs);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <X> List<X> getSimple(X x, boolean distinct) {
        SQLStatement selectList = new SQLStatement(db);
        appendSQL(selectList, x);
        ResultSet rs = prepare(selectList, distinct).executeQuery();
        List<X> result = Utils.newArrayList();
        try {
            while (rs.next()) {
                try {
                    X value = (X) rs.getObject(1);
                    result.add(value);
                } 
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } 
        catch (SQLException e) {
            throw new RuntimeException(e);
        } 
        finally {
            JdbcUtils.closeSilently(rs);
        }
        return result;
    }

    public <A> QueryWhere<T> where(final StringFilter whereCondition){
    	Token conditionCode = new Token() {
			
			@SuppressWarnings("hiding")
			public <T> void appendSQL(SQLStatement stat, Query<T> query) {
				stat.appendSQL(whereCondition.getConditionString(query.from));			
			}
		};
		conditions.add(conditionCode);
		return new QueryWhere<T>(this);
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#where(A)
	 */
    public <A> QueryCondition<T, A> where(A x) {
        return new QueryCondition<T, A>(this, x);
    }
    
    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#set(A, A)
	 */
    public <A> QuerySet<T, A> set(A x, A v) {
    	if (Collection.class.isAssignableFrom(x.getClass())) {
    		// this is a relation updating is not supported like this
    		throw new IllegalStateException("To update relations use db.update(Object)");
    	}
    	return new QuerySet<T, A>(this, x, v);
    }
    
    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#primaryKey(java.lang.Object)
	 */
    public QueryCondition<T, Object> primaryKey() {
    	TableDefinition<?> def = from.getAliasDefinition();
    	
    	// def will not be null here
    	List<TableDefinition.FieldDefinition> primaryKeys = def.getPrimaryKeyFields();
    	if (primaryKeys == null || primaryKeys.size() == 0) {
            throw new IllegalStateException("No primary key columns defined for table " + def.tableName + " - no update possible");
        }
    	if (primaryKeys.size() > 1)
    		throw new UnsupportedOperationException("Entity relationship is not supported for complex primary keys. Found in " + def.tableName);
    	for (TableDefinition.FieldDefinition field: primaryKeys) {
    		return new QueryCondition<T, Object>(this, field.getValue(from.getAlias()));
    	}
		return null;
	}
    
    /**
	 * Start the where clause from here.
	 * NOT SUPPORTED YET
	 * @param <A>
	 * @param filter - a filter on fields of the object, to run through a compiler/ parser
	 * @return QueryWhere<T>
	 */
    public <A> QueryWhere<T> where(Filter filter) {
        HashMap<String, Object> fieldMap = Utils.newHashMap();
        for (Field f : filter.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object obj = f.get(filter);
                if (obj == from.getAlias()) {
                    List<TableDefinition.FieldDefinition> fields = from.getAliasDefinition().getFields();
                    String name = f.getName();
                    for (TableDefinition.FieldDefinition field : fields) {
                        String n = name + "." + field.field.getName();
                        Object o = field.field.get(obj);
                        fieldMap.put(n, o);
                    }
                }
                fieldMap.put(f.getName(), f.get(filter));
            } 
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            f.setAccessible(false);
        }
        Token filterCode = new ClassReader().decompile(filter, fieldMap, "where");
        // String filterQuery = filterCode.toString();
        conditions.add(filterCode);
        return new QueryWhere<T>(this);
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#whereTrue(java.lang.Boolean)
	 */
    public QueryWhere<T> whereTrue(Boolean condition) {
        Token token = new Function("", condition);
        addConditionToken(token);
        return new QueryWhere<T>(this);
    }
//## Java 1.5 end ##

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#orderBy(java.lang.Object)
	 */
//## Java 1.5 begin ##
    public QueryInterface<T> orderBy(Object... expressions) {
        for (Object expr : expressions) {
            OrderExpression<T> e =
                new OrderExpression<T>(this, expr, false, false, false);
            addOrderBy(e);
        }
        return this;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#orderByDesc(java.lang.Object)
	 */
    public QueryInterface<T> orderByDesc(Object expr) {
        OrderExpression<T> e =
            new OrderExpression<T>(this, expr, true, false, false);
        addOrderBy(e);
        return this;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#groupBy(java.lang.Object)
	 */
    public Query<T> groupBy(Object... groupBy) {
        this.groupByExpressions = groupBy;
        return this;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#appendSQL(org.h2.jaqu.SQLStatement, java.lang.Object)
	 */
    public void appendSQL(SQLStatement stat, Object x) {
        if (x == Function.count()) {
            stat.appendSQL("COUNT(*)");
            return;
        }
        Token token = Db.getToken(x);
        if (token != null) {
            token.appendSQL(stat, this);
            return;
        }
        SelectColumn<T> col = aliasMap.get(x);
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
            stat.appendSQL(" WHERE ");
            for (Token token : conditions) {
                token.appendSQL(stat, this);
                stat.appendSQL(" ");
            }
        }
        // add the discriminator if 'T' is a part of an inheritance tree.
        if (from.getAliasDefinition().inheritedType == InheritedType.DISCRIMINATOR) {
        	stat.appendSQL(" AND " + from.getAs() + "." + from.getAliasDefinition().discriminatorColumn + "=" + from.getAliasDefinition().discriminatorValue + " ");
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

    @SuppressWarnings("unchecked")
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
            stat.appendSQL(" GROUP BY ");
            int i = 0;
            for (Object obj : groupByExpressions) {
                if (i++ > 0) {
                    stat.appendSQL(", ");
                }
                appendSQL(stat, obj);
                stat.appendSQL(" ");
            }
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
        if (db.factory.isShowSQL())
        	StatementLogger.select(stat.getSQL());
        return stat;
    }

    /* (non-Javadoc)
	 * @see org.h2.jaqu.FullQueryInterface#innerJoin(U)
	 */
    @SuppressWarnings("unchecked")
    public <U> QueryJoin innerJoin(U alias) {
        TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
        SelectTable<T> join = new SelectTable(this, alias, false);
        def.initSelectObject(join, alias, aliasMap);
        joins.add(join);
        return new QueryJoin(this, join);
    }
    
    /* (non-Javadoc)
     * @see org.h2.jaqu.FullQueryInterface#leftOuterJoin(U)
     */
    @SuppressWarnings("unchecked")
    public <U> QueryJoin leftOuterJoin(U alias) {
        TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
        SelectTable<T> join = new SelectTable(this, alias, true);
        def.initSelectObject(join, alias, aliasMap);
        joins.add(join);
        return new QueryJoin(this, join);
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
}
