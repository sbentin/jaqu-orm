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
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 30/01/2010		Shai Bentin			 create
 */
package org.h2.jaqu;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jaqu.TableDefinition.FieldDefinition;
import org.h2.jaqu.util.Utils;

/**
 * This factory creates persistence session ({@link Db}) which work against a single defined data source. Multiple Factories can handle
 * multiple data resources.
 * 
 * @author Shai Bentin
 */
public final class JaquSessionFactory {

	/** If set to false, Jaqu will not attempt to create the table from the object and assume it exists */
	protected boolean createTable = true;

	protected Dialect DIALECT = Dialect.H2;
	
	private DataSource dataSource;
	private boolean autoCommit = true;
	private boolean showSQL = false;
	private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
	private final Map<Class<?>, TableDefinition<?>> classMap; 
    
    public JaquSessionFactory(DataSource ds) {
    	if (ds == null)
    		throw new IllegalStateException("Missing valid datasource!!!");
    	this.dataSource = ds;
    	Map<Class<?>, TableDefinition<?>> map = Utils.newHashMap();
    	// this map is synchronized on get put operations. The reason is that this map can be updated by more then one thread
    	// we don't want a situation where two threads update it at the same time with the same definition. Notice however, this
    	// doesn't prevent a possible situation when two threads update this table with the same class definition. This situation
    	// is not wanted, but the cost is minimal and it will not hurt the "thread safe" flow of jaqu so we don't deal with it.
    	classMap = Collections.synchronizedMap(map);
    }
    
    public JaquSessionFactory(DataSource ds, boolean autoCommit, int transactionIsolation) {
    	this(ds);
    	this.autoCommit = autoCommit;
    	this.transactionIsolation = transactionIsolation;
    	this.DIALECT = DIALECT.getDialect(ds.getClass().getName());
    }
    
    /**
     * Returns a Jaqu session backed up by an underlying DB connection
     * @return Db
     */
    public Db getSession()  {
		try {
			Connection conn = dataSource.getConnection();
			conn.setAutoCommit(this.autoCommit);
			conn.setTransactionIsolation(this.transactionIsolation);
			return new Db(conn, this);
		}
		catch (Exception e) {
			throw convert(e);
		}
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
    	return this.DIALECT;
    }
    
    /**
     * Sets the DB dialect to be used with this factory. Must match the underlying Datasource.
     * @param dialect
     */
    public void setDialect(Dialect dialect) {
		this.DIALECT = dialect;		
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
    		 throw new JaquError("Missing table definition on Object " + t.getClass().getSimpleName());
    	 List<FieldDefinition> primaryKeys = def.getPrimaryKeyFields();
    	 if (null == primaryKeys || primaryKeys.isEmpty())
         	throw new JaquError("Object " + t.getClass().getName() + " has no primary keys defined");
         
         if (primaryKeys.size() > 1)
         	throw new JaquError("NOT SUPPORTED! - Can not return a key for an Object [" + t.getClass() + "] with more then one primary key defined!!!");
    	
         FieldDefinition pkDef = primaryKeys.get(0);
    	 try {
			return (X)pkDef.field.get(t);
		}
		catch (Exception e) {
			throw new JaquError(e.getMessage(), e);
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
        TableDefinition<T> def = db.factory.getTableDefinition(clazz);
        if (def == null) {
            def = new TableDefinition<T>(clazz, db.factory.DIALECT);
            def.mapFields(db);
            db.factory.classMap.put(clazz, def);
            def.mapOneToOneFields(db);
            if (db.factory.createTable && allowCreate)
            	def.createTableIfRequired(db);
        }
        return def;
    }
}
