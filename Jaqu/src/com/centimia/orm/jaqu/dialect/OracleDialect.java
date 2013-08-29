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
package com.centimia.orm.jaqu.dialect;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.JaquError;
import com.centimia.orm.jaqu.SQLDialect;
import com.centimia.orm.jaqu.Types;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.util.StatementBuilder;

/**
 * 
 * @author Shai Bentin
 *
 */
public class OracleDialect implements SQLDialect {

	/** 
	 * @see com.centimia.orm.jaqu.SQLDialect#getDataType(java.lang.Class)
	 */
	public String getDataType(Class<?> fieldClass) {
		if (fieldClass == Integer.class) {
			return "NUMBER(10)";
		}
		else if (fieldClass == String.class) {
			return "VARCHAR2";
		}
		else if (fieldClass == Double.class) {
			return "NUMBER(38,15)";
		}
		else if (fieldClass == java.math.BigDecimal.class) {
			return "NUMBER(38,15)";
		}
		else if (fieldClass == java.util.Date.class) {
			return "TIMESTAMP(6)";
		}
		else if (fieldClass == java.sql.Date.class) {
			return "DATE";
		}
		else if (fieldClass == java.sql.Time.class) {
			return "DATE";
		}
		else if (fieldClass == java.sql.Timestamp.class) {
			return "TIMESTAMP(9)";
		}
		else if (fieldClass == Byte.class) {
			return "NUMBER(3)";
		}
		else if (fieldClass == Long.class) {
			return "NUMBER(19)";
		}
		else if (fieldClass == Short.class) {
			return "NUMBER(5)";
		}
		else if (fieldClass == Boolean.class) {
			return "NUMBER(1)";
		}
		else if (fieldClass == Float.class) {
			return "NUMBER(38,7)";
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
			if (componentClass.getAnnotation(Entity.class) != null)
				throw new JaquError("IllegalArgument - Array of type 'com.centimia.orm.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			return "BLOB";
		}
		else if (fieldClass.isEnum()) {
			return "VARCHAR2";
		}
		return "VARCHAR2";
	}

	/** 
	 * @see com.centimia.orm.jaqu.SQLDialect#createTableString(java.lang.String)
	 */
	public String createTableString(String tableName) {
		return "CREATE TABLE " + tableName;
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#checkTableExists(java.lang.String, com.centimia.orm.jaqu.Db)
	 */
	public boolean checkTableExists(String tableName, Db db) {
		String query = "SELECT 1 FROM USER_TABLES WHERE TABLE_NAME = '" + tableName.toUpperCase() + "'";
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
	 * Oracle mapping is not straight forward so we map according to the type
	 * 
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 */
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		switch (type) {
    		case INTEGER: return (rs.getObject(columnName) != null) ? rs.getInt(columnName): null;
    		case LONG: return (rs.getObject(columnName) != null) ? rs.getLong(columnName): null;
    		case BIGDECIMAL: return rs.getBigDecimal(columnName);
    		case BOOLEAN: return (rs.getObject(columnName) != null) ? rs.getBoolean(columnName): null;
    		case BLOB: return rs.getObject(columnName);
    		case CLOB: return rs.getClob(columnName);
    		case BYTE: return (rs.getObject(columnName) != null) ? rs.getByte(columnName): null;
    		case STRING: return rs.getString(columnName);
    		case ENUM: return rs.getString(columnName);
    		case DOUBLE: return (rs.getObject(columnName) != null) ? rs.getDouble(columnName): null;
    		case FLOAT: return (rs.getObject(columnName) != null) ? rs.getFloat(columnName): null;
    		case SHORT: return (rs.getObject(columnName) != null) ? rs.getShort(columnName): null;
    		case TIMESTAMP: return rs.getTimestamp(columnName);
    		case SQL_DATE: return rs.getDate(columnName);
    		case UTIL_DATE: return rs.getTimestamp(columnName);
    		case TIME: return rs.getTime(columnName);
    		case FK: {
    			Object o = rs.getObject(columnName); 
    			if (o != null) {
    				if (o instanceof BigDecimal) {
	    				if (((BigDecimal)o).scale() == 0) {
	    					if (((BigDecimal)o).precision() <= 10)
	    						return rs.getInt(columnName);
	    					else
	    						return rs.getLong(columnName);
	    				}
	    				else if (((BigDecimal)o).scale() == 14) {
	    					return rs.getFloat(columnName);
	    				}
	    				else
	    					return rs.getDouble(columnName);
    				}
    				else
    					return o;
    			}
    			return null;
    		}
    		default: return null;
    	}
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getIdentityType()
	 */
	public String getIdentityType() {
		return "NUMBER(19)";
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#createDiscrimantorColumn(java.lang.String, java.lang.String)
	 */
	public String createDiscrimantorColumn(String tableName, String discriminatorName) {
        return "ALTER TABLE " + tableName + " ADD " + discriminatorName + " VARCHAR(2)";
    }

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#checkDiscriminatorExists(java.lang.String, java.lang.String, com.centimia.orm.jaqu.Db)
	 */
	public boolean checkDiscriminatorExists(String tableName, String discriminatorName, Db db) {
		String query = "Select 1 from user_tab_columns c where c.table_name = '" + tableName + "' and c.column_name = '" + discriminatorName + "'";
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

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#getFunction(com.centimia.orm.jaqu.dialect.Functions)
	 */
	public String getFunction(Functions functionName) {
		switch (functionName){
			case IFNULL: return "NVL";
		}
		return "";
	}

	public String createIndexStatement(String name, String tableName, boolean unique, String[] columns) {
		String query;
		if (name.length() == 0){
			name = columns[0] + "_" + (Math.random() * 10000) + 1;
		}
		if (unique)
			query = "CREATE UNIQUE INDEX " + tableName + "." + name + " ON (";
		else
			query = "CREATE INDEX " + tableName + "." + name + " ON (";
		for (int i = 0; i < columns.length; i++){
			if (i > 0){
				query += ",";
			}
			query += columns[i] + " ASC";
		}
		query += ")";
		return query;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#wrapUpdateQuery(com.centimia.orm.jaqu.util.StatementBuilder, java.lang.String, java.lang.String)
	 */
	public StatementBuilder wrapUpdateQuery(StatementBuilder innerUpdate, String tableName, String as) {
		StatementBuilder buff = new StatementBuilder("UPDATE ").append(tableName).append(" ").append(as).append(" SET ");
		buff.append(innerUpdate);
		return buff;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#wrapDeleteQuery(com.centimia.orm.jaqu.util.StatementBuilder, java.lang.String, java.lang.String)
	 */
	public StatementBuilder wrapDeleteQuery(StatementBuilder innerDelete, String tableName, String as) {
		StatementBuilder buff = new StatementBuilder("DELETE FROM ").append(tableName).append(" ").append(as).append(" ").append(innerDelete);
		return buff;
	}
}
