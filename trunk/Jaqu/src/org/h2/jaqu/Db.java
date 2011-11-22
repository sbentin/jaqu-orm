/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.h2.jaqu.TableDefinition.FieldDefinition;
import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.util.JdbcUtils;
import org.h2.jaqu.util.StatementBuilder;
import org.h2.jaqu.util.StatementLogger;
import org.h2.jaqu.util.Utils;
import org.h2.jaqu.util.WeakIdentityHashMap;

/**
 * This class represents a connection to an underlying data store which supports JDBC.
 */
public class Db {

    private static final Map<Object, Token> TOKENS = Collections.synchronizedMap(new WeakIdentityHashMap<Object, Token>());

    private final Connection conn;
    protected JaquSessionFactory factory;
    
    /* 
     * A list of objects this thread has already visited. Keeps a different list per thread.
     * 
     * Key - The object reEntrant
     * Value - Last Parent who holds this object 
     */
    ThreadLocal<Map<Class<?>, Map<String, Object>>> reEntrantList = new ThreadLocal<Map<Class<?>, Map<String, Object>>>();

	private boolean closed = true;
    
    Db(Connection conn, JaquSessionFactory factory) {
        this.conn = conn;
        this.factory = factory;
        this.closed = false;
    }

    /**
     * Insert the given object and all it's children to the DB.
     * TODO support cascadeType
     * @param <T>
     * @param t
     */
    public <T> void insert(T t) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	checkSession(t);
        Class<?> clazz = t.getClass();
        define(clazz).insert(this, t);
    }

    /**
     * Inserts the object and returns it's primary key, generated or not.
     * 
     * @param <T>
     * @param <X>
     * @param t
     * @return X the primary key.
     * @throws JaquError when no primary keys exists or more then one primary key exists or when the object inserted and primary key could not be retrieved
     * @throws RuntimeException (could also be a JaquError) when insert failed. 
     */
	@SuppressWarnings("unchecked")
	public <T,X> X insertAndGetPK(T t){
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	checkSession(t);
        Class<T> clazz = (Class<T>) t.getClass();
        TableDefinition<T> td = define(clazz);
        
        List<FieldDefinition> primaryKeys = td.getPrimaryKeyFields();
        if (null == primaryKeys || primaryKeys.isEmpty())
        	throw new JaquError("Object " + t.getClass().getName() + " has no primary keys defined");
        
        if (primaryKeys.size() > 1)
        	throw new JaquError("NOT SUPPORTED! - Can not return a key for an Object [" + t.getClass() + "] with more then one primary key defined!!!");
        td.insert(this, t);
        primaryKeys.get(0).field.setAccessible(true);
        X pk = null;
		try {			
			pk = (X) primaryKeys.get(0).field.get(t);
		}
		catch (Exception e) {
			// unable to retrieve the key, however the object was inserted to the db so we return anyway but with null;
			throw new JaquError(e);
		}
        return pk; 
    }
    
	/**
	 * Utility method to get the primary key of an existing entity or table
	 * Can also be called from the factory class.
	 * 
	 * @see org.h2.jaqu.JaquSessionFactory#getPrimaryKey(Object)
	 * @param <T>
	 * @param <X>
	 * @param t
	 * @return X
	 */
	public <T, X> X getPrimaryKey(T t){
		return factory.getPrimaryKey(t);
	}
	
    /**
     * Insert objects in a comma delimited array of 0..n
     * 
     * @param <T>
     * @param tArray
     */
    public <T> void insert(T ... tArray) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t : tArray) {
            insert(t);
        }
    }
    
    /**
     * Insert all objects on the list
     * 
     * @param <T>
     * @param list
     */
    public <T> void insertAll(List<T> list) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
        for (T t : list) {
            insert(t);
        }
    }
    
    /**
     * Merge means that if the object exists it is updated (so are all his children), if not it is inserted (so are all his children)
     * TODO support cascadeType
     * @param <T>
     * @param t
     */
    public <T> void merge(T t) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	checkSession(t);
        Class< ? > clazz = t.getClass();
        define(clazz).merge(this, t);
    }

    /**
     * merge all the given objects of the same type. They are merged in the order in which they are iterated on within the list
     * 
     * @param <T>
     * @param list
     */
    public <T> void merge(List<T> list) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t: list){
    		merge(t);
    	}
    }
    
    /**
     *  merge all the given objects of the same type. They are merged in the order in which they are given.
     *  
     * @param <T>
     * @param tArray
     */
    public <T> void merge(T ... tArray) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t: tArray){
    		merge(t);
    	}
    }
    
    /**
     * Delete the immediate given object. If the object has a relationships, the link is always updated according to the cascade type.
     * 
     * @param <T>
     * @param t
     */
    public <T> void delete(T t) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	checkSession(t);
    	Class<?> clazz = t.getClass();
    	TableDefinition<?> tdef = define(clazz);
    	if (tdef.isAggregateParent) {
    		// we have aggregate children
    		for (FieldDefinition fdef: tdef.getFields()) {
    			if (fdef.fieldType.ordinal() > 1) { // more then a one to one relation.
    				deleteParentRelation(fdef, t); // if it has relations it must be a Table type by design
    			}
    		}
    	}
    	// after dealing with the children we delete the object
    	tdef.delete(this, t);
    }
    
    /**
     * Delete all the given objects of the same type. Deletes in the order the given list is iterated upon.
     * 
     * @param <T>
     * @param list
     */
    public <T> void delete(List<T> list) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t: list){
    		delete(t);
    	}
    }
    
    /**
     * Delete all the given objects of the same type. Deletes in the order they given.
     * 
     * @param <T>
     * @param list
     */
    public <T> void delete(T ... tArray) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t: tArray){
    		delete(t);
    	}
    }
    
    /**
     * Updates the immediate given object. If the object has a relationship, the link is always updated as needed. In case where the link 
     * is to a non persisted entity the new entity is inserted into the DB.
     * 
     * @param <T>
     * @param t
     */
    public <T> void update(T t) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	checkSession(t);
        Class< ? > clazz = t.getClass();
        define(clazz).update(this, t);
    }

    /**
     * Update all the given objects of the same type. Updates in the order the given list is iterated upon.
     * 
     * @param <T>
     * @param list
     * @see Db#update(Object))
     */
    public <T> void update(List<T> list){
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t: list){
    		update(t);
    	}
    }
    
    /**
     * Update all the given objects of the same type. Updates in the order they are given.
     * 
     * @param <T>
     * @param list
     * @see Db#update(Object))
     */
    public <T> void update(T ... tArray){
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	for (T t: tArray){
    		update(t);
    	}
    }
    
    /**
     * Represents the "from clause" of the SQL select
     * @param <T>
     * @param alias
     * @return QueryInterface<T>
     */
    public <T extends Object> QueryInterface<T> from(T alias) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
        Class<?> clazz = alias.getClass();
        define(clazz);
        return Query.from(this, alias);
    }

    /**
     * API for creating a table from a given object. If the object is annotated with entity the relation's (if exist) tables are also created
     * @param <T>
     * @param clazz
     */
    public <T> void createTable(Class<T> clazz) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
        define(clazz);
    }

    /**
     * Rollback the underlying connection
     */
    public void rollback() {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
		try {
			this.conn.rollback();
		}
		catch (SQLException e) {
			// can't rollback nothing can be done!!!
		}		
	}

    /**
     * Commit the underlying connection
     */
	public void commit() {
		if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
		try {
			this.conn.commit();
		}
		catch (SQLException e) {
			// can't commit nothing can be done!!!
		}	
	}
	
    /**
     * Closes the session and underlying db connection
     */
    public void close() {
        try {
            conn.close();
            this.closed  = true;
        } 
        catch (Exception e) {
            throw new JaquError(e);
        }
    }

    /**
     * Run a SQL query directly against the database.
     *
     * @param sql the SQL statement
     * @return the result set
     */
    public ResultSet executeQuery(String sql) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
        try {
        	if (factory.isShowSQL())
				StatementLogger.select(sql);
            return conn.createStatement().executeQuery(sql);
        } 
        catch (SQLException e) {
            throw new JaquError(e.getMessage(), e);
        }
    }
    
    /**
     * Run a SQL statement directly against the database.
     *
     * @param sql the SQL statement
     * @return the update count
     */
    public int executeUpdate(String sql) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
        try {
            Statement stat = conn.createStatement();
            if (factory.isShowSQL())
				StatementLogger.update(sql);
            int updateCount = stat.executeUpdate(sql);
            stat.close();
            return updateCount;
        } 
        catch (SQLException e) {
            throw new JaquError(e.getMessage(), e);
        }
    }
	
 
    /**
     * If the Object is not in a synchronized connection to the DB this method synchronizes the object into the session. 
     * After synchronization is done, all the deleted relations are deleted according to the cascadeType. To save the Object to the DB
     * {@see update} should be called on the object!
     * 
     * <p>
     * <b>[Notice], This method performs a delete to the underlying persistence store!!! Be very careful when merging Table Objects. It is very recommended to do work on the object
     * within the session and detach them for display purposes. </b>
     * 
     * @param t
     */
    @SuppressWarnings("unchecked")
	void attach(Object t) {
    	if (this.closed)
    		throw new IllegalStateException("Session is closed!!!");
    	this.addSession(t);
    	// get the relations, through a reflection get (not to do anything by lazy loading)
    	TableDefinition<?> tdef = define(t.getClass());
    	FieldDefinition pkDef = tdef.getPrimaryKeyFields().get(0);
    	Object pk;
		try {
			pkDef.field.setAccessible(true);
			pk = pkDef.field.get(t);
		}
		catch (Exception e) {
			throw new JaquError("PK Not accessible!!!", e);
		}
    	for (FieldDefinition fdef: tdef.getFields()) {
    		try {
				switch (fdef.fieldType) {
					case NORMAL: continue;
					case FK: {
						Object o = fdef.field.get(t);
						if (o != null)
							addSession(o);
						break;
					}
					case O2M:
					case M2M: {
						Collection<?> col = (Collection<?>) fdef.field.get(t);
						if (col != null) {
							if (AbstractJaquCollection.class.isAssignableFrom(col.getClass())) {
								((AbstractJaquCollection) col).setDb(this);
								((AbstractJaquCollection) col).merge();
							}
							else {
								// here we get a whole new relationship container which symbolizes the current relationships
								// of the Entity. However, there still might be  a case where we have relationships existing in the DB.
								// Thus, our algorithm should be, clean the current relationships then apply the new ones. Here we think of performance!
								// If the relationship has cascade type delete, we need to dispose of the objects as well as the relationships, else we just 
								// dispose of the relationships.
								if (null != pk) {
									// we may get objects that have a primary key to be inserted with which means they don't exist in the DB
									Object parent = this.from(t.getClass().newInstance()).primaryKey().is(pk).selectFirst();
									if (null != parent)
										deleteParentRelation(fdef, parent);
								}
								if (List.class.isAssignableFrom(col.getClass())) {
									JaquList l = new JaquList((List) col, this, fdef, factory.getPrimaryKey(t));
									l.setDb(this);
									l.merge();
									fdef.field.set(t, l);
								}
								else if (Set.class.isAssignableFrom(col.getClass())) {
									JaquSet s = new JaquSet((Set)col, this, fdef, factory.getPrimaryKey(t));
									s.setDb(this);
									s.merge();
									fdef.field.set(t, s);
								}
							}
						}
						break;
					}
				}
			}
			catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException)e;
				else throw new JaquError(e.getMessage(), e);
			}
    	}
    }
    
    /**
     * Check if the {@link Entity} is part of this live session. If not attach it to this session.
     * @param <T>
     * @param t
     */
    <T> void checkSession(T t) {
    	try {
			if (t.getClass().getAnnotation(Entity.class) != null) {
				// instrument this instance of the class
				Field dbField = t.getClass().getField("db");
				dbField.setAccessible(true);
				
				// put the open connection on the object. As long as the connection is open calling the getter method on the 'obj' will produce the relation
				Object o = dbField.get(t);
				if (!this.equals(o))
					attach(t);
			}
		}
		catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new JaquError(e.getMessage(), e);
		}
    }
    
    /**
     * The method returns a list of relation objects. Used for lazy loading of object relationships
     * @param <T>
     * @param fieldName
     * @param myObject
     * @param type
     * @return Collection<T>
     */
	<T> Collection<T> getRelationFromDb(String fieldName, Object myObject, Class<T> type) {
		if (closed)
			throw new JaquError("Cannot initialize 'Relation' outside an open session!!!. Try initializing field directly within the class.");
		TableDefinition<?> def = define(myObject.getClass());
		FieldDefinition definition = def.getDefinitionForField(fieldName);
		
		
		if (!Collection.class.isAssignableFrom(definition.field.getType()))
			throw new JaquError(fieldName + " relation is not a collection type!!!");
		try {
			List<T> result = (List<T>) getRelationFromDb(definition, factory.getPrimaryKey(myObject), type);
			if (definition.field.getType().isAssignableFrom(result.getClass()))
				return new JaquList<T>(result, this, definition, factory.getPrimaryKey(myObject));
			else {
				// only when the type is a Set type we will be here
				HashSet<T> set = Utils.newHashSet();
				set.addAll(result);
				return new JaquSet<T>(set, this, definition, factory.getPrimaryKey(myObject));
			}
		}
		catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new JaquError(e.getMessage(), e);
		}
	}
	
	/**
	 * Prepare the statement
	 * @param sql
	 * @return PreparedStatement
	 */
    PreparedStatement prepare(String sql) {
        try {
            return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } 
        catch (SQLException e) {
            throw new JaquError(e);
        }
    }

    static <X> X registerToken(X x, Token token) {
        TOKENS.put(x, token);
        return x;
    }

    static Token getToken(Object x) {
        return TOKENS.get(x);
    }
    
    /**
     * If not defined this method defines the table and creates it if needed.
     * @param <T>
     * @param clazz
     * @return TableDefinition
     */
    <T> TableDefinition<T> define(Class<T> clazz){
    	return JaquSessionFactory.define(clazz, this);
    }
    
    /*
     * 
     * @param <A>
     * @param x
     * @return
     */
    <A> TestCondition<A> test(A x) {
        return new TestCondition<A>(x);
    }
    
    /**
     * Prepares the reentrant list with the object that should no be reentered into.
     * 
     * @param obj
     */
    void prepareRentrant(Object obj) {
		if (this.reEntrantList.get() == null) {
			Map<Class<?>, Map<String, Object>> map = Utils.newHashMap();
			Map<String, Object> innerMap = Utils.newHashMap();
			innerMap.put(factory.getPrimaryKey(obj).toString(), obj);
			map.put(obj.getClass(), innerMap);
			this.reEntrantList.set(map);
		}
		else {
			Map<Class<?>, Map<String, Object>> map = this.reEntrantList.get();
			Map<String, Object> innerMap = map.get(obj.getClass());
			if (innerMap == null) {
				innerMap = Utils.newHashMap();
				innerMap.put(factory.getPrimaryKey(obj).toString(), obj);
				map.put(obj.getClass(), innerMap);
			}
			innerMap.put(factory.getPrimaryKey(obj).toString(), obj);
		}
	}
    
    void removeReentrant(Object obj) {
    	if (this.reEntrantList.get() != null) {
    		Map<Class<?>, Map<String, Object>> map = this.reEntrantList.get();
    		Map<String, Object> innerMap = map.get(obj.getClass());
    		innerMap.remove(factory.getPrimaryKey(obj).toString());
    		if (innerMap.isEmpty())
    			map.remove(obj.getClass());
    	}
    }
    
	<T> List<T> getRelationByRelationTable(FieldDefinition def, Object myPrimaryKey, Class<T> type){
		TableDefinition<T> targetDef = define(type);
		// for String primary keys do the following
		String pk = (myPrimaryKey instanceof String) ? "'"+myPrimaryKey.toString()+"'" : myPrimaryKey.toString();
		StatementBuilder builder = new StatementBuilder("SELECT target.* FROM ").append(targetDef.tableName).append(" target, ").append(def.relationDefinition.relationTableName);
		builder.append(" rt where rt.").append(def.relationDefinition.relationFieldName).append("=").append(pk).append(" and rt.").append(def.relationDefinition.relationColumnName);
		builder.append("= target.").append(targetDef.getPrimaryKeyFields().get(0).columnName);

		List<T> result = Utils.newArrayList();
		ResultSet rs = null;
		try {
        	rs = prepare(builder.toString()).executeQuery();
            while (rs.next()) {
                T item = Utils.newObject(type);
                targetDef.readRow(item, rs, this);
                result.add(item);
            }
        } 
        catch (SQLException e) {
            throw new JaquError(e.getMessage(), e);
        } 
        finally {
            JdbcUtils.closeSilently(rs);
        }
        return result;
	}
    
	boolean checkReEntrant(Object obj) {
		if (obj.getClass().getAnnotation(Entity.class) != null)
			return checkReEntrant(obj.getClass(), factory.getPrimaryKey(obj)) != null;
		return false;
	}
	
	Object checkReEntrant(Class<?> clazz, Object key) {
		if (key != null){
			key = key.toString();
			if (reEntrantList.get() != null) {
				Map<String, ?> innerMap = reEntrantList.get().get(clazz);
				if (innerMap != null) {
					return innerMap.get(key);
				}
			}
		}
		return null;
	}

	private <T> List<T> getRelationFromDb(final FieldDefinition def, final Object myPrimaryKey, Class<T> type) throws Exception {
		T descriptor = type.newInstance();
		List<T> result;
		if (def.relationDefinition.relationTableName == null) {
			result = this.from(descriptor).where(new StringFilter() {
				
				/*
				 * (non-Javadoc)
				 * @see org.h2.jaqu.StringFilter#getConditionString(org.h2.jaqu.SelectTable)
				 */
				public String getConditionString(SelectTable<?> st) {
					return st.getAs() + "." + def.relationDefinition.relationFieldName  + " = " + myPrimaryKey.toString();
				}
			}).select();
		}
		else
			result = this.getRelationByRelationTable(def, myPrimaryKey, type);
		return result;		
	}
	
	void addSession(Object obj) {
		try {
			if (obj.getClass().getAnnotation(Entity.class) != null) {
				// instrument this instance of the class
				Field dbField = obj.getClass().getField("db");
				dbField.setAccessible(true);
				// put the open connection on the object. As long as the connection is open calling the getter method on the 'obj' will produce the relation
				dbField.set(obj, this);
			}
		}
		catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new JaquError(e.getMessage(), e);
		}
	}

	/**
	 * True when the Db is closed
	 * @return boolean
	 */
	boolean closed() {
		return closed;
	}

	/**
	 * 
	 * @param field - the definition of the field on the parent
	 * @param table - the object child to be updated.
	 * @param obj - the object Parent to update the child with
	 */
	void updateRelationship(FieldDefinition field, Object table, Object obj) {
		String primaryKey = (factory.getPrimaryKey(table) instanceof String) ? "'" + factory.getPrimaryKey(table) + "'" : factory.getPrimaryKey(table).toString();
		String relationPK = (factory.getPrimaryKey(obj) instanceof String) ? "'" + factory.getPrimaryKey(obj) + "'" : factory.getPrimaryKey(obj).toString();
		switch (field.fieldType) {
			case O2M: handleO2MRelationship(field, table, obj, primaryKey, relationPK); return;
			case M2M: handleM2Mrelationship(field, table, obj, primaryKey, relationPK); return;
		}
	}

	private void handleM2Mrelationship(FieldDefinition field, Object table, Object obj, String primaryKey, String relationPK) {
		// we wan't to update the other side relationship. Because we're in session we can simply get the list and set it to null
		// when the user calls get again the list will be lazy loaded with the correct values....
		Field targetField;
		try {
			targetField = table.getClass().getDeclaredField(field.relationDefinition.relationFieldName);
			targetField.setAccessible(true);
			targetField.set(table, null);
			targetField.setAccessible(false);
		}
		catch (Exception e1) {
			throw new IllegalStateException("The object for table " + table.getClass() + " does not hold a list of " + obj.getClass() + "!! Data is not consistent");
		}
		
		// field has a relation table. In relation table we do a merge (i.e insert only if missing update if exists)
		mergeRelationTable(field, primaryKey, relationPK);
	}

	private void handleO2MRelationship(FieldDefinition field, Object table, Object obj, String primaryKey, String relationPK) {
		if (field.relationDefinition.relationTableName == null) {
			// We have a relationship without a relationTable. We might have a two sided O2M relationship, or a single sided relationship
			try {
				Field realtedField = table.getClass().getDeclaredField(field.relationDefinition.relationFieldName);
				realtedField.setAccessible(true);
				// update the object for consistency
				realtedField.set(table, obj);
				realtedField.setAccessible(false);
				update(table);
			}
			catch (NoSuchFieldException e) {
				// this is not a two sided relationship, we need to update the table with the id
				// when we reach here the object has already been merged. All we need to do is update it.
				
				// Calling define here costs very little since this table's definition is cached.
				TableDefinition<?> def = define(table.getClass());
				StatementBuilder updateQuery = new StatementBuilder("UPDATE ").append(def.tableName);
				updateQuery.append(" SET ").append(field.relationDefinition.relationFieldName).append(" = ").append(relationPK);
				// we assume that our table has a single column primary key.
				updateQuery.append(" WHERE ").append(def.getPrimaryKeyFields().get(0).columnName).append(" = ").append(primaryKey);
				
				executeUpdate(updateQuery.toString());
			}
			catch (Exception e) {
				throw new JaquError(e.getMessage(), e);
			}			
			return;
		}
		
		// field has a relation table. In relation table we do a merge (i.e insert only if missing update if exists)
		mergeRelationTable(field, primaryKey, relationPK);
	}

	/**
	 * Create a relation table entry for the relationship if one does not exist.
	 * @param field
	 * @param primaryKey
	 * @param relationPK
	 */
	private void mergeRelationTable(FieldDefinition field, String primaryKey, String relationPK) {
		StatementBuilder checkExistsQuery = new StatementBuilder("SELECT * FROM ").append(field.relationDefinition.relationTableName).append(" WHERE ");
		checkExistsQuery.append(field.relationDefinition.relationColumnName).append(" = ").append(primaryKey).append(" AND ").append(field.relationDefinition.relationFieldName);
		checkExistsQuery.append(" = ").append(relationPK);
		
		ResultSet rs = executeQuery(checkExistsQuery.toString());
		
		try {
			if (!rs.next()) {
				// the relationship is missing
				StatementBuilder insertStmnt = new StatementBuilder("INSERT INTO ").append(field.relationDefinition.relationTableName);
				insertStmnt.append(" (").append(field.relationDefinition.relationColumnName).append(',').append(field.relationDefinition.relationFieldName).append(") ");
				insertStmnt.append(" VALUES (").append(primaryKey).append(',').append(relationPK).append(')');
				
				executeUpdate(insertStmnt.toString());
			}
		}
		catch (SQLException se) {
			throw new JaquError(se);
		}
		finally {
			JdbcUtils.closeSilently(rs);
		}
	}

	/*
	 * Removes the relationship between all children of the given parent to the parent because the parent is being deleted from the underlying persistence layer.
	 * If cascade type is delete the children are also removed from DB.
	 * 
	 */
	void deleteParentRelation(FieldDefinition fdef, Object parent) {
		if (fdef.relationDefinition.cascadeType == CascadeType.DELETE) {
			// in one to many there is a question of cascade delete, i.e delete the child as well. In M2M this can not be true
			try {
				// using the getter because of lazy loading...
				fdef.getter.setAccessible(true);
				Collection<?> relations = (Collection<?>) fdef.getter.invoke(parent); // this must be a collection by design
				if (relations != null) {
					for (Object o: relations)
						delete(o);
				}
				fdef.getter.setAccessible(false);
			}
			catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException)e;
				else
					throw new JaquError(e.getMessage(), e);
			}
		}
		if (fdef.relationDefinition.relationTableName == null && fdef.relationDefinition.cascadeType != CascadeType.DELETE) { // if it's cascade delete these objects where deleted already so we can skip
			// O2M relation, we need to find the other side and update the field, only if we didn't delete it before. Two options here: 1. This is a two sided relationship, which means that the field exists, 
			// 2. One sided relationship, the field FK is only in the DB.... Either way deleting from the DB will do the job!
			TableDefinition<?> tdef = define(fdef.relationDefinition.dataType);
			String pk = (factory.getPrimaryKey(parent) instanceof String) ? "'" + factory.getPrimaryKey(parent).toString() + "'" : factory.getPrimaryKey(parent).toString();
			StatementBuilder builder = new StatementBuilder("UPDATE ").append(tdef.tableName).append(" SET ").append(fdef.relationDefinition.relationFieldName).append("=null WHERE ");
			builder.append(fdef.relationDefinition.relationFieldName).append("=").append(pk);
			executeUpdate(builder.toString());
			return;
		}
		// relationTables exist both in O2M and M2M relations. In this case all we need to do is to remove all the entries in the table that include the parent
		if (fdef.relationDefinition.relationTableName != null) {
			String pk = (factory.getPrimaryKey(parent) instanceof String) ? "'" + factory.getPrimaryKey(parent).toString() + "'" : factory.getPrimaryKey(parent).toString();
			StatementBuilder builder = new StatementBuilder("DELETE FROM ").append(fdef.relationDefinition.relationTableName).append(" WHERE ");
			builder.append(fdef.relationDefinition.relationFieldName).append("=").append(pk);
			executeUpdate(builder.toString());
		}
	}
	
	/*
	 * Remove the relationship between a given child and its parent. If the cascade type is delete the child is also removed from the underlying storage.
	 * @param fdef
	 * @param child
	 */
	void deleteChildRelation(FieldDefinition fdef, Object child, Object parentPrimaryKey) {
		// delete depending on the cascade type
		if (fdef.relationDefinition.cascadeType == CascadeType.DELETE)
			delete(child);
		if (fdef.relationDefinition.relationTableName == null && fdef.relationDefinition.cascadeType != CascadeType.DELETE) {
			try {
				Field otherSideRelation = fdef.relationDefinition.dataType.getDeclaredField(fdef.relationDefinition.relationFieldName);
				otherSideRelation.setAccessible(true);
				otherSideRelation.set(child, null);
				otherSideRelation.setAccessible(false);
			}
			catch (Exception e) {
				throw new JaquError(e.getMessage(), e);
			}
		}
		// relationTables exist both in O2M and M2M relations. In this case all we need to remove a specific entry in the relation table.
		if (fdef.relationDefinition.relationTableName != null) {
			String pk = (factory.getPrimaryKey(child) instanceof String) ? "'" + factory.getPrimaryKey(child).toString() + "'" : factory.getPrimaryKey(child).toString();
			String pPk = (parentPrimaryKey instanceof String) ? "'" + parentPrimaryKey.toString() + "'" : parentPrimaryKey.toString();
			StatementBuilder builder = new StatementBuilder("DELETE FROM ").append(fdef.relationDefinition.relationTableName).append(" WHERE ");
			builder.append(fdef.relationDefinition.relationFieldName).append("=").append(pPk).append(" AND ").append(fdef.relationDefinition.relationColumnName);
			builder.append(" = ").append(pk);
			executeUpdate(builder.toString());
		}
	}
}
