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
import java.time.temporal.TemporalAccessor;
import java.util.Date;

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
 */
public class H2Dialect implements SQLDialect{

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getDataType(java.lang.Class)
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
			return "TIMESTAMP";
		}
		else if (fieldClass == java.sql.Date.class) {
			return "DATE";
		}
		else if (fieldClass == java.time.LocalDate.class) {
			return "DATE";
		}
		else if (fieldClass == java.time.LocalDateTime.class) {
			return "TIMESTAMP";
		}
		else if (fieldClass == java.time.ZonedDateTime.class) {
			return "TIMESTAMP";
		}
		else if (fieldClass == java.time.LocalTime.class) {
			return "TIME";
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
			else if (null != componentClass.getAnnotation(Entity.class) || null != componentClass.getAnnotation(MappedSuperclass.class)) {
				throw new JaquError("IllegalArgument - Array of type 'com.centimia.orm.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			}
			else {
				// any other array will just be an array of objects in the DB.
				return "ARRAY";
			}
		}
		else if (fieldClass.isEnum()) {
			return "VARCHAR";
		}
		return "VARCHAR";
	}
	
	/** 
	 * @see com.centimia.orm.jaqu.SQLDialect#createTableString(java.lang.String)
	 */
	public String createTableString(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName;
    }
	
	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#checkTableExists(java.lang.String, com.centimia.orm.jaqu.Db)
	 */
	public boolean checkTableExists(String tableName, Db db) {
		// if the line above does not do the job, use this method instead...
		return false;
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
		String query = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS C1 WHERE C1.TABLE_NAME = '" + tableName + "' AND C1.COLUMN_NAME = '" + discriminatorName + "'";
		return db.executeQuery(query, rs -> {
			if (rs.next())
				return true;
			return false;
		});
	}
	
	/**
	 * In H2 mapping is very straight forward between DB types and java types and thus a simple return is used
	 * 
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 */
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		switch (type) {
			case ENUM: return rs.getString(columnName);
			case ENUM_INT: return rs.getInt(columnName);
			case BIGDECIMAL: return rs.getBigDecimal(columnName);
			case LOCALDATE: return null != rs.getDate(columnName) ? rs.getDate(columnName).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null; // TODO this should be fixed to support zone
    		case LOCALTIME: return null != rs.getTime(columnName) ? null != rs.getTime(columnName).toLocalTime() : null;
    		case STRING:  return rs.getString(columnName);
    		default: return rs.getObject(columnName);
		}
	}
	
	/**
	 * In H2 mapping is very straight forward between DB types and java types and thus a simple return is used
	 * 
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, int)
	 */
	public Object getValueByType(Types type, ResultSet rs, int columnNumber) throws SQLException {
		switch (type) {
			case ENUM: return rs.getString(columnNumber);
			case ENUM_INT: return rs.getInt(columnNumber);
			case BIGDECIMAL: return rs.getBigDecimal(columnNumber);
			case LOCALDATE: return null != rs.getDate(columnNumber) ? rs.getDate(columnNumber).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null; // TODO this should be fixed to support zone
    		case LOCALTIME: return null != rs.getTime(columnNumber) ? null != rs.getTime(columnNumber).toLocalTime() : null;
    		case STRING:  return rs.getString(columnNumber);
    		default: return rs.getObject(columnNumber);
		}
	}
	
	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getIdentityType()
	 */
	public String getIdentityType() {
		return "IDENTITY";
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#getFunction(com.centimia.orm.jaqu.dialect.Functions)
	 */
	public String getFunction(Functions functionName) {
		switch(functionName){
			case IFNULL: return "IFNULL";
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#createIndexStatement(java.lang.String, boolean, java.lang.String[])
	 */
	public String createIndexStatement(String name, String tableName, boolean unique, String[] columns) {
		String query;
		if (name.length() == 0){
			name = columns[0] + "_" + (Math.random() * 10000) + 1;
		}
		if (unique)
			query = "CREATE UNIQUE INDEX IF NOT EXISTS " + name + " ON " + tableName + " (";
		else
			query = "CREATE INDEX IF NOT EXISTS " + name + " ON " + tableName + " (";
		for (int i = 0; i < columns.length; i++){
			if (i > 0){
				query += ",";
			}
			query += columns[i];
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

	@Override
	public String getQueryStyleDate(TemporalAccessor temporal) {
		return null;
	}

	@Override
	public String getQueryStyleDate(Date date) {
		return null;
	}

	@Override
	public String getSequnceQuery(String seqName) {
		StatementBuilder builder = new StatementBuilder("SELECT ").append(seqName).append(".nextval from dual");
		return builder.toString();
	}
}
