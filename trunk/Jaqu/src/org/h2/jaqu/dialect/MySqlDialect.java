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
 * Initial Developer: Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 09/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.jaqu.Db;
import org.h2.jaqu.SQLDialect;
import org.h2.jaqu.Table;
import org.h2.jaqu.Types;
import org.h2.jaqu.annotation.Entity;

/**
 * 
 * @author Shai Bentin
 *
 */
public class MySqlDialect implements SQLDialect {

	/**
	 * @see org.h2.jaqu.SQLDialect#checkTableExists(java.lang.String, org.h2.jaqu.Db)
	 */
	public boolean checkTableExists(String tableName, Db db) {
		// My SQL support "select TABLE IF NOT EXISTS" so we don't access the DB here to check it...
		return false;
	}
	
	/**
	 * @see org.h2.jaqu.SQLDialect#createTableString(java.lang.String)
	 */
	public String createTableString(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName;
	}

	/**
	 * @see org.h2.jaqu.SQLDialect#getDataType(java.lang.Class)
	 */
	public String getDataType(Class<?> fieldClass) {
		if (fieldClass == Integer.class) {
			return "INTEGER";
		}
		else if (fieldClass == String.class) {
			return "VARCHAR";
		}
		else if (fieldClass == Character.class) {
			return "CHAR";
		}
		else if (fieldClass == Double.class) {
			return "DOUBLE";
		}
		else if (fieldClass == java.math.BigDecimal.class) {
			return "DOUBLE";
		}
		else if (fieldClass == java.util.Date.class) {
			return "DATETIME";
		}
		else if (fieldClass == java.sql.Date.class) {
			return "DATETIME";
		}
		else if (fieldClass == java.sql.Time.class) {
			return "TIME";
		}
		else if (fieldClass == java.sql.Timestamp.class) {
			return "TIMESTAMP";
		}
		else if (fieldClass == Byte.class) {
			return "TINYINT";
		}
		else if (fieldClass == Long.class) {
			return "BIGINT";
		}
		else if (fieldClass == Short.class) {
			return "SMALLINT";
		}
		else if (fieldClass == Boolean.class) {
			return "TINYINT";
		}
		else if (fieldClass == Float.class) {
			return "FLOAT";
		}
		else if (fieldClass == java.sql.Blob.class) {
			return "BLOB";
		}
		else if (fieldClass == java.sql.Clob.class) {
			return "CLOB";
		}
		else if (fieldClass.isArray()) {
			// not recommended for real use. Arrays and relational DB don't go well together and don't make much sense!
			Class<?> componentClass = fieldClass.getComponentType();
			if (Table.class.isAssignableFrom(componentClass) || componentClass.getAnnotation(Entity.class) != null)
				throw new IllegalArgumentException(
						"Array of type 'org.h2.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			return "BLOB";
		}
		else if (fieldClass.isEnum()) {
			return "VARCHAR";
		}
		return "VARCHAR";
	}

	/*
	 * @see org.h2.jaqu.SQLDialect#getValueByType(org.h2.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 *  mapping is very close between DB types and java types so we just return the object at hand!
	 */
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		switch (type) {
			case BOOLEAN: return rs.getBoolean(columnName);
			case BYTE: return rs.getByte(columnName);
			case ENUM: return rs.getString(columnName);
			default: return rs.getObject(columnName);
		}
	}

	/**
	 * @see org.h2.jaqu.SQLDialect#getIdentityType()
	 */
	public String getIdentityType() {
		return "BIGINT NOT NULL AUTO_INCREMENT";
	}

	/**
	 * @see org.h2.jaqu.SQLDialect#createDiscrimantorColumn(java.lang.String, java.lang.String)
	 */
	public String createDiscrimantorColumn(String tableName, String discriminatorName) {
        return "ALTER TABLE " + tableName + "ADD " + discriminatorName + " VARCHAR(2)";
    }

	/**
	 * @see org.h2.jaqu.SQLDialect#checkDiscriminatorExists(java.lang.String, java.lang.String, org.h2.jaqu.Db)
	 */
	public boolean checkDiscriminatorExists(String tableName, String discriminatorName, Db db) {
		return false;
	}
}
