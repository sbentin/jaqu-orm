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

import javax.sql.CommonDataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.util.Utils;

/**
 * This factory creates persistence session ({@link Db}) which work against a single defined data source. There should be only one "Singleton" JaquSession
 * factory per defined data source. Multiple Factories can handle multiple data resources.
 *
 * @author Shai Bentin
 */
public final class JaquSessionFactory {

	private final ThreadLocal<Db> currentSession = new ThreadLocal<>();
	private final Map<Class<?>, TableDefinition<?>> classMap;

	enum ACID_CONFIG {INTERNAL, EXTERNAL}

	// setup a transaction manager if one exists
	TransactionManager tm = null;

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
	 * <li>TRANSACTION_READ_COMMITTED - @see @{link Connection#TRANSACTION_READ_COMMITTED}</li>
	 * <li>TRANSACTION_NONE - @see {@link Connection#TRANSACTION_NONE} @</li>
	 * <li>TRANSACTION_READ_UNCOMMITTED - @see {@link Connection#TRANSACTION_READ_UNCOMMITTED}</li>
	 * <li>TRANSACTION_REPEATABLE_READ - @see {@link Connection#TRANSACTION_REPEATABLE_READ}</li>
	 * <li>TRANSACTION_SERIALIZABLE - @see {@link Connection#TRANSACTION_SERIALIZABLE}</li>
	 * </ol>
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
	public JaquSessionFactory(CommonDataSource ds) {
    	if (null == ds)
    		throw new JaquError("IllegalState - Missing valid datasource!!!");
    	this.dataSource = new DatasourceWrapper(ds);
    	// This map is synchronized on put operations, within the @link{#define} process. The reason is that this map can be
    	// updated by more then one thread and I don't want a situation where two threads update it at the same time with the same definition.
    	classMap = Utils.newHashMap();

    	// attempt auto strapping of the dialect
    	this.dialect = Dialect.getDialect(ds.getClass().getName());
    }

	/**
	 * Start jaqu with or without a transaction manager but change some basic JDBC transaction defaults allowing manual commit/ rollback handeling.
	 * <b>Note: </b>Using this constructor sets the {@link ACID_CONFIG} to INTERNAL
	 *
	 * @param ds - the data source. Expected either javax.sql.Datasource or javax.sql.XADatasource
	 * @param autoCommit
	 * @param transactionIsolation
	 */
	public JaquSessionFactory(CommonDataSource ds, boolean autoCommit, int transactionIsolation) {
		this(ds);
		this.acidConfig = ACID_CONFIG.INTERNAL;
		this.autoCommit = autoCommit;
		this.transactionIsolation = transactionIsolation;
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
    		Db db = currentSession();
    		if (null == db) {
    			db = createConnection();
    			currentSession.set(db);
    		}
    		return db;
		}
		catch (Exception e) {
			throw convert(e);
		}
    }

    /**
     * Returns a JaQu session backed up by an underlying DB connection that is currently associated with the running thread
     * or 'null' if no Db is associated
     *
     * @return Db
     */
    public Db currentSession() {
    	Db db = currentSession.get();
    	if (null != db && db.closed()) {
			currentSession.remove();
			db = null;
		}
    	return db;
    }

    /**
     * Always returns a new Jaqu session, backed up by an underlying DB connection, which is not associated to the thread
     * and thus it is up to the caller to manage the lifecycle of this session.
     * When operating under the context of a transaction the caller needs to commit/ rollback the transaction before closing.
     * <b>Use with care.</b>
     *
     * @return Db
     */
    public Db newLocalSession() {
    	try {
    		return createConnection();
    	}
    	catch (Exception e) {
			throw convert(e);
		}
    }

    /**
	 * Allows changing the {@link ACID_CONFIG} throttle.
	 * @param isExternal
	 */
	public JaquSessionFactory setAcidConfigIsExternal(boolean isExternal) {
		if (isExternal)
			this.acidConfig = ACID_CONFIG.EXTERNAL;
		else
			this.acidConfig = ACID_CONFIG.INTERNAL;
		return this;
	}

	/**
	 *
	 * @param tm
	 * @return JaquSessionFactory
	 */
    public JaquSessionFactory setTransactionManager(TransactionManager tm) {
    	this.tm = tm;
    	return this;
    }

    /**
     * Sets whether to create the table if it does not exist in the underlying storage.
     * @param createTable
     */
    public JaquSessionFactory setCreateTable(boolean createTable) {
    	this.createTable = createTable;
    	return this;
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
    public JaquSessionFactory setDialect(Dialect dialect) {
		this.dialect = dialect;
		return this;
	}

    /**
	 * Set to true for debugging the generated SQL that is sent to the DB.
	 * @param showSQL true for showing the generated SQL just before running
	 */
	public JaquSessionFactory setShowSQL(boolean showSQL) {
		this.showSQL = showSQL;
		return this;
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
    <T> TableDefinition<T> updateTableDefinition(Class<T> clazz, Db db, boolean allowCreate){
    	synchronized (classMap) {
    		// Within the synchronized block we check again for the existence of the definition
    		// because some other thread may have created it in the mean time. Only if it is null
    		// we insert it to the map.
    		TableDefinition<T> def = getTableDefinition(clazz);
    		if (null == def) {
    			def = new TableDefinition<>(clazz, this.dialect);
    			def.mapFields(db);
    			// when here we have successfully mapped the fields of the entity and its O2M M2M relationships. Because
    			// in O2O relationships we may have a circular call for define on the same class so we put the definition
    			// in the map before we do it and thus we won't run through define again.
    			classMap.put(clazz, def);

    			// now define the O2O relationships.
                def.mapOneToOneFields(db);
                if (this.createTable && allowCreate)
                	def.createTableIfRequired(db);

    		}
    		return def;
		}
    }

    /*
     * convert an exception to an error object
     */
    protected static Error convert(Exception e) {
        return new Error(e);
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
			// if I'm an XADatasource I know that I'm in a transaction so don't play with autoCommit.
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
		if (StatementLogger.isDebugEnabled())
			StatementLogger.debug("opening connection " + conn.toString());
		return new Db(conn, this);
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
        if (null == def) {
            def = db.factory.updateTableDefinition(clazz, db, allowCreate);
        }
        return def;
    }
}
