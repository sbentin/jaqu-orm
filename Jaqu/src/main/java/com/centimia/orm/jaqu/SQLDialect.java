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

import com.centimia.orm.jaqu.dialect.Functions;
import com.centimia.orm.jaqu.util.StatementBuilder;

/**
 * An interface implemented by all Dialects
 * @author Shai Bentin
 */
public interface SQLDialect {

	/**
	 * Returns the appropriate Data type in the DB jargon fitting the given field java type
	 * @param fieldClass
	 * @return String
	 */
	public abstract String getDataType(Class<?> fieldClass);

	/**
	 * Returns the appropriate string for creating a Table in the specific Dialect jargon
	 * @param tableName
	 * @return String
	 */
	public abstract String createTableString(String tableName);

	/**
	 * Retrieves the value from the result set according to the appropriate java data type.
	 * @param type
	 * @param rs
	 * @param columnName
	 * @return Object
	 * @throws SQLException
	 */
	public abstract Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException;

	/**
	 * Checks if the table already exists. Some databases don't have this feature incorporated in their SQL language.
	 * 
	 * @param tableName
	 * @param db
	 * @return boolean
	 */
	public abstract boolean checkTableExists(String tableName, Db db);

	/**
	 * The type of field type used for Identity in the Dialect Jargon
	 * @return String
	 */
	public abstract String getIdentityType();

	/**
	 * Because Alter table is different in every dialect we used this method to add a discriminator column
	 * @param tableName
	 * @param discriminatorName
	 * @return String
	 */
	public String createDiscrimantorColumn(String tableName, String discriminatorName);

	/**
	 * Check if the discriminator column exists
	 * 
	 * @param tableName
	 * @param discriminatorName
	 * @param db
	 * @return boolean
	 */
	public boolean checkDiscriminatorExists(String tableName, String discriminatorName, Db db);

	/**
	 * returns the function that is the right function syntax for this dialect
	 * 
	 * @param functionName
	 * @return String
	 */
	public abstract String getFunction(Functions functionName);

	/**
	 * Creates and returns the proper Alter statement that will create the index given in this dialect.
	 * 
	 * @param name
	 * @param unique
	 * @param columns
	 * @return String
	 */
	public abstract String createIndexStatement(String name, String tableName, boolean unique, String[] columns);
	
	/**
	 * wraps the correct form for writing the update statement for this dialect
	 * @param innerUpdate
	 * @param tableName
	 * @param as
	 * @return StatementBuilder
	 */
	public StatementBuilder wrapUpdateQuery(StatementBuilder innerUpdate, String tableName, String as);
	
	/**
	 * wraps the correct form for writing the delete statement for this dialect
	 * @param innerDelete
	 * @param tableName
	 * @param as
	 * @return StatementBuilder
	 */
	public StatementBuilder wrapDeleteQuery(StatementBuilder innerDelete, String tableName, String as);
	
	/**
	 * Allows a dialect to handle a deadlock exception. Default behavior is to rethrow it as a Jaqu Error
	 * @param e
	 */
	default void handleDeadlockException(SQLException e) {
		throw new JaquError(e, e.getMessage());
	}
}
