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
import java.util.Arrays;
import java.util.Date;

import com.centimia.core.ExceptionMessages;
import com.centimia.core.exception.ResourceDeadLockException;
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
public class MySqlDialect implements SQLDialect {

	private boolean isCcoreDeadlockHandler;
	
	public MySqlDialect() {
		try {
			MySqlDialect.class.getClassLoader().loadClass("com.mysql.jdbc.exceptions.DeadlockTimeoutRollbackMarker");
			isCcoreDeadlockHandler = true;
		} 
		catch (ClassNotFoundException e) {
			isCcoreDeadlockHandler = false;
		}
	}
	
	@Override
	public boolean checkTableExists(String tableName, Db db) {
		// My SQL support "select TABLE IF NOT EXISTS" so we don't access the DB here to check it...
		return false;
	}
	
	@Override
	public String createTableString(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName;
	}

	@Override
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
			return "DOUBLE";
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
			if (null != componentClass.getAnnotation(Entity.class) || null != componentClass.getAnnotation(MappedSuperclass.class))
				throw new JaquError("IllegalArgument - Array of type 'com.centimia.orm.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			return "BLOB";
		}
		else if (fieldClass.isEnum()) {
			return VARCHAR;
		}
		return VARCHAR;
	}

	/*
	 * mapping is very close between DB types and java types so we just return the object at hand!
	 */
	@Override
	public Object getValueByType(Types type, ResultSet rs, String columnName) throws SQLException {
		switch (type) {
			case BOOLEAN: return (rs.getObject(columnName) != null) && rs.getBoolean(columnName);
			case BYTE: return rs.getByte(columnName);
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
	
	/*
	 * mapping is very close between DB types and java types so we just return the object at hand!
	 */
	@Override
	public Object getValueByType(Types type, ResultSet rs, int columnNumber) throws SQLException {
		switch (type) {
			case BOOLEAN: return (rs.getObject(columnNumber) != null) && rs.getBoolean(columnNumber);
			case BYTE: return rs.getByte(columnNumber);
			case ENUM: return rs.getString(columnNumber);
			case ENUM_INT: return rs.getInt(columnNumber);
			case BIGDECIMAL: return rs.getBigDecimal(columnNumber);
			case LOCALDATE: return null != rs.getDate(columnNumber) ? rs.getDate(columnNumber).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null; // TODO this should be fixed
    		case LOCALTIME: return null != rs.getTime(columnNumber) ? null != rs.getTime(columnNumber).toLocalTime() : null;
			default: return rs.getObject(columnNumber);
		}
	}

	@Override
	public String getIdentityType() {
		return "BIGINT NOT NULL AUTO_INCREMENT";
	}

	@Override
	public String createDiscrimantorColumn(String tableName, String discriminatorName) {
        return "ALTER TABLE " + tableName + " ADD " + discriminatorName + " VARCHAR(2)";
    }

	@Override
	public boolean checkDiscriminatorExists(String tableName, String discriminatorName, Db db) {
		String query = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = '" + discriminatorName + "'";
		return db.executeQuery(query, ResultSet::next);		
	}

	@Override
	public String getFunction(Functions functionName) {
		switch (functionName){
			case IFNULL: return "IFNULL";
		}
		return "";
	}

	@Override
	public String createIndexStatement(String name, String tableName, boolean unique, String[] columns) {
		StringBuilder query = new StringBuilder();
		if (name.isEmpty()) {
			name = columns[0] + "_" + (Math.random() * 10000) + 1;
		}
		if (unique)
			query.append("CREATE UNIQUE INDEX IF NOT EXISTS ").append(name).append(" ON ").append(tableName).append(" (");
		else
			query.append("CREATE INDEX IF NOT EXISTS ").append(name).append(" ON ").append(tableName).append(" (");
		for (int i = 0; i < columns.length; i++){
			if (i > 0){
				query.append(",");
			}
			query.append(columns[i]);
		}
		query.append(")");
		return query.toString();
	}

	@Override
	public StatementBuilder wrapUpdateQuery(StatementBuilder innerUpdate, String tableName, String as) {
		StatementBuilder buff = new StatementBuilder("UPDATE ").append(tableName).append(" ").append(as).append(" SET ");
		buff.append(innerUpdate);
		return buff;
	}

	@Override
	public StatementBuilder wrapDeleteQuery(StatementBuilder innerDelete, String tableName, String as) {
		return new StatementBuilder("DELETE " + as + " FROM ").append(tableName).append(" ").append(as).append(" ").append(innerDelete);
	}

	@Override
	public void handleDeadlockException(SQLException e) {
		if (isCcoreDeadlockHandler) {
			Class<?>[] iFaces = e.getClass().getInterfaces();
			if (Arrays.stream(iFaces).anyMatch(iFace -> "DeadlockTimeoutRollbackMarker".equals(iFace.getSimpleName())))
				throw new ResourceDeadLockException(ExceptionMessages.DEADLOCK, e);
		}
		SQLDialect.super.handleDeadlockException(e);
	}

	@Override
	public String getQueryStyleDate(TemporalAccessor temporal) {
		return null;
	}

	@Override
	public String getQueryStyleDate(Date date) {
		return null;
	}
}
