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
 * 30/01/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.util.Utils;

/**
 * This factory creates persistence session ({@link Db}) which work against a single defined data source. Multiple Factories can handle
 * multiple data resources.
 * 
 * @author Shai Bentin
 */
public final class JaquSessionFactory {

	private final ThreadLocal<Db> currentSession = new ThreadLocal<Db>();
	private final Map<Class<?>, TableDefinition<?>> classMap; 
	
	enum ACID_CONFIG {INTERNAL, EXTERNAL};
	
	// configuration fields
	/** Holds the underlying database connection factory/Datasource */
	private DatasourceWrapper dataSource;
	
	/** 
	 * This variable decides whether the connection will autoCommit or not. 
	 * When wrapping JaQu work in a transaction autoCommit=true usually has no effect
	 * however setting it to false is recommended. Default is 'true'.
	 */
	private boolean autoCommit = true;
	
	/** When true JaQu will output to log the sql statements it produces */
	private boolean showSQL = false;
	
	/**
	 * Determines the isolation level for a single connection.<p>
	 * <b>Available Isolations:</b><ol>
	 * <li>TRANSACTION_READ_COMMITTED</li>
	 * <li>TRANSACTION_NONE</li>
	 * <li>TRANSACTION_READ_UNCOMMITTED</li>
	 * <li>TRANSACTION_REPEATABLE_READ</li>
	 * <li>TRANSACTION_SERIALIZABLE</li>
	 * </ol>
	 * 
	 * @see Connection#TRANSACTION_NONE
	 * @see Connection#TRANSACTION_READ_COMMITTED
	 * @see Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see Connection#TRANSACTION_REPEATABLE_READ
	 * @see Connection#TRANSACTION_SERIALIZABLE
	 * 
	 * default is TRANSACTION_READ_COMMITTED
	 */
	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
    
	/** If set to false, JaQu will not attempt to create the table from the object and assume it exists */
	boolean createTable = true;

	/** The underlying relational DB dialect. Default dialect is set to H2 */
	Dialect dialect = Dialect.H2;

	/** 
	 * EXTERNAL - if you want to manage transaction isolation and auto commit externally. 
	 * INTERNAL  - to let JaquSessionFactory configure your connections via autoCommit and transactionIsolation fields. This means 
	 * 			   that if autoCommit is set to false then the user must handle commit himself.
	 */
	ACID_CONFIG acidConfig = ACID_CONFIG.INTERNAL;
	
	/**
	 * Define the factory with a datasource. Use this constructor when running inside a container that manages its own transactions
	 * 
	 * @param ds - the datasource. expected either javax.sql.Datasource or javax.sql.XADatasource
	 */
	public JaquSessionFactory(Object ds) {
    	if (null == ds)
    		throw new JaquError("IllegalState - Missing valid datasource!!!");
    	this.dataSource = new DatasourceWrapper(ds);
    	// This map is synchronized on put operations. The reason is that this map can be updated by more then one thread and
    	// I don't want a situation where two threads update it at the same time with the same definition.
    	classMap = Utils.newHashMap();
    	
    	// attempt auto strapping of the dialect
    	// this.dialect = Dialect.getDialect(ds.getClass().getName());
    }
    
	/**
	 * Start jaqu with or without a transaction manager but change some basic JDBC transaction defaults allowing manual commit/ rollback handeling.
	 * <b>Note: </b>Using this constructor sets the {@link ACID_CONFIG} to INTERNAL
	 * 
	 * @param ds - the datasource. expected either javax.sql.Datasource or javax.sql.XADatasource
	 * @param autoCommit
	 * @param transactionIsolation
	 */
	public JaquSessionFactory(Object ds, boolean autoCommit, int transactionIsolation) {
		this(ds);
		this.acidConfig = ACID_CONFIG.INTERNAL;
		this.autoCommit = autoCommit;
		this.transactionIsolation = transactionIsolation;
	}
	
	/**
	 * Allows changing the {@link ACID_CONFIG} throttle.
	 * @param isExternal
	 */
	public void setAcidConfigIsExternal(boolean isExternal) {
		if (isExternal)
			this.acidConfig = ACID_CONFIG.EXTERNAL;
		else
			this.acidConfig = ACID_CONFIG.INTERNAL;
	}
	
    /**
     * Returns a JaQu session backed up by an underlying DB connection.<br>
     * When making multiple calls to get session on the same thread this methods attempts to return the same Db session provided
     * it is still active.
     * 
     * @return Db
     */
    public Db getSession()  {
    	try {
	    	/* since it is not in the dataSource.getConnection contract 
	    	 * to make sure we always get the open connection on the thread we take care of this here
	    	 */
			if (null != currentSession.get()) {
				Db db = currentSession.get();
				if (db.closed()){
					currentSession.remove();
					db = createConnection();
				}
				return db;
			}
			return createConnection();
		}
		catch (Exception e) {
			throw convert(e);
		}
    }

	/**
	 * Creates a database connection. 
	 * @return Db
	 * @throws SQLException
	 * @throws SystemException 
	 * @throws RollbackException 
	 * @throws IllegalStateException 
	 */
	private Db createConnection() throws Exception {
		Connection conn = null;
		try {			
			// if I'm an XADatasource i know that I'm in a transaction so don't play with autocommit.
			if (dataSource.isXA()) {
				conn = dataSource.getXAConnection().getConnection();
			}
			else {
				conn = dataSource.getConnection();
			}
			if (acidConfig == ACID_CONFIG.INTERNAL) {
				conn.setAutoCommit(this.autoCommit);
				conn.setTransactionIsolation(this.transactionIsolation);
			}
		}
		catch (Exception e) {
			if (null != conn && !conn.isClosed()) {
				conn.close();
			}
			throw e;
		}
		Db db = new Db(conn, this);
		currentSession.set(db);
		return db;
	}
    
    /**
     * Sets whether to create the table if it does not exist in the underlying storage.
     * @param createTable
     */
    public void setCreateTable(boolean createTable) {
    	this.createTable = createTable;
    }
    
    /**
     * True if this factory creates Tables when the table does not exist.
     * @return boolean
     */
    public boolean isCreateTable() {
    	return createTable;
    }

    /**
     * The DB dialect this factory uses for its connections. Must match the underlying Datasource.
     * @return Dialect
     */
    public Dialect getDialect() {
    	return this.dialect;
    }
    
    /**
     * Sets the DB dialect to be used with this factory. Must match the underlying Datasource.
     * @param dialect
     */
    public void setDialect(Dialect dialect) {
		this.dialect = dialect;		
	}
    
    /**
	 * Set to true for debugging the generated SQL that is sent to the DB.
	 * @param showSQL true for showing the generated SQL just before running
	 */
	public void setShowSQL(boolean showSQL) {
		this.showSQL = showSQL;
	}

	/**
	 * @return boolean - true if in debug mode and printing the generated SQL to the log.
	 */
	public boolean isShowSQL() {
		return showSQL;
	}

	/**
     * Extract the primary key for the table represented by the field given.<br>
     * The field must represent a Table which has already been defined.
     * 
     * @param pk
     * @return Object - the primary key value
     */
    @SuppressWarnings("unchecked")
	<T,X> X getPrimaryKey(T t) {
    	 TableDefinition<?> def = getTableDefinition(t.getClass());
    	 if (def == null)
    		 throw new JaquError("Missing table definition on Object {%s}", t.getClass().getSimpleName());
    	 List<FieldDefinition> primaryKeys = def.getPrimaryKeyFields();
    	 if (null == primaryKeys || primaryKeys.isEmpty())
         	throw new JaquError("Object {%s} has no primary keys defined", t.getClass().getName());
         
         if (primaryKeys.size() > 1)
         	throw new JaquError("NOT SUPPORTED! - Can not return a key for an Object {%s} with more then one primary key defined!!!", t.getClass().getName());
    	
         FieldDefinition pkDef = primaryKeys.get(0);
    	 try {
			return (X)pkDef.field.get(t);
		}
		catch (Exception e) {
			throw new JaquError(e, e.getMessage());
		}
    }
    
    /**
     * Get the table definition. The table must be defined in the factory at this point
     * @param <T>
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    <T> TableDefinition<T> getTableDefinition(Class<T> clazz) {
        return (TableDefinition<T>) classMap.get(clazz);
    }
    
    /**
     * Updates the table definition map that lazy loads and holds all the table definitions so we don't go through the
     * definition on each call. It returns null when we successfully insert a new definition to the map. Before inserting
     * we check for an existing map. If it exist we return the found map (which means it is already configured) 
     * and does not need reconfiguring.
     * 
     * @param <T>
     * @param clazz
     * @param def
     * @return TableDefinition<T> - when the definition is already in the map, or 'null' when the definition is new and is put into the map.
     */
    <T> TableDefinition<T> updateTableDefinition(Class<T> clazz, TableDefinition<T> def){
    	synchronized (classMap) {
    		// Within the synchronized block we check again for the existence of the definition
    		// because some other thread may have created it in the mean time. Only if it is null
    		// we insert it to the map.
    		TableDefinition<T> existing = getTableDefinition(clazz);
    		if (null == existing) {
    			classMap.put(clazz, def);
    			return null;
    		}
    		return existing;
		}
    }
    
    /*
     * convert an exception to an error object
     */
    protected static Error convert(Exception e) {
        return new Error(e);
    }
    
    /*
     * Define a table from class on the underlying db. If the table does not exist it is created.
     */
    static <T> TableDefinition<T> define(Class<T> clazz, Db db){
    	return define(clazz, db, true);
    }
    
    /*
     * Define a table from class on underlying db.
     * @param <T> - the class type to be defined
     * @param clazz - the class type to be defined
     * @param db - the Db session this is done under. Note that if definition
     * @param allowCreate
     * @return TableDefinition<T>
     */
    static <T> TableDefinition<T> define(Class<T> clazz, Db db, boolean allowCreate) {
        // first non blocking operation. After the definition exists we just return
    	TableDefinition<T> def = db.factory.getTableDefinition(clazz);
        if (def == null) {
            def = new TableDefinition<T>(clazz, db.factory.dialect);
            TableDefinition<T> existing = db.factory.updateTableDefinition(clazz, def);
            if (null != existing)
            	// This check is done for thread safety. It means that by the time the current thread reached here some other thread 
            	// had already updated the map with the definition so we don't need to configure it again, we just discard 'def' and 
            	// return the 'existing' definition.
            	return existing;
            def.mapFields(db);            
            def.mapOneToOneFields(db);
            if (db.factory.createTable && allowCreate)
            	def.createTableIfRequired(db);
        }
        return def;
    }
}
