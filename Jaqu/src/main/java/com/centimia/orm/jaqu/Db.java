/*
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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.transaction.Status;
import javax.transaction.SystemException;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Event;
import com.centimia.orm.jaqu.annotation.Immutable;
import com.centimia.orm.jaqu.annotation.MappedSuperclass;
import com.centimia.orm.jaqu.util.ClassUtils;
import com.centimia.orm.jaqu.util.FieldComperator;
import com.centimia.orm.jaqu.util.StatementBuilder;
import com.centimia.orm.jaqu.util.Utils;
import com.centimia.orm.jaqu.util.WeakIdentityHashMap;

/**
 * This class represents a connection to an underlying data store which supports JDBC.
 */
public class Db implements AutoCloseable {

	public static final int FULL_DEPTH = -1;
	
    private static final String UNABLE_TO_FILL_ROW = "Unable to fill row. Maybe resultSet is not of this object type?";
	private static final String COM_CENTIMIA_ORM = "com.centimia.orm";
	private static final String SET = " SET ";
	private static final String UPDATE = "UPDATE ";
	private static final String WHERE = " WHERE ";
	private static final String SESSION_IS_CLOSED = "IllegalState - Session is closed!!!";
	// used in scoping to show when we have no stack trace, although we should have as opposed to null that shows we didn't have one
	// because the call was not made.
	private static final StackTraceElement UNKNOWN_CALLER = new StackTraceElement("<unknown>", "<unknown>", null, -1);
	private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE); // a stack trace walker, more efficient way to explore stack
	
	/*
     * A list of objects this specific DB call has already visited. This list is cleared after each call.
     * Keeps a different list per thread.
     *
     * Key - The object reEntrant
     * Value - Last Parent who holds this object
     */
    final CacheManager reEntrantCache;

    /*
     * A list of objects this specific Db already fetched from the DB. This list survives multi calls to the same DB
     * but is cleared when the Db is closed. Keeps a different list per thread.
     */
    final CacheManager multiCallCache;

    protected JaquSessionFactory factory;

	// determines if the Db connection is closed. This gets value of true only when the underlying connection is closed on invalid
	private volatile boolean closed = true;

	private final Map<Object, Token> tokens = Collections.synchronizedMap(new WeakIdentityHashMap<Object, Token>());

	private Connection conn;

	private DbUtils utils;

	// for granular control of commit and close of this db session when no transaction exists.
	private boolean closeExternal;
	private boolean commitExternal;
	
	// The stack‑trace element of the method that called commitExternal()
    private StackTraceElement scopeChangeCaller = null;
    
	private boolean rollbackOnly = false;
	
    Db(Connection conn, JaquSessionFactory factory) {
        this.conn = conn;
        this.factory = factory;
        this.reEntrantCache = new CacheManager(factory);
        this.multiCallCache = new CacheManager(factory);
        this.closed = false;
    }

	/**
     * Insert the given object and all it's children to the DB.
     *
     * @param <T>
     * @param t
     */
    public <T> void insert(T t) {
    	this.insert(t, FULL_DEPTH);
    	clearReEntrentCache();
    }
    
    /**
     * Insert the given object and all it's children to the DB.
     * <p>
     * <b>Important Note about depth</b>:<br>
     * When running fluent api, the Db connection maintains a re-entrent cache of objects it has 
     * already processed during the current connection. So, if you retrieve an object tree with fluent api
     * and then make changes to the tree and use one of the (insert/update/merge) operations with depth
     * less then FULL_DEPTH, the data will be processed correctly in the DB. However, if you then try to
     * re-fetch the same object tree with fluent api, you may get objects from the re-entrent cache that hold
     * a previous state of the object. To illustrate:
     * <pre>
     * // say you have an Object A that holds an Object B that holds an Object C.
     * 
     * ObjectA a = new ObjectA();
     * ObjectB b = new ObjectB();
     * ObjectC c = new ObjectC();
     * a.setB(b);
     * b.setC(c);
     * 
     * db.insert(a); // inserts A, B and C in the DB (full depth)
     * 
     * ObjectA aFromDb = new ObjectA();
     * aFromDb = db.from(aFromDb).primaryKey().is(a.getId()).selectFirst(); // retrieves A, B and C from the DB
     * // in the multiCall cache we now have A, B and C
     * 
     * a.name = "new name for A";
     * a.getB().name = "new name for B";
     * a.getB().getC().name = "new name for C";
     * 
     * db.update(a, 1) // updates A and B in the DB, C is not updated
     * // in the 'a' object tree, however, c has a new name.
     * // in the 'aFromDb' object tree, c has the old name.
     * 
     * ObjectA a1 = new ObjectA();
     * a1 = db.from(a1).primaryKey().is(1).selectFirst(); // retrieves A, B and C from the DB
     * // however, this connection cached A, B and C in the multiCall cache on the previous 'select' fluent api call
     * assertEquals("new name for A", a1.getName()); // will fail because the cached A has the old name, even though the DB has the new name
     * 
     * // if we change aFormDb instead and update then the cache will be updated to the objects state but the DB
     * // will have a different state for C.
     * </pre>
     * Jaqu assumes that the latter course of action is not the common one. However, if you do run into this you
     * must close and reopen the connection to clear the multi call cache.
     * </p>
     * @param <T>
     * @param t
     * @param depth - the depth of the object tree to insert (Db.FULL_DEPTH for full depth)
     */
    @SuppressWarnings("unchecked")
	public <T> void insert(T t, int depth) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	t = checkSession(t);
        Class<?> clazz = t.getClass();
        TableDefinition<?> definition = define(clazz);
        if (null != definition.getInterceptor())
        	definition.getInterceptor().onInsert(t);
        definition.insert(this, t, depth);
        clearReEntrentCache();
    }

    /**
     * Inserts the object and returns the top most Object in the hierarcy's primary key, generated or not.
     *
     * @param <T>
     * @param <X>
     * @param t
     * @return X the primary key.
     * @throws JaquError when no primary keys exists or more then one primary key exists or when the object inserted and primary key could not be retrieved
     * @throws RuntimeException (could also be a JaquError) when insert failed.
     */
    public <T,X> X insertAndGetPK(T t) {
		X pk = insertAndGetPK(t, FULL_DEPTH);
		clearReEntrentCache();
        return pk;
	}
    
    /**
     * Inserts the object and returns the top most Object in the hierarcy's primary key, generated or not.
     *
     * @param <T>
     * @param <X>
     * @param t
     * @param depth - the depth of the object tree to insert (Db.FULL_DEPTH for full depth)
     * @return X the primary key.
     * @throws JaquError when no primary keys exists or more then one primary key exists or when the object inserted and primary key could not be retrieved
     * @throws RuntimeException (could also be a JaquError) when insert failed.
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
	@SuppressWarnings("unchecked")
	public <T,X> X insertAndGetPK(T t, int depth) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	t = checkSession(t);
        Class<T> clazz = (Class<T>) t.getClass();
        TableDefinition<T> td = define(clazz);

        List<FieldDefinition> primaryKeys = td.getPrimaryKeyFields();
        if (null == primaryKeys || primaryKeys.isEmpty())
        	throw new JaquError("Object {%s} has no primary keys defined", t.getClass().getName());

        if (primaryKeys.size() > 1)
        	throw new JaquError("NOT SUPPORTED! - Can not return a key for an Object {%s} with more then one primary key defined!!!", t.getClass().getName());
        // test for intercepter.
        if (null != td.getInterceptor())
        	td.getInterceptor().onInsert(t);
        td.insert(this, t, depth);
        primaryKeys.get(0).field.setAccessible(true);
        X pk = null;
		try {
			pk = (X) primaryKeys.get(0).field.get(t);
		}
		catch (JaquError je) {
			throw je;
		}
		catch (Exception e) {
			// unable to retrieve the key, however the object was inserted to the db so we return anyway but with null
			throw new JaquError(e, e.getMessage());
		}
		clearReEntrentCache();
        return pk;
    }

	/**
	 * Utility method to get the primary key of an existing entity or table
	 * Can also be called from the factory class.
	 *
	 * see com.centimia.orm.jaqu.JaquSessionFactory#getPrimaryKey(Object)
	 * @param <T>
	 * @param <X>
	 * @param t
	 * @return X
	 */
	public <T, X> X getPrimaryKey(T t) {
		if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
		return factory.getPrimaryKey(t);
	}

    /**
     * Insert objects in a comma delimited array of 0..n
     *
     * @param <T>
     * @param depth - the depth of the object tree to insert (Db.FULL_DEPTH for full depth)
     * @param tArray
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    @SuppressWarnings("unchecked")
	public <T> void insert(int depth, T ... tArray) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t : tArray) {
            insert(t, depth);
        }
    	clearReEntrentCache();
    }

    /**
     * Insert all objects on the list
     *
     * @param <T>
     * @param list
     * @param depth - the depth of the object tree to insert (Db.FULL_DEPTH for full depth)
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    public <T> void insertAll(List<T> list, int depth) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
        for (T t : list) {
            insert(t, depth);
        }
        clearReEntrentCache();
    }

    /**
     * Merge means that if the object exists it is updated (so are all his children), if not it is inserted (so are all his children)
     *
     * @param <T>
     * @param t
     */ 
    public <T> void merge(T t) {
    	this.merge(t, FULL_DEPTH);
    	clearReEntrentCache();
    }
    
    /**
     * Merge means that if the object exists it is updated (so are all his children), if not it is inserted (so are all his children)
     *
     * @param <T>
     * @param t
     * @param depth - the depth of the object tree to merge (Db.FULL_DEPTH for full depth)
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    @SuppressWarnings("unchecked")
	public <T> void merge(T t, int depth) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	t = checkSession(t);
        Class<?> clazz = t.getClass();
        TableDefinition<?> definition = define(clazz);
        if (null != definition.getInterceptor())
        	definition.getInterceptor().onMerge(t);
        definition.merge(this, t, depth);
        clearReEntrentCache();
    }

    /**
     * merge all the given objects of the same type. They are merged in the order in which they are iterated on within the list
     *
     * @param <T>
     * @param list
     * @param depth - the depth of the object tree to merge (Db.FULL_DEPTH for full depth)
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    public <T> void merge(List<T> list, int depth) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t: list){
    		merge(t, depth);
    	}
    	clearReEntrentCache();
    }

    /**
     *  merge all the given objects of the same type. They are merged in the order in which they are given.
     *
     * @param <T>
     * @param depth - the depth of the object tree to merge (Db.FULL_DEPTH for full depth)
     * @param tArray
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    @SuppressWarnings("unchecked")
	public <T> void merge(int depth, T ... tArray) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t: tArray){
    		merge(t, depth);
    	}
    	clearReEntrentCache();
    }

    /**
     * Delete the immediate given object. If the object has a relationships, the link is always updated according to the cascade type.
     * <b>Note: </b> This delete works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).where()....delete();
     * </pre>
     *
     * @param <T>
     * @param t
     */
	public <T> void delete(T t) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	if (null == factory.getPrimaryKey(t))
    		// if I don't have a primary key I can't delete the object, don't know how
    		return;
    	t = checkSession(t);
    	Class<?> clazz = t.getClass();
    	TableDefinition<?> tdef = define(clazz);
    	tdef.delete(this, t);
    	clearReEntrentCache();
    }

    /**
     * Delete all the given objects of the same type. Deletes in the order the given list is iterated upon.
     * <b>Note: </b> This delete works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).where()....delete();
     * </pre>
     *
     * @param <T>
     * @param list
     */
    public <T> void delete(List<T> list) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t: list){
    		delete(t);
    	}
    	clearReEntrentCache();
    }

    /**
     * Delete all the given objects of the same type. Deletes in the order they given.
     * <b>Note: </b> This delete works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).where()....delete();
     * </pre>
     *
     * @param tArray
     */
    @SuppressWarnings("unchecked")
	public <T> void delete(T ... tArray) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t: tArray){
    		delete(t);
    	}
    	clearReEntrentCache();
    }

    /**
     * Deletes all elements from the table.
     * <b>Note></b> this utility method simply executes "delete from [TableName]". It runs without a session on the connection.
	 * the method is not compliant with cascade and will throw an exception when unable to delete due to constraints.
     * <p>
     * <b>Note - this utility method does not clear related fields. This may leave foreign keys in related objects, pointing
     * to non existing objects.</b>
     *
     * @param clazz
     * @return int - num of elements deleted
     */
    public <T> int deleteAll(Class<T> clazz) {
    	TableDefinition<?> definition = define(clazz);
    	return definition.deleteAll(this);
    }

    /**
     * Updates the immediate given object. If the object has a relationship, the link is always updated as needed. In case where the link
     * is to a non persisted entity the new entity is inserted into the DB.
     * <b>Note: </b> This update works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).set()....update();
     * </pre>
     *
     * @param <T>
     * @param t
     */
    public <T> void update(T t) {
    	this.update(t, FULL_DEPTH);
    	clearReEntrentCache();
    }
    
    /**
     * Updates the immediate given object. If the object has a relationship, the link is always updated as needed. In case where the link
     * is to a non persisted entity the new entity is inserted into the DB.
     * <b>Note: </b> This update works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).set()....update();
     * </pre>
     *
     * @param <T>
     * @param t
     * @param depth - the depth of the object tree to update (Db.FULL_DEPTH for full depth)
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    @SuppressWarnings("unchecked")
	public <T> void update(T t, int depth) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	if (null == factory.getPrimaryKey(t))
    		// if I don't have a primary key I can't update the object, don't know how
    		return;
    	t = checkSession(t);
        Class< ? > clazz = t.getClass();
        TableDefinition<?> definition = define(clazz);
        if (null != definition.getInterceptor())
        	definition.getInterceptor().onUpdate(t);
        definition.update(this, t, depth);
        clearReEntrentCache();
    }

	/**
     * Update all the given objects of the same type. Updates in the order the given list is iterated upon.
     * <b>Note: </b> This update works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).set()....update();
     * </pre>
     *
     * @param <T>
     * @param list
     * @param depth - the depth of the object tree to update (Db.FULL_DEPTH for full depth)
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    public <T> void update(List<T> list, int depth){
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t: list){
    		update(t, depth);
    	}
    	clearReEntrentCache();
    }

    /**
     * Update all the given objects of the same type. Updates in the order they are given.
     * <b>Note: </b> This update works for Entities, and objects with mapped primary keys. For a general from update use the SQL like format
     * <pre>
     * 	db.from(T).set()....update();
     * </pre>
     *
     * <T>
     * @param depth - the depth of the object tree to update (Db.FULL_DEPTH for full depth)
     * @param tArray
     * @see {@link Db#insert(Object, int)} for important note about depth and re-entrent cache.
     */
    @SuppressWarnings("unchecked")
	public <T> void update(int depth, T ... tArray){
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	for (T t: tArray){
    		update(t, depth);
    	}
    	clearReEntrentCache();
    }

    /**
     * The query will be built according to all immediate fields (not including relationships) that have value within the example object.
     *
     * @param example
     * @return List<T>
     */
    public <T> List<T> selectByExample(T example){
    	return selectByExample(example, new BasicExampleOptions(example, this));
    }

    /**
     * Select by example using a builder object result type mapping. The example is built based on {@link BasicExampleOptions}.
     *
     * @param example
     * @param result
     * @return List<Z>
     */
    public <T, Z> List<Z> selectByExample(T example, Z result) {
    	QueryWhere<T> select = getExampleQuery(example, new BasicExampleOptions(example, this));
    	if (null == select)
    		// this means that the child entity was not flagged out, and nothing was found to match the example
			// therefore we must conclude that there is no match for our search.
    		return new ArrayList<>();
    	return select.select(result);
    }

    /**
     * Select by example using a builder object result type mapping. The example is built based on the given example options.
     *
     * @see GeneralExampleOptions
     * @see BasicExampleOptions
     * @param example
     * @param result
     * @param params
     * @return List<Z>
     */
    public <T, Z> List<Z> selectByExample(T example, Z result, ExampleOptions params) {
    	QueryWhere<T> select = getExampleQuery(example, params);
    	if (null == select)
    		// this means that the child entity was not flagged out, and nothing was found to match the example
			// therefore we must conclude that there is no match for our search.
    		return new ArrayList<>();
    	return select.select(result);
    }

    /**
     * The query will be built according to the 'example' object and the options given.
     * <b>Noet: </b> values in O2M, M2M, M2O relationships are disregarded and set as an example in the select. To select on relationships write your own selects
     *
     * @see ExampleOptions
     * @param example
     * @param params
     * @return List<T>
     */
    public <T> List<T> selectByExample(T example, ExampleOptions params) {
    	QueryWhere<T> select = getExampleQuery(example, params);
    	if (null == select)
    		// this means that the child entity was not flagged out, and nothing was found to match the example
			// therefore we must conclude that there is no match for our search.
    		return new ArrayList<>();
    	return select.select();
    }

    /**
     * Returns a single object based on the given class built from the given result set.
     * <b>Note: </b> The resultSet is not closed by this method, you need to close it yourself!
     * 
     * &lt;T&gt;
     * @param rs - the result set
     * @param clazz - the return type expected
     * @return T or null if no results were found.
     * @throws JaquError when there is a mismatch between the resultSet and the object
     */
    public <T> T selectFirstByResultSet(ResultSet rs, Class<T> clazz){
    	List<T> results = selectByResultSet(rs, clazz);
    	if (!results.isEmpty())
    		return results.get(0);
    	return null;
    }

    /**
     * Returns the objects based on the given class built from the given result set.
     * <b>Note: </b> The resultSet is not closed by this method, you need to close it yourself!
     *
     * &lt;T&gt;
     * @param rs - the result set
     * @param clazz - the return type expected
     * @return List&lt;T&gt; or empty list if nothing was found
     * @throws JaquError when there is a mismatch between the resultSet and the object
     */
    public <T> List<T> selectByResultSet(ResultSet rs, Class<T> clazz) {
    	List<T> result = Utils.newArrayList();
    	if (Utils.isSimpleType(clazz)) {    		
    		try {
                while (rs.next()) {
                	Types type = Types.valueOf(clazz.getSimpleName().toUpperCase());
                    @SuppressWarnings("unchecked")
					T item = (T) factory.getDialect().getValueByType(type, rs, 1);
                    result.add(item);
                }
            }
            catch (SQLException e) {
                throw new JaquError(e, e.getMessage());
            }
        	catch (Exception e) {
        		throw new JaquError(UNABLE_TO_FILL_ROW, e);
        	}
    	}
    	else {
    		TableDefinition<T> def = JaquSessionFactory.define(clazz, this, false);
	    	try {
	            while (rs.next()) {
	                T item = def.readRow(rs, this);
	                this.addSession(item);
	                result.add(item);
	            }
	        }
	        catch (SQLException e) {
	            throw new JaquError(e, e.getMessage());
	        }
	    	catch (Exception e) {
	    		throw new JaquError(UNABLE_TO_FILL_ROW, e);
	    	}
    	}
        return result;
    }

    /**
     * Returns a single object based on the given class built from the current line within the given result set.
     * <b>Note: </b> The resultSet is not closed by this method, you need to close it yourself!
     *
     * &lt;T&gt;
     * @param rs - the result set
     * @param clazz - the return type expected
     * @return List&lt;T&gt; or empty list if nothing was found
     * @throws JaquError when there is a mismatch between the resultSet and the object
     */
    @SuppressWarnings("unchecked")
	public <T> T selectSingleByResultSet(ResultSet rs, Class<T> clazz) {
    	if (Utils.isSimpleType(clazz)) {    		
    		try {
            	Types type = Types.valueOf(clazz.getSimpleName().toUpperCase());
				return (T) factory.getDialect().getValueByType(type, rs, 1);
            }
            catch (SQLException e) {
                throw new JaquError(e, e.getMessage());
            }
        	catch (Exception e) {
        		throw new JaquError(UNABLE_TO_FILL_ROW, e);
        	}
    	}
    	else {
    		TableDefinition<T> def = JaquSessionFactory.define(clazz, this, false);
	    	try {
                T item = def.readRow(rs, this);
                this.addSession(item);
                return item;
	        }
	    	catch (Exception e) {
	    		throw new JaquError(UNABLE_TO_FILL_ROW, e);
	    	}
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
    		throw new JaquError(SESSION_IS_CLOSED);
        return Query.from(this, alias);
    }

    /**
     * API for creating a table from a given object. If the object is annotated with entity the relation's (if exist) tables are also created
     * @param <T>
     * @param clazz
     */
    public <T> void createTable(Class<T> clazz) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
        define(clazz);
    }

    /**
     * When working in nested method calls in which any of the inner method calls may also use the same thread attached connection to the Db.
     * i.e
     * <pre>
     * 	methodA {
     * 		try (Db db = sessionFactory.getSession()) {
     * 			try {
     * 				... do some db work
     * 				methodB();
     * 				... do some more db work
     * 				db.commit();
     * 			}
     * 			catch (Exception any) {
     * 				db.rollback();
     * 			}
     * 		}
     * 	}
     * 
     * 	methodB {
     * 		try (Db db = sessionFactory.getSession()) {
     * 			try {
     * 				... do some db work
     * 				db.commit();
     * 			}
     * 			catch (Exception any) {
     * 				db.rollback();
     * 			}
     * 		}
     * 	}
     * </pre>
     * Since JaQu handles the connection and each 'getSession()' call returns the same thread attached Db instance, sometimes,
     * you want each method to be completely Db independent. The solution is easy, instead of calling of {@link JaquSessionFactory#getSession()} just call {@link JaquSessionFactory#newLocalSession()}.
     * However, in many business scenarios you may want to control the commit order of things, even though you are calling different methods. To achieve this
     * when not running under an external Transaction Manager (JTA) you can use scoping.
     * <p>
     * Once you set a scope in the outer method the connection will <b>only</b> be closed when closing it in the outer method. When you set the 'externalCommit' to 'true'
     * from that point and on 'commit' will not be applied until you call {@link #resetScope()}. Note that calling 'resetScope' should only occur on the most outer method.
     * <p>So the above method could look like this:
     * <pre>
     * 	methodA {
     * 		try (Db db = sessionFactory.getSession().applyScope(true) {
     * 			try {
     * 				... do some db work
     * 				methodB();
     * 				... do some more db work
     * 				db.resetExternal().commit();
     * 			}
     * 			catch (Exception any) {
     * 				db.resetExternal().rollback();
     * 			}
     * 		}
     * 	}
     * 
     * 	methodB {
     * 		try (Db db = sessionFactory.getSession()) {
     * 			try {
     * 				... do some db work
     * 				db.commit();
     * 			}
     * 			catch (Exception any) {
     * 				db.rollback();
     * 			}
     * 		}
     * 	}
     * </pre>
     * In this example work done in methodB will not be committed until commit is issued in methodA. The connection will be close when exiting the try with resources
     * in methodA.
     * <p>
     * Since managing scope on nested methods can be complicated to follow this method records its original caller.
     * <p>
     * @param externalCommit set to true to control commit from the outer method.
     * @see JaquSessionFactory#newLocalSession()
     * @return Db
     */
    public Db applyScope(boolean externalCommit) {
    	if (this.commitExternal && !externalCommit) {
    		// we can't change commit state from 'true' to 'false', only from 'false' to 'true'. The reason is that if an external call declared
    		// that it will manage commit, an internal is not allowed to change this decision.
    		String msg = String.format("This scope is managed externally from %s. You can not set values which were scoped previously in an outer scope back to unscoped "
    				+ "unless you are within the same outer scope.\n"
    				+ "To reset the scope use resetScope method!!! If you need to specifically commit a connection in an internal method scope "
    				+ "use a different connection by calling 'newLocalSession' on the factory.", scopeChangeCaller);
    		throw new JaquError(msg);
    	}
   		this.commitExternal = externalCommit;
    	
    	if (!closeExternal) {
	    	// the following line records the caller of the applyScope so that we can tell "who" applied scope to begin with.
    		// only when closeExternal is still false on this call we are at the first caller.
	   		scopeChangeCaller = WALKER.walk(stream -> stream.skip(2)
	   								.map(StackWalker.StackFrame::toStackTraceElement)
	   								.findFirst()
	   								.orElse(UNKNOWN_CALLER));
    	}
   		this.closeExternal = true;
   		return this;
    }

    /**
     * returns true when this session is scoped. It is sometimes beneficial
     * to know if the session is scoped before deciding if to apply a scope or not.
     * 
     * @return boolean
     */
    public boolean isScoped() {
    	return closeExternal || commitExternal;
    }
    
    /**
     * resets the external commit and external close to false.
     * <b>SPECIAL CARE</b> When executing this method be absolutely sure you are in the most outer scope. (i.e the method where the scope was opened)
     *
     * @return Db
     */
    public Db resetScope() {
    	this.closeExternal = false;
    	this.commitExternal = false;
    	this.scopeChangeCaller = null;
    	return this;
    }

    /**
     * Rollback the underlying connection.
	 * <p>
	 * <b>Note</b> that if "commitExternal" is true, after rolling back you should notify the enclosing caller of the rollback.
     */
    public void rollback() {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
		try {
			try {
				this.rollbackOnly = true;
				if (null != this.factory.tm && null != this.factory.tm.getTransaction() && 
					hasRunningTransaction(this.factory.tm.getTransaction().getStatus())) {
						
					try {
						this.factory.tm.getTransaction().setRollbackOnly();
						return;
					}
					catch (IllegalStateException e) {
						StatementLogger.error("trying to roll back transaction when it is not allowed [" + e.getMessage() + "]");
					}
					catch (SystemException e) {
						StatementLogger.error("unable to mark connection for rollback for an unknown reason. [" + e.getMessage() + "]");
					}
				}
			}
			catch (SystemException e) {
				StatementLogger.error("unable to get status on transaction for an unknown reason. [" + e.getMessage() + "]");
			}
			catch (NullPointerException npe) {
				// Just in case, this should not happen!
				// this means that there is no transaction (getTransaction() was null)
			}
			// we will reach here if there is no transaction
			this.conn.rollback();
		}
		catch (SQLException e) {
			// can't rollback nothing can be done!!!
			throw new JaquError(e, "Unable to rollback session's underlying connection because --> {%s}", e.getMessage());
		}
	}

	/**
     * Commit the underlying connection
     */
	public void commit() {
		if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
		try {
			try {
				if (commitExternal)
					return;
				if (rollbackOnly) {
					rollback();
					return;
				}
				if (null != this.factory.tm && null != this.factory.tm.getTransaction()
					&& hasRunningTransaction(this.factory.tm.getTransaction().getStatus())) {
					// if we're in a running transaction it is up to the transaction manager to commit not me.
					return;
				}
			}
			catch (SystemException e) {
				StatementLogger.error("unable to get status on transaction for an unknown reason. [" + e.getMessage() + "]");
			}
			catch (NullPointerException npe) {
				// this means that there is no transaction (getTransaction() was null)
			}
			this.conn.commit();
		}
		catch (SQLException e) {
			// can't commit nothing can be done!!!
			throw new JaquError(e, "Unable to commit session's underlying connection because --> {%s}", e.getMessage());
		}
	}

    /**
     * Closes the session and underlying db connection
     */
    @Override
	public void close() {
        if (!closed) {
        	try {
        		if (closeExternal)
        			return;

        		// if we're in a running transaction that has not been committed it is not up to me to close the connection
        		if (null != this.factory.tm && null != this.factory.tm.getTransaction()) {
        			int status = this.factory.tm.getTransaction().getStatus();
        			if (hasRunningTransaction(status) && status != Status.STATUS_COMMITTED)
        				return;
        		}
			}
			catch (SystemException e) {
				StatementLogger.error("unable to get transaction status for an unknown reason. [" + e.getMessage() + "]");
			}
        	catch (NullPointerException npe) {
				// this means that there is no transaction (getTransaction() was null)
			}
        	try {
        		if (null != utils) {
					utils.clean();
				}
        		conn.close();
        		if (StatementLogger.isDebugEnabled())
        			StatementLogger.debug("closing connection " + conn.toString());
        	}
        	catch (SQLException e) {
	            throw new JaquError(e, "Unable to close session's underlying connection because --> {%s}", e.getMessage());
	        }
        	finally {
        		this.factory.removeSession();
	        	clean();
	        }
        }
    }

    /**
     * true if this connection had been closed
     * @return boolean
     */
    public boolean isClosed() {
		return this.closed;
	}

    /**
     * Run a SQL query directly against the database.
     *
     * @param sql the SQL statement
     * @param processor
     * @param args
     * @return the result set
     */
    public <T> T executeQuery(String sql, IResultProcessor<T> processor, Object ... args) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	
    	try (PreparedStatement stmnt = this.prepare(sql)) {
    		if (null != args && 0 < args.length) {
				for (int i = 0; i < args.length; i++){
					stmnt.setObject(i + 1, args[i]); // +1 is because parameters in database APIs start with 1 not with 0
				}
	    	}
    		if (factory.isShowSQL())
    			StatementLogger.select(sql);
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
	    		return processor.processResult(rs);
	    	}
    	}
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }

    /**
     * A utility method that executes the String query and returns the result class
     *
     * @param sql
     * @param clazz
     * @paran args
     *
     * @return List<T>
     */
    public <T> List<T> executeQuery(String sql, Class<T> clazz, Object ... args) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	
    	if (factory.isShowSQL()) {
			StatementLogger.select(sql);
    	}
    	
    	return executeQuery(sql, rs -> {
    		if (null != rs) {
        		return selectByResultSet(rs, clazz);
        	}
    		return Utils.newArrayList();
    	}, args);
    }

    /**
     * A utility method that executes the String query and processes the result, per result, as an object of the given class.
     * 
     * @param sql
     * @param processor
     * @param clazz
     * @param args
     * @return List&lt;K&gt;
     */
    public <T, K> List<K> executeQuery(String sql, Function<T, K> processor, Class<T> clazz, Object ... args) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	
    	try (PreparedStatement stmnt = this.prepare(sql)) {
    		if (null != args && 0 < args.length) {
				for (int i = 0; i < args.length; i++) {
					stmnt.setObject(i + 1, args[i]); // +1 is because parameters in database APIs start with 1 not with 0
				}
	    	}
    		if (factory.isShowSQL())
    			StatementLogger.select(sql);
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
	    		List<K> result = new ArrayList<>();
	    		while (rs.next()) {
	    			T item = selectSingleByResultSet(rs, clazz);
                    result.add(processor.apply(item));
	    		}
	    		return result;
	    	}
    	}
    	catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }
    
    /**
     * A utility method that executes the pre-configured prepared statement returns the result class
     * <p>
     * It is possible to obtain a {@link PreparedStatement} for this connection from utils.
     * </p>
     * <p>
     * <b>Note: the method does not close the prepared statement once done</b>
     * </p>
     * 
     * @param stmnt
     * @param clazz
     * @return List<T>
     */
    public <T> List<T> executeQuery(PreparedStatement stmnt, Class<T> clazz) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	
    	try (ResultSet rs = stmnt.executeQuery()) {
    		this.multiCallCache.clearReEntrent();
    		return selectByResultSet(rs, clazz);
    	}
		catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
    }
    
    
    /**
     * A utility method that executes a pre-configured prepared statement and processes the result, per result, as an object of the given class.<br>
     * <p>
     * It is possible to obtain a {@link PreparedStatement} for this connection from utils.
     * </p>
     * <p>
     * <b>Note: the method does not close the prepared statement once done</b>
     * </p>
     * 
     * @param <T>
     * @param <K>
     * @param stmnt
     * @param processor
     * @param clazz
     * @return List<K>
     */
    public <T, K> List<K> executeQuery(PreparedStatement stmnt, Function<T, K> processor, Class<T> clazz) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	
    	try {
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
	    		List<K> result = new ArrayList<>();
	    		while (rs.next()) {
	    			T item = selectSingleByResultSet(rs, clazz);
                    result.add(processor.apply(item));
	    		}
	    		return result;
	    	}
    	}
    	catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }
    
    /**
     * A utility that builds a callable statement out of the given string, sets the arguments calls the statement and returns the object values.
     * the callable statements should look like '{call doCallable (?, ?)}'
     *
     * <b>Note</b> Since update executes without objects but effects the state of the db jaqu can no longer insure
	 * that the objects taken from the db before are still valid so MultiCache is cleared. Objects will no longer be the same instance if fetched again</b>
     *
     * @param callableStmnt
     * @param clazz
     * @param args
     * @return List&lt;T&gt;
     */
    public <T> List<T> executeCallable(String callableStmnt, Class<T> clazz, Object ... args) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	try (CallableStatement stmnt = this.prepareCallable(callableStmnt)) {
    		if (null != args && 0 < args.length) {
				for (int i = 0; i < args.length; i++) {
					stmnt.setObject(i + 1, args[i]); // +1 is because parameters in database APIs start with 1 not with 0
				}
	    	}
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
				return selectByResultSet(rs, clazz);
	    	}
    	}
		catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
    }

    /**
     * A utility that builds a callable statement out of the given string, sets the arguments calls the statement and returns the processed object values.
     * the callable statements should look like '{call doCallable (?, ?)}'
     *
     * <b>Note</b> Since update executes without objects but effects the state of the db jaqu can no longer insure
	 * that the objects taken from the db before are still valid so MultiCache is cleared. Objects will no longer be the same instance if fetched again</b>
     *
     * @param callableStmnt
     * @param clazz
     * @param args
     * @return List&lt;T&gt;
     */
    public <T, K> List<K> executeCallable(String callableStmnt, Function<T, K> processor, Class<T> clazz, Object ... args) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	try (CallableStatement stmnt = this.prepareCallable(callableStmnt)) {
    		if (null != args && 0 < args.length) {
				for (int i = 0; i < args.length; i++) {
					stmnt.setObject(i + 1, args[i]); // +1 is because parameters in database APIs start with 1 not with 0
				}
	    	}
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
	    		List<K> result = new ArrayList<>();
	    		while (rs.next()) {
	    			T item = selectSingleByResultSet(rs, clazz);
	    			result.add(processor.apply(item));
	    		}
	    		return result;
	    	}
    	}
    	catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
    }
    
    /**
     * A utility that calls the pre-configured callable statement and returns the object values.<br>
     *
     * <p>
     * It is possible to obtain a {@link CallableStatement} for this connection from utils.
     * </p>
     * <p>
     * <b>Note:
     * <ul>
     * 	<li> the method does not close the callable statement once done</li>
     *  <li>Since update executes without objects but effects the state of the db jaqu can no longer insure
	 * that the objects taken from the db before are still valid so MultiCache is cleared. Objects will no longer be the same instance if fetched again</li>
     * </ul>
     * </b>
     * </p>
     * 
     * @param callableStmnt
     * @param clazz
     * @param args
     * @return List&lt;T&gt;
     */
    public <T> List<T> executeCallable(CallableStatement stmnt, Class<T> clazz) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	try {
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
				return selectByResultSet(rs, clazz);
	    	}
    	}
		catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
    }
    
    /**
     * A utility that calls the pre-confidured callable statement and returns the processed object values.
     *
     * <p>
     * It is possible to obtain a {@link CallableStatement} for this connection from utils.
     * </p>
     * <p>
     * <b>Note:
     * <ul>
     * 	<li> the method does not close the callable statement once done</li>
     *  <li>Since update executes without objects but effects the state of the db jaqu can no longer insure
	 * that the objects taken from the db before are still valid so MultiCache is cleared. Objects will no longer be the same instance if fetched again</li>
     * </ul>
     * </b>
     * </p>
     * 
     * @param callableStmnt
     * @param clazz
     * @return List&lt;T&gt;
     */
    public <T, K> List<K> executeCallable(CallableStatement stmnt, Function<T, K> processor, Class<T> clazz) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	try {
	    	try (ResultSet rs = stmnt.executeQuery()) {
	    		this.multiCallCache.clearReEntrent();
	    		List<K> result = new ArrayList<>();
	    		while (rs.next()) {
	    			T item = selectSingleByResultSet(rs, clazz);
	    			result.add(processor.apply(item));
	    		}
	    		return result;
	    	}
    	}
    	catch (SQLException e) {
			throw new JaquError(e, e.getMessage());
		}
    }
    
    /**
     * Run an update query directly on the database
     * <p>
     * <b>Note</b> Since update executes without objects but effects the state of the db jaqu can no longer insure
	 * that the objects taken from the db before are still valid so MultiCache is cleared. Objects will no longer be the same instance if fetched again</b>
	 *
     * @param preparedStmnt
     * @param args
     * @return int
     */
    public int executeUpdate(String preparedStmnt, Object ... args) {
    	return executeUpdate(true, preparedStmnt, args);
    }

    /**
     * Run a SQL statement directly against the database.
     * <p>
     * <b>Note</b> Since update executes without objects but effects the state of the db jaqu can no longer insure
	 * that the objects taken from the db before are still valid so MultiCache is cleared. Objects will no longer be the same instance if fetched again</b>
	 *
     * @param sql the SQL statement
     * @return the update count
     */
    public int executeUpdate(String sql) {
    	return executeUpdate(true, sql);
    }

    /**
     * Check if the {@link Entity} is part of this live session. If not attach it to this session.
     * @param <T> - must be annotated as {@link Entity} to have any session attachment effect
     * @param t
     * @return &lt;T&gt; t - returns the same instance or an equal instance that was previously saved in cache
     */
    @SuppressWarnings("unchecked")
	public <T> T checkSession(T t) {
    	try {
			if (null != t.getClass().getAnnotation(Entity.class)  || null != t.getClass().getAnnotation(MappedSuperclass.class)) {
				// instrument this instance of the class
				Field dbField = t.getClass().getField("db");
				dbField.setAccessible(true);

				// put the open connection on the object. As long as the connection is open calling the getter method on the 'obj' will produce the relation
				Object o = dbField.get(t);
				if (!this.equals(o)) {
					Object attached = attach(t);
					multiCallCache.prepareReEntrent(attached);
					if (attached != t)
						return (T)attached;
				}
			}
			return t;
		}
		catch (Exception e) {
			throw new JaquError(e, e.getMessage());
		}
    }

	/**
	 * Add a Db Session to the Entity class. Object must be annotated with @Entity
	 * <p>
	 * <b>note:
	 * <ol>
	 * <li>unlike {@link #checkSession(Object)} this does not attach the object, 
	 * i.e no list merging is performed.</li>
	 * <li>it does not go through the object tree and does not add a session to the relationships</li>
	 * </ol>
	 * </b>
	 * This method is used mostly internally but is quicker for entities that do not need list merging.
	 * 
	 * @param t &lt;T&gt;
	 */
	public <T> void addSession(T t) {
		try {
			if (null != t.getClass().getAnnotation(Entity.class) || null != t.getClass().getAnnotation(MappedSuperclass.class)) {
				// instrument this instance of the class
				Field dbField = t.getClass().getField("db");
				dbField.setAccessible(true);
				// put the open connection on the object. As long as the connection is open calling the getter method on the 'obj' will produce the relation
				dbField.set(t, this);
			}
		}
		catch (Exception e) {
			throw new JaquError(e, e.getMessage());
		}
	}

	public DbUtils utils() {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);

		if (null == this.utils)
			this.utils = new DbUtils(this);
		return this.utils;
	}

	/**
	 * use this method for cases when your entity holds an 'Entity' relationship but you want to test conditions compared with the primary key and not
	 * the Entity Object.
	 *
	 * @param <T>
	 * @param <A>
	 * @param o
	 * @return GenericMask&lt;T, A&gt;
	 */
	public static <T, A>  GenericMask<T, A> asPrimaryKey(final T o, final Class<A> primaryKeyClass) {
		return new GenericMask<>(o, primaryKeyClass);
	}

    /**
     * If the Object is not in a synchronized connection to the DB this method synchronizes the object into the session.
     * After synchronization is done, all the deleted relations are deleted according to the cascadeType. To save the Object to the DB
     * {@link #update} should be called on the object!
     *
     * <p>
     * <b>[Notice], This method performs a delete to the underlying persistence store!!! Be very careful when merging Table Objects. It is very recommended to do work on the object
     * within the session and detach them for display purposes. </b>
     *
     * @param t
     */
    @SuppressWarnings({ "unchecked" })
	Object attach(Object t) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
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
			throw new JaquError(e, "PK Not accessible!!!");
		}
		// make sure this object is synchronized with the cache
		if (null != pk) {
			Object o = multiCallCache.checkReEntrent(t.getClass(), pk);
			if (null != o) {
				// this object exists in cache.
				if (o != t && null == t.getClass().getAnnotation(Immutable.class)) {
					// the cached object is not the same instance as the new one being attached so we check which instance we use.
					// general rule is that we use the latest we get unless the version cached is later then the new.
					if (null != tdef.version) {
						// compare versions in order to determine which is the one we keep
						Long verCache = (Long) tdef.version.getValue(o);
						Long verNew = (Long) tdef.version.getValue(t);
						if (null == verCache || (null != verNew && verNew >= verCache)) {
							multiCallCache.removeReEntrent(o, pk);
							o = null;
						}
					}
					else {
						// since it does not have a managed version we have no way of knowing which is the latest
						// therefore we use the latest we received. It is up to the developer to manage this.
						multiCallCache.removeReEntrent(o, pk);
						o = null;
					}
				}
				if (null != o)
					return o;
			}
		}
    	for (FieldDefinition fdef: tdef.getFields()) {
    		try {
				switch (fdef.fieldType) {
					case M2O:
					case NORMAL: continue;
					case FK: {
						fdef.field.setAccessible(true);
						Object o = fdef.field.get(t);
						if (o != null) {
							if (fdef.noUpdateField) {
								// only need to set the 'db' but we will not be working on this object so don't need to attach the rest
								this.addSession(o);
								continue;
							}
							Object attached = checkSession(o);
							if (attached != o) {
								fdef.field.set(t, o);
							}
						}
						break;
					}
					case O2M:
					case M2M: {
						fdef.field.setAccessible(true);
						Collection<?> col = (Collection<?>) fdef.field.get(t);
						if (col != null) {
							if (col instanceof AbstractJaquCollection) {
								// set all transient fields (These are session related and mean nothing outside the session so we don't carry them)
								((AbstractJaquCollection<?>) col).setDb(this);
								((AbstractJaquCollection<?>) col).setFieldDefinition(fdef);
								((AbstractJaquCollection<?>) col).parentPk = pk;

								((AbstractJaquCollection<?>) col).merge();
							}
							else {
								// here we get a whole new relationship container which symbolizes the current relationships
								// of the Entity. However, there still might be a case where we have relationships existing in the DB.
								// Thus, our algorithm should be, clean the current relationships then apply the new ones. Here we think of performance!
								// If the relationship has cascade type delete, we need to dispose of the objects as well as the relationships, else we just
								// dispose of the relationships.
								if (null != pk) {
									// we may get objects that have a primary key to be inserted with, which means they don't exist in the DB
									Object parent = Utils.newObject(t.getClass());
									pkDef.field.set(parent, pk);
									this.addSession(parent);
									deleteParentRelation(fdef, parent);
								}
								if (List.class.isAssignableFrom(col.getClass())) {
									@SuppressWarnings("rawtypes")
									JaquList l = new JaquList((List) col, this, fdef, factory.getPrimaryKey(t));
									l.setDb(this);
									l.merge();
									fdef.field.set(t, l);
								}
								else if (Set.class.isAssignableFrom(col.getClass())) {
									@SuppressWarnings("rawtypes")
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
				throw new JaquError(e, e.getMessage());
			}
    	}
    	return t;
    }

    /**
     * The method returns a list of relation objects. Used for lazy loading of object relationships
     * @param <T>
     * @param fieldName
     * @param myObject
     * @param type
     * @return Collection<T>
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	<T> Collection<T> getRelationFromDb(String fieldName, Object myObject, Class<T> type) {
		if (closed)
			throw new JaquError("Cannot initialize a 'Relation' outside an open session!!!. Try initializing the field directly within the class.");
		TableDefinition<?> def = define(myObject.getClass());
		FieldDefinition definition = def.getDefinitionForField(fieldName);

		if (!Collection.class.isAssignableFrom(definition.field.getType()))
			throw new JaquError("%s relation is not a collection type!!!", fieldName);
		try {
			List result = Utils.newArrayList();
			for (Class<?> dataType : definition.relationDefinition.dataType) {
				result.addAll(getRelationFromDb(definition, factory.getPrimaryKey(myObject), dataType));
			}
			if (definition.relationDefinition.dataType.length > 1 && null != definition.relationDefinition.orderByField) {
				result.sort(new FieldComperator(definition.relationDefinition.dataType[0], definition.relationDefinition.orderByField));
			}
			if (definition.field.getType().isAssignableFrom(result.getClass()))
				return new JaquList<>(result, this, definition, factory.getPrimaryKey(myObject));
			else {
				// only when the type is a Set type we will be here
				HashSet<T> set = Utils.newHashSet();
				set.addAll(result);
				return new JaquSet<>(set, this, definition, factory.getPrimaryKey(myObject));
			}
		}
		catch (Exception e) {
			throw new JaquError(e, e.getMessage());
		}
	}

	/**
	 * Prepare the statement
	 * @param sql
	 * @return PreparedStatement
	 */
    PreparedStatement prepare(String sql, String... idColumnNames) {
        try {
            if (Dialect.ORACLE == factory.getDialect() && idColumnNames.length > 0)
            	return conn.prepareStatement(sql, idColumnNames);
        	return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }
    
    /**
     * Prepare the callable statement from string
     * @param sql
     * @return CallableStatement
     */
    CallableStatement prepareCallable(String sql) {
        try {
        	return conn.prepareCall(sql);
        }
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }
    
    <X> X registerToken(X x, Token token) {
    	tokens.put(x, token);
        return x;
    }

    Token getToken(Object x) {
        return tokens.get(x);
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

	<T> List<T> getRelationByRelationTable(FieldDefinition def, Object myPrimaryKey, Class<T> type){
		TableDefinition<T> targetDef = define(type);
		// for String primary keys do the following
		StatementBuilder builder = new StatementBuilder("SELECT target.* FROM ").append(targetDef.tableName).append(" target, ").append(def.relationDefinition.relationTableName);
		builder.append(" rt where rt.").append(def.relationDefinition.relationFieldName).append("= ?").append(" and rt.").append(def.relationDefinition.relationColumnName);
		builder.append("= target.").append(targetDef.getPrimaryKeyFields().get(0).columnName);

		if (null != def.relationDefinition.orderByColumn)
			builder.append(" order by rt." + def.relationDefinition.orderByColumn + " " + def.relationDefinition.direction);
		List<T> result = Utils.newArrayList();
		
		if (factory.isShowSQL())
			StatementLogger.info(builder.toString());
		try (PreparedStatement pStmnt = this.prepare(builder.toString())) {
			pStmnt.setObject(1, myPrimaryKey);
        	try (ResultSet rs = pStmnt.executeQuery()) {
        		while (rs.next()) {
                    T item = targetDef.readRow(rs, this);
                    result.add(item);
                }
        	}
        }
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
        return result;
	}

	/**
	 * True when the Db is closed
	 * @return boolean
	 */
	boolean closed() {
		if (closed)
			return true;

		try {
			if (this.conn.isClosed() || !this.isValid()) {
				clean();
			}
		}
		catch (Exception e) {
			clean();
		}

		return closed;
	}

	/**
	 *
	 * @param field - the definition of the field on the parent
	 * @param table - the object child to be updated.
	 * @param obj - the object Parent to update the child with
	 */
	void updateRelationship(FieldDefinition field, Object table, Object obj) {
		Object pKey = factory.getPrimaryKey(table);
		Object rPk = factory.getPrimaryKey(obj);
		String primaryKey = (pKey instanceof String) ? "'" + pKey + "'" : pKey.toString();
		String relationPK = (rPk instanceof String) ? "'" + rPk + "'" : rPk.toString();
		switch (field.fieldType) {
			case O2M: handleO2MRelationship(field, table, obj, primaryKey, relationPK); return;
			case M2M: handleM2Mrelationship(field, table, obj, primaryKey, relationPK); return;
			case M2O: handleM2ORelationship(field, primaryKey, relationPK); return;
			case FK:
			case NORMAL: return;
		}
	}

	/*
	 * Removes the relationship between all children of the given parent to the parent because the parent is being deleted
	 * from the underlying persistence layer. If cascade type is delete the children are also removed from DB.
	 *
	 */
	@SuppressWarnings("unchecked")
	void deleteParentRelation(FieldDefinition fdef, Object parent) {
		// using the getter because of lazy loading...
		Collection<?> relations = null;
		try {
			fdef.getter.setAccessible(true);
			relations = (Collection<?>) fdef.getter.invoke(parent); // this must be a collection by design
			fdef.getter.setAccessible(false);
		}
		catch (Exception e) {
			throw new JaquError(e, e.getMessage());
		}
		if (fdef.relationDefinition.cascadeType == CascadeType.DELETE) {
			// in one to many there is a question of cascade delete, i.e delete the child as well. In M2M this can not be true
			if (relations != null) {
				for (Object o: relations)
					delete(o);
			}
		}
		// this means we are not in cascade delete, so we just break the link
		else {
			// validate relation data type
	        if (null == fdef.relationDefinition.dataType || 0 == fdef.relationDefinition.dataType.length) {
	            throw new JaquError("Internal Error: Relation definition for field %s has no data types", fdef.field.getName());
	        }
	        
			// since the relationFieldName should be the same for the hierarchy we can just get the first
			TableDefinition<?> tdef = define(fdef.relationDefinition.dataType[0]);
			// the following code runs only if we have not deleted our objects and we have an update interceptor.
			if (null != tdef.getInterceptor() && tdef.hasInterceptEvent(Event.UPDATE) && null != relations) {
				for (Object o: relations)
					tdef.getInterceptor().onUpdate(o);
			}
			if (null == fdef.relationDefinition.relationTableName) { // if it's cascade delete these objects where deleted already so we can skip
				// O2M relation, we need to find the other side and update the field, only if we didn't delete it before. Two options here: 1. This is a two sided relationship, which means that the field exists,
				// 2. One sided relationship, the field FK is only in the DB.... Either way deleting from the DB will do the job!
				StatementBuilder builder = new StatementBuilder(UPDATE)
						.append(tdef.tableName)
						.append(SET)
						.append(fdef.relationDefinition.relationFieldName)
						.append("=null WHERE ");
				builder.append(fdef.relationDefinition.relationFieldName).append("= ?");
				Object pKey = factory.getPrimaryKey(parent);
				executeUpdate(false, builder.toString(), pKey);
				return;
			}
		}

		// relationTables exist both in O2M and M2M relations. In this case all we need to do is to remove all the entries in the table that include the parent
		// this code runs also for cascade deletes because the normal delete removes the object, the following also removes the reference from the relationtable.
		if (fdef.relationDefinition.relationTableName != null) {
			StatementBuilder builder = new StatementBuilder("DELETE FROM ")
					.append(fdef.relationDefinition.relationTableName)
					.append(WHERE);
			builder.append(fdef.relationDefinition.relationFieldName).append("= ?");
			Object pKey = factory.getPrimaryKey(parent);
			executeUpdate(builder.toString(), pKey);
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
			Field otherSideRelation = null;
			try {
				// I use fdef.relationDefinition.dataType[0] because even in inheritance situation the relation field must be the same
				otherSideRelation = ClassUtils.findField(fdef.relationDefinition.dataType[0], fdef.relationDefinition.relationFieldName);
				otherSideRelation.setAccessible(true);
				otherSideRelation.set(child, null);
				// updates the child in the DB.
				update(child);
			}
			catch (NoSuchFieldException nsfe) {
				// this is not a two sided relationship, we need to update the table with the id
				// when we reach here the object has already been merged. All we need to do is update it.

				// Calling define here costs very little since this table's definition is cached.
				TableDefinition<?> def = define(child.getClass());
				StatementBuilder updateQuery = new StatementBuilder(UPDATE).append(def.tableName);
				updateQuery
					.append(SET)
					.append(fdef.relationDefinition.relationFieldName)
					.append(" = ")
					.append("null")
					// we assume that our table has a single column primary key.
					.append(WHERE)
					.append(fdef.relationDefinition.relationFieldName)
					.append(" = ?");
				executeUpdate(false, updateQuery.toString(), parentPrimaryKey);
			}
			catch (Exception e) {
				throw new JaquError(e, e.getMessage());
			}
			finally {
				if (null != otherSideRelation) {
					otherSideRelation.setAccessible(false);
				}
			}
		}
		// relationTables exist both in O2M and M2M relations. In this case all we need to remove a specific entry in the relation table.
		if (fdef.relationDefinition.relationTableName != null) {
			StatementBuilder builder = new StatementBuilder("DELETE FROM ")
					.append(fdef.relationDefinition.relationTableName)
					.append(WHERE)
					.append(fdef.relationDefinition.relationFieldName)
					.append("= ?")
					.append(" AND ")
					.append(fdef.relationDefinition.relationColumnName)
					.append(" = ?");
			Object rPk = factory.getPrimaryKey(child);
			executeUpdate(builder.toString(), parentPrimaryKey, rPk);
		}
	}

	int executeUpdate(boolean cleanRenentrent, String sql, Object ... args) {
    	if (this.closed)
    		throw new JaquError(SESSION_IS_CLOSED);
    	
    	if (factory.isShowSQL()) {
			StatementLogger.update(sql);
    	}
    	try (PreparedStatement stat = conn.prepareStatement(sql)) {
    		if (null != args && 0 < args.length) {
				for (int i = 0; i < args.length; i++) {
					stat.setObject(i + 1, args[i]); // +1 is because parameters in database APIs start with 1 not with 0														
				}
    		}
            int updateCount = stat.executeUpdate();
            if (cleanRenentrent)
            	this.multiCallCache.clearReEntrent();
            return updateCount;
        }
        catch (SQLException e) {
            throw new JaquError(e, e.getMessage());
        }
    }

	private void clearReEntrentCache() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length >= 4 && null != stackTrace[3] && stackTrace[3].getClassName().indexOf(COM_CENTIMIA_ORM) == -1)
			reEntrantCache.clearReEntrent();
	}
	
	private <T> List<T> getRelationFromDb(final FieldDefinition def, final Object myPrimaryKey, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
		T descriptor = Utils.newObject(type);
		List<T> result;
		if (null == def.relationDefinition.relationTableName) {
			QueryWhere<T> where = this.from(descriptor).where(st -> {
				String pk = (String.class.isAssignableFrom(myPrimaryKey.getClass()) ? "'" + myPrimaryKey.toString() + "'" : myPrimaryKey.toString());
				return st.getAs() + "." + def.relationDefinition.relationFieldName  + " = " + pk;
			});
			if (null != def.relationDefinition.orderByField) {
				Field field = ClassUtils.findField(type, def.relationDefinition.orderByField);
				field.setAccessible(true);
				if ("DESC".equals(def.relationDefinition.direction))
					result =  where.orderByDesc(field.get(descriptor)).select();
				else
					result = where.orderBy(field.get(descriptor)).select();
			}
			else
				result = where.select();
		}
		else
			result = this.getRelationByRelationTable(def, myPrimaryKey, type);
		return result;
	}

	/*
	 * see if connection is still valid. Many implementations get this wrong, the connection is not marked closed but the connection is no longer valid, timedout or something similar
	 * this method checks this exact situation.
	 */
	private boolean isValid() {
		try {
			boolean valid = conn.isValid(1);
			if (!valid) {
				try {
					// this is an open invalid connection. A connection that was not closed by the user
					StatementLogger.info("Closing connection for you!!! Please close it yourself!");
					if (null != utils)
						utils.clean();
					this.conn.close();
					StatementLogger.info("Invalid connection found!!! Cleaning up");
				}
				catch (Exception sqle) {
					// this means we have a serious problem with this connection. We will return false and send this object for clean up.
				}
			}
			return valid;
		}
		catch (Exception t) {
			// many jdbc driver implementations don't actually implement this method so we catch this error and try to validate the connection our selves.
		}
		try (Statement stmnt = conn.createStatement()) {
			stmnt.setQueryTimeout(1);
			if (factory.isShowSQL())
				StatementLogger.info("select 1");
			stmnt.executeQuery("select 1");
		}
		catch (Exception t) {
			// catching any throwable here means the connection is not valid any more
			try {
				// this is an open invalid connection. A connection that was not closed by the user
				StatementLogger.info("Closing connection for you!!! Please close it yourself!");
				if (null != utils)
					utils.clean();
				this.conn.close();
				StatementLogger.info("Invalid connection found!!! Cleaning up");
			}
			catch (Exception sqle) {
				// this means we have a serious problem with this connection. We will return false and send this object for clean up.
			}
			return false;
		}
		return true;
	}

	/**
	 * cleans the DB from everything
	 */
	private void clean() {
		this.closed = true;
		this.conn = null;
		this.factory = null;
		reEntrantCache.clearReEntrent();
		multiCallCache.clearReEntrent();
		this.scopeChangeCaller = null;
		this.closeExternal = false;
		this.commitExternal = false;
		this.rollbackOnly = false;
		tokens.clear();
	}

	/**
	 * returns true if there is a running transaction status
	 * @param status
	 * @return boolean
	 */
	private boolean hasRunningTransaction(int status) {
		return status != Status.STATUS_NO_TRANSACTION && status != Status.STATUS_UNKNOWN;
	}

	private void handleM2Mrelationship(FieldDefinition field, Object table, Object obj, String primaryKey, String relationPK) {
		// we wan't to update the other side relationship. Because we're in session we can simply get the list and set it to null
		// when the user calls get again the list will be lazy loaded with the correct values....
		Field targetField;
		try {
			targetField = ClassUtils.findField(table.getClass(), field.relationDefinition.relationFieldName);
			targetField.setAccessible(true);
			targetField.set(table, null);
			targetField.setAccessible(false);
		}
		catch (Exception e1) {
			throw new JaquError("IllegalState - The object for table %s does not hold a list of %s!! Data is not consistent", table.getClass(), obj.getClass());
		}

		// field has a relation table. In relation table we do a merge (i.e insert only if missing update if exists)
		mergeRelationTable(field, primaryKey, relationPK);
	}

	private void handleO2MRelationship(FieldDefinition field, Object table, Object obj, String primaryKey, String relationPK) {
		if (null == field.relationDefinition.relationTableName) {
			// We have a relationship without a relationTable. We might have a two sided O2M relationship, or a single sided relationship
			try {
				Field realtedField = ClassUtils.findField(table.getClass(), field.relationDefinition.relationFieldName);
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
				StatementBuilder updateQuery = new StatementBuilder(UPDATE).append(def.tableName);
				updateQuery.append(SET).append(field.relationDefinition.relationFieldName).append(" = ").append(relationPK);
				// we assume that our table has a single column primary key.
				updateQuery.append(WHERE).append(def.getPrimaryKeyFields().get(0).columnName).append(" = ").append(primaryKey);

				executeUpdate(false, updateQuery.toString());
			}
			catch (Exception e) {
				throw new JaquError(e, e.getMessage());
			}
			return;
		}

		// field has a relation table. In relation table we do a merge (i.e insert only if missing update if exists)
		mergeRelationTable(field, primaryKey, relationPK);
	}

	/**
	 * Create a relation if it does not exist between a One side and the many. Information coming from the many side.
	 * @param field - the field holding the relationship
	 * @param obj - the child object (many side)
	 * @param primaryKey - the one side key
	 * @param relationPK - the many side key.
	 */
	private void handleM2ORelationship(FieldDefinition field, String primaryKey, String relationPK) {
		// first determine the relation table name.
		String relationTableName = field.relationDefinition.relationTableName;
		if (null != relationTableName && !relationTableName.isEmpty()) {
			mergeRelationTable(field, primaryKey, relationPK);
		}
	}

	/**
	 * Create a relation table entry for the relationship if one does not exist.
	 *
	 * @param field
	 * @param primaryKey
	 * @param relationPK
	 */
	private void mergeRelationTable(FieldDefinition field, String primaryKey, String relationPK) {
		StatementBuilder checkExistsQuery = new StatementBuilder("SELECT * FROM ").append(field.relationDefinition.relationTableName).append(WHERE);
		checkExistsQuery.append(field.relationDefinition.relationColumnName).append(" = ").append(primaryKey).append(" AND ").append(field.relationDefinition.relationFieldName);
		checkExistsQuery.append(" = ").append(relationPK);

		executeQuery(checkExistsQuery.toString(), rs -> {
			if (!rs.next()) {
				// the relationship is missing
				StatementBuilder insertStmnt = new StatementBuilder("INSERT INTO ").append(field.relationDefinition.relationTableName);
				insertStmnt.append(" (").append(field.relationDefinition.relationColumnName).append(',').append(field.relationDefinition.relationFieldName).append(") ");
				insertStmnt.append(" VALUES (").append(primaryKey).append(',').append(relationPK).append(')');

				executeUpdate(false, insertStmnt.toString());
			}
			return null;
		});
	}

	/**
     * Prepare the select object for running the query.
     * @param example
     * @param params
     * @return QueryWhere<T>
     */
    private <T> QueryWhere<T> getExampleQuery(T example, ExampleOptions params){
    	@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>)example.getClass();
    	T desc = Utils.newObject(clazz);
    	QueryWhere<T> select = this.from(desc).where(st -> "1=1");

    	TableDefinition<?> tDef = define(clazz);
		try {
			for (FieldDefinition fDef: tDef.getFields()) {
				if (fDef.isSilent || fDef.isExtension)
					continue; // this field can not be selected upon.
				if (!params.getExcludeProps().contains(fDef.field.getName())) {
					fDef.field.setAccessible(true);
					Object val = fDef.field.get(example);
					boolean add = true;
					if (params.getExcludeNulls() && null == val)
						add = false;
					if (params.getExcludeZeros() && null != val) {
						if (val.getClass().isPrimitive()) {
							if (0 == new BigDecimal(String.valueOf(val)).intValue())
								add = false;
						}
						else if (Number.class.isAssignableFrom(val.getClass()) && 0 == ((Number)val).intValue()) {
							add = false;
						}
					}
					if (add) {
						if (null == val) {
							select = select.and(fDef.field.get(desc)).isNull();
						}
						else if (String.class.isAssignableFrom(val.getClass())) {
							select = select.and(fDef.field.get(desc)).like(val, params.getLikeMode());
						}
						else if (val.getClass().isAnnotationPresent(Entity.class) || val.getClass().isAnnotationPresent(MappedSuperclass.class)) {
							List<Object> joins = selectByExample(val);
							if (!joins.isEmpty()) {
								select = select.and(fDef.field.get(desc)).in(joins.toArray());
							}
							else {
								// this means that the child entity was not flagged out, and nothing was found to match the example
								// therefore we must conclude that there is no match for our search.
								return null;
							}
						}
						else {
							select = select.and(fDef.field.get(desc)).is(val);
						}
					}
				}
			}
		}
		catch (Exception e) {
			// we can't process this example
			throw new JaquError(e, e.getMessage());
		}
    	return select;
    }
}