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
import com.centimia.orm.jaqu.util.StatementBuilder;

/**
 * This dialect holds information to connect the Jaqu O/R Mapping to DB/2
 * @author Shai Bentin
 */
public class DB2Dialect implements SQLDialect {

	/** 
	 * @see com.centimia.orm.jaqu.SQLDialect#createTableString(java.lang.String)
	 */
	public String createTableString(String tableName) {
		return "IF NOT EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME='" + tableName + "') CREATE TABLE " + tableName;
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
		if (fieldClass == Integer.class) {
			return "INTEGER";
		}
		else if (fieldClass == String.class) {
			return "VARCHAR";
		}
		else if (fieldClass == Double.class) {
			return "DOUBLE";
		}
		else if (fieldClass == java.math.BigDecimal.class) {
			return "DECIMAL(38,15)";
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
			return "SMALLINT";
		}
		else if (fieldClass == Long.class) {
			return "BIGINT";
		}
		else if (fieldClass == Short.class) {
			return "SMALLINT";
		}
		else if (fieldClass == Boolean.class) {
			return "SMALLINT";
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
			// not recommended for real use. Arrays and relational DB don't go well together and don't make much sense!
			Class<?> componentClass = fieldClass.getComponentType();
			if (componentClass.getAnnotation(Entity.class) != null)
				throw new JaquError("IllegalArgument - Array of type 'com.centimia.orm.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			return "BLOB";
		}
		else if (fieldClass.isEnum()) {
			return "VARCHAR";
		}
		return "VARCHAR";
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 */
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		switch (type) {
			case ENUM: return rs.getString(columnName);
			case ENUM_INT: return rs.getInt(columnName);
			case BIGDECIMAL: return rs.getBigDecimal(columnName);
			case LOCALDATE: return null != rs.getDate(columnName) ? rs.getDate(columnName).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null; // TODO this should be fixed
    		case LOCALTIME: return null != rs.getTime(columnName) ? null != rs.getTime(columnName).toLocalTime() : null;
			default: return rs.getObject(columnName);
		}
	}

	/**
	 * @see com.centimia.orm.jaqu.SQLDialect#getIdentityType()
	 */
	public String getIdentityType() {
		return "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1, NO CACHE)";
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
		String query = "SELECT 1 FROM syscat.columns WHERE tabname = '" + tableName + "' and colname = '" + discriminatorName + "'" ;
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
		switch(functionName){
			case IFNULL: return "COALESCE";
		}
		return null;
	}

	public String createIndexStatement(String name, String tableName, boolean unique, String[] columns) {
		String query;
		if (name.length() == 0){
			name = columns[0] + "_" + (Math.random() * 10000) + 1;
		}
		if (unique)
			query = "CREATE UNIQUE INDEX " + name + " ON " + tableName + " (";
		else
			query = "CREATE INDEX " + name + " ON " + tableName + " (";
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryStyleDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}
}
