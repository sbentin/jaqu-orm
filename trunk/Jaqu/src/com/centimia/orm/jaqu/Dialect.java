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
 * 09/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.centimia.orm.jaqu.dialect.DB2Dialect;
import com.centimia.orm.jaqu.dialect.Functions;
import com.centimia.orm.jaqu.dialect.H2Dialect;
import com.centimia.orm.jaqu.dialect.MySqlDialect;
import com.centimia.orm.jaqu.dialect.OracleDialect;
import com.centimia.orm.jaqu.dialect.SQLServerDialect;

/**
 * 
 * @author Shai Bentin
 */
public enum Dialect {
	H2 (new H2Dialect()), 
	ORACLE (new OracleDialect()),
	DB2 (new DB2Dialect()),
	MYSQL (new MySqlDialect()),
	SQLSERVER (new SQLServerDialect());
	
	final SQLDialect dialect;
	
	Dialect(SQLDialect dialect){
		this.dialect = dialect;
	}
	
	/**
	 * Returns the dataType in the db dialect's matching the field class given
	 * @param fieldClass
	 * @return String
	 */
	String getDataType(Class<?> fieldClass) {
		return dialect.getDataType(fieldClass);
	}
	
	/**
	 * Return the statement this dialect uses in order to create a table
	 * @param tableName
	 * @return String
	 */
	String getCreateTableStatment(String tableName) {
		return dialect.createTableString(tableName);
	}
	
	/**
	 * Get the value from the result set based on the dialect data types
	 * @param type
	 * @param rs
	 * @param columnName
	 * @return Object
	 * @throws SQLException
	 */
	Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		return dialect.getValueByType(type, rs, columnName);
	}

	/**
	 * Check if the table object (given by name) exists in the DB
	 * @param tableName
	 * @param db
	 * @return boolean true if given table name exists in the DB
	 */
	boolean checkTableExists(String tableName, Db db) {
		return dialect.checkTableExists(tableName, db);		
	}

	/**
	 * Used for inheritance. The method builds the correct statement for this dialect to create (add) a discriminator column
	 * for a table that is used for several types of objects discriminated by a discriminator.
	 * 
	 * @param tableName
	 * @param discriminatorName
	 * @return String the statement to execute for this dialect in order to create a discriminator column
	 */
	String getDiscriminatorStatment(String tableName, String discriminatorName) {
		return dialect.createDiscrimantorColumn(tableName, discriminatorName);
	}
	
	/**
	 * Checks if the discriminator column exists.
	 * 
	 * @param joinTableName
	 * @param discriminatorName
	 * @param db
	 * @return boolean
	 */
	boolean checkDiscriminatorExists(String joinTableName, String discriminatorName, Db db) {
		return dialect.checkDiscriminatorExists(joinTableName, discriminatorName, db);
	}
	
	/**
	 * Returns a String representing the column type of an Identity field in this Dialect
	 * @return String
	 */
	String getIdentityType() {
		return dialect.getIdentityType();
	}
	
	/**
	 * Some functions have different names in different dialects. Returns the correct name for this dialect
	 * @param functionName
	 * @return String
	 */
	public String getFunction(Functions functionName) {
		return dialect.getFunction(functionName);
	}
	
	/**
	 * Creates and returns the proper Alter statement that will create the index given in this dialect.
	 * 
	 * @param name
	 * @param tableName
	 * @param unique 
	 * @param columns
	 * @return String
	 */
	public String getIndexStatement(String name, String tableName, boolean unique, String[] columns){
		return dialect.createIndexStatement(name, tableName, unique, columns);
	}
	
	/**
	 * Returns the SQLDialect based on the driver class. Default H2
	 * @param clazz
	 * @return Dialect
	 */
	Dialect getDialect(String clazz) {
		if (clazz.toLowerCase().indexOf("oracle") != -1)
			return ORACLE;
		if (clazz.toLowerCase().indexOf("db2") != -1)
			return DB2;
		if (clazz.toLowerCase().indexOf("mysql") != -1)
			return MYSQL;
		if (clazz.toLowerCase().indexOf("microsoft") != -1)
			return SQLSERVER;
		if (clazz.toLowerCase().indexOf("h2") != -1)
			return H2;
		return H2;
	}
}
