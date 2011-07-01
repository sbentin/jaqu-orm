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
public class H2Dialect implements SQLDialect{

	/**
	 * @see org.h2.jaqu.SQLDialect#getDataType(java.lang.Class)
	 */
	public String getDataType(Class<?> fieldClass) {
		if (fieldClass == Integer.class) {
			return "INT";
		}
		else if (fieldClass == String.class) {
			return "VARCHAR";
		}
		else if (fieldClass == Double.class) {
			return "DOUBLE";
		}
		else if (fieldClass == java.math.BigDecimal.class) {
			return "DECIMAL";
		}
		else if (fieldClass == java.util.Date.class) {
			return "DATE";
		}
		else if (fieldClass == java.sql.Date.class) {
			return "DATE";
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
			return "BOOLEAN";
		}
		else if (fieldClass == Float.class) {
			return "REAL";
		}
		else if (fieldClass == java.sql.Blob.class) {
			return "BLOB";
		}
		else if (fieldClass == java.sql.Clob.class) {
			return "CLOB";
		}
		else if (fieldClass.isArray()) {
			Class<?> componentClass = fieldClass.getComponentType();
			if (Byte.class.isAssignableFrom(componentClass)) {
				// byte array is mapped to BINARY type
				return "BINARY";
			}
			else if (Table.class.isAssignableFrom(componentClass) || componentClass.getAnnotation(Entity.class) != null) {
				throw new IllegalArgumentException(
						"Array of type 'org.h2.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			}
			else {
				// any other array will just be an array of objects in the DB.
				return "ARRAY";
			}
		}
		return "VARCHAR";
	}
	
	/** 
	 * @see org.h2.jaqu.SQLDialect#createTableString(java.lang.String)
	 */
	public String createTableString(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName;
    }
	
	/**
	 * @see org.h2.jaqu.SQLDialect#checkTableExists(java.lang.String, org.h2.jaqu.Db)
	 */
	public boolean checkTableExists(String tableName, Db db) {
		// if the line above does not do the job, use this method instead...
		return false;
	}
	
	/**
	 * @see org.h2.jaqu.SQLDialect#createDiscrimantorColumn(java.lang.String, java.lang.String)
	 */
	public String createDiscrimantorColumn(String tableName, String discriminatorName) {
        return "ALTER TABLE " + tableName + " ADD " + discriminatorName + " VARCHAR(2)";
    }
	
	/**
	 * @see org.h2.jaqu.SQLDialect#checkDiscriminatorExists(java.lang.String, java.lang.String, org.h2.jaqu.Db)
	 */
	public boolean checkDiscriminatorExists(String tableName, String discriminatorName, Db db) {
		String query = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS C1 WHERE C1.TABLE_NAME = '" + tableName + "' AND C1.COLUMN_NAME = '" + discriminatorName + "'";
		ResultSet rs = null;
		try {
			rs = db.executeQuery(query);
			if (rs.next())
				return true;
		}
		catch (SQLException e) {
			return false;
		}
		finally {
			if (rs != null)
				try {
					rs.close();
				}
				catch (SQLException e) {
					// nothing to do here
				}
		}
		return false;
	}
	
	/**
	 * In H2 mapping is very straight forward between DB types and java types and thus a simple return is used
	 * 
	 * @see org.h2.jaqu.SQLDialect#getValueByType(org.h2.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 */
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		return rs.getObject(columnName);
	}
	
	/**
	 * @see org.h2.jaqu.SQLDialect#getIdentityType()
	 */
	public String getIdentityType() {
		return "IDENTITY";
	}
}
