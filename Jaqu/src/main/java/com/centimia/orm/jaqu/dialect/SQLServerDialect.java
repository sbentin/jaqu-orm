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
 * 09/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.JaquError;
import com.centimia.orm.jaqu.SQLDialect;
import com.centimia.orm.jaqu.Types;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.MappedSuperclass;
import com.centimia.orm.jaqu.util.StatementBuilder;

/**
 * 
 * @author Shai Bentin
 *
 */
public class SQLServerDialect implements SQLDialect {

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#createTableString(java.lang.String)
	 */
	public String createTableString(String tableName) {
	    return "IF NOT EXISTS (SELECT * FROM SYSOBJECTS WHERE NAME='" + tableName +"' AND XTYPE='U') CREATE TABLE " + tableName;
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#checkTableExists(java.lang.String, com.centimia.orm.jaqu.Db)
	 */
	public boolean checkTableExists(String tableName, Db db) {
		// if the line above does not do the job, use this method instead...
		return false;
	}
	
	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getDataType(java.lang.Class)
	 */
	public String getDataType(Class<?> fieldClass) {
		final String VARCHAR = "VARCHAR";
		final String DATETIME = "DATETIME";
		final String TIME = "TIME";
		
		if (fieldClass == Integer.class) {
			return "INTEGER";
		}
		else if (fieldClass == String.class) {
			return VARCHAR;
		}
		else if (fieldClass == Character.class) {
			return "CHAR";
		}
		else if (fieldClass == Double.class) {
			return "DOUBLE";
		}
		else if (fieldClass == java.math.BigDecimal.class) {
			return "DECIMAL(38,15)";
		}
		else if (fieldClass == java.util.Date.class) {
			return DATETIME;
		}
		else if (fieldClass == java.sql.Date.class) {
			return DATETIME;
		}
		else if (fieldClass == java.time.LocalDate.class) {
			return DATETIME;
		}
		else if (fieldClass == java.time.LocalDateTime.class) {
			return DATETIME;
		}
		else if (fieldClass == java.time.ZonedDateTime.class) {
			return DATETIME;
		}
		else if (fieldClass == java.time.LocalTime.class) {
			return TIME;
		}
		else if (fieldClass == java.sql.Time.class) {
			return TIME;
		}
		else if (fieldClass == java.sql.Timestamp.class) {
			return DATETIME;
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
			return "BIT";
		}
		else if (fieldClass == Float.class) {
			return "REAL";
		}
		else if (fieldClass == java.sql.Blob.class) {
			return "VARBINARY(MAX)";
		}
		else if (fieldClass == java.sql.Clob.class) {
			return "VARCHAR(MAX)";
		}
		else if (fieldClass.isArray()) {
			// not recommended for real use. Arrays and relational DB don't go well together and don't make much sense!
			Class<?> componentClass = fieldClass.getComponentType();
			if (null != componentClass.getAnnotation(Entity.class) || null != componentClass.getAnnotation(MappedSuperclass.class))
				throw new JaquError("IllegalArgument - Array of type 'com.centimia.orm.jaqu.Table' are relations. Either mark as transient or use a Collection type instead.");
			return "VARBINARY(MAX)";
		}
		else if (fieldClass.isEnum()) {
			return VARCHAR;
		}
		return VARCHAR;
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 */
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		switch (type) {
			case ENUM: return rs.getString(columnName);
			case ENUM_INT: return rs.getInt(columnName);
			case BOOLEAN: return (rs.getObject(columnName) != null) && rs.getBoolean(columnName);
			case BIGDECIMAL: return rs.getBigDecimal(columnName);
			case LOCALDATE: return null != rs.getDate(columnName) ? rs.getDate(columnName).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null; // TODO this should be fixed
    		case LOCALTIME: return null != rs.getTime(columnName) ? null != rs.getTime(columnName).toLocalTime() : null;
			default: return rs.getObject(columnName);
		}
	}
	
	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, int)
	 */
	public Object getValueByType(Types type, ResultSet rs, int columnNumber) throws SQLException {
		switch (type) {
			case ENUM: return rs.getString(columnNumber);
			case ENUM_INT: return rs.getInt(columnNumber);
			case BOOLEAN: return (rs.getObject(columnNumber) != null) && rs.getBoolean(columnNumber);
			case BIGDECIMAL: return rs.getBigDecimal(columnNumber);
			case LOCALDATE: return null != rs.getDate(columnNumber) ? rs.getDate(columnNumber).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null; // TODO this should be fixed
    		case LOCALTIME: return null != rs.getTime(columnNumber) ? null != rs.getTime(columnNumber).toLocalTime() : null;
			default: return rs.getObject(columnNumber);
		}
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getIdentityType()
	 */
	public String getIdentityType() {
		return "BIGINT IDENTITY(1,1)";
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#checkDiscriminatorExists(java.lang.String, java.lang.String, com.centimia.orm.jaqu.Db)
	 */
	public boolean checkDiscriminatorExists(String tableName, String discriminatorName, Db db) {
		String query = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = '" + discriminatorName + "'";
		return db.executeQuery(query, ResultSet::next);
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#createDiscrimantorColumn(java.lang.String, java.lang.String)
	 */
	public String createDiscrimantorColumn(String tableName, String discriminatorName) {
		return "ALTER TABLE " + tableName + " ADD " + discriminatorName + " VARCHAR(2)";
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#getFunction(com.centimia.orm.jaqu.dialect.Functions)
	 */
	public String getFunction(Functions functionName) {
		switch (functionName){
			case IFNULL: return "ISNULL";
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#createIndexStatement(java.lang.String, java.lang.String, boolean, java.lang.String[])
	 */
	public String createIndexStatement(String name, String tableName, boolean unique, String[] columns) {
		StringBuilder query = new StringBuilder();
		if (name.length() == 0){
			name = columns[0] + "_" + (Math.random() * 10000) + 1;
		}
		if (unique)
			query.append("CREATE UNIQUE INDEX ");
		else
			query.append("CREATE INDEX ");
		query.append(name).append(" ON ").append(tableName).append(" (");
		for (int i = 0; i < columns.length; i++){
			if (i > 0){
				query.append(",");
			}
			query.append(columns[i]);
		}
		query.append(")");
		return query.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#wrapUpdateQuery(com.centimia.orm.jaqu.util.StatementBuilder, java.lang.String, java.lang.String)
	 */
	public StatementBuilder wrapUpdateQuery(StatementBuilder innerUpdate, String tableName, String as) {
		StatementBuilder buff = new StatementBuilder("UPDATE ").append(as).append(" SET ");
		buff.append(innerUpdate).append(" FROM ").append(tableName).append(" ").append(as);
		return buff;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#wrapDeleteQuery(com.centimia.orm.jaqu.util.StatementBuilder, java.lang.String, java.lang.String)
	 */
	public StatementBuilder wrapDeleteQuery(StatementBuilder innerDelete, String tableName, String as) {
		return new StatementBuilder("DELETE ").append(as).append(" FROM ").append(tableName).append(" ").append(as).append(" ").append(innerDelete);
	}
}
