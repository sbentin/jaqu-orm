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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 *
 */
public class OracleDialect implements SQLDialect {

	/** 
	 * @see com.centimia.orm.jaqu.SQLDialect#getDataType(java.lang.Class)
	 */
	public String getDataType(Class<?> fieldClass) {
		final String VARCHAR2 = "VARCHAR2";
		final String TIMESTAMP_6 = "TIMESTAMP(6)";
		
		if (fieldClass == Integer.class) {
			return "NUMBER(10)";
		}
		else if (fieldClass == String.class) {
			return VARCHAR2;
		}
		else if (fieldClass == Double.class) {
			return "NUMBER(38,15)";
		}
		else if (fieldClass == java.math.BigDecimal.class) {
			return "NUMBER(38,15)";
		}
		else if (fieldClass == java.util.Date.class) {
			return TIMESTAMP_6;
		}
		else if (fieldClass == java.sql.Date.class) {
			return "DATE";
		}
		else if (fieldClass == java.time.LocalDate.class) {
			return "DATE";
		}
		else if (fieldClass == java.time.LocalDateTime.class) {
			return TIMESTAMP_6;
		}
		else if (fieldClass == java.time.ZonedDateTime.class) {
			return TIMESTAMP_6;
		}
		else if (fieldClass == java.time.LocalTime.class) {
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
			if (null != componentClass.getAnnotation(Entity.class) || null != componentClass.getAnnotation(MappedSuperclass.class))
				throw new JaquError("IllegalArgument - Array of type 'com.centimia.orm.jaqu.Entity' are relations. Either mark as transient or use a Collection type instead.");
			return "BLOB";
		}
		else if (fieldClass.isEnum()) {
			return VARCHAR2;
		}
		return VARCHAR2;
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
		return db.executeQuery(query, ResultSet::next);
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
    		case BOOLEAN: return (rs.getObject(columnName) != null) && rs.getBoolean(columnName);
    		case BLOB: return rs.getObject(columnName);
    		case CLOB: return rs.getClob(columnName);
    		case BYTE: return (rs.getObject(columnName) != null) ? rs.getByte(columnName): null;
    		case STRING: return rs.getString(columnName);
    		case ENUM: return rs.getString(columnName);
    		case ENUM_INT: return rs.getInt(columnName);
    		case DOUBLE: return (rs.getObject(columnName) != null) ? rs.getDouble(columnName): null;
    		case FLOAT: return (rs.getObject(columnName) != null) ? rs.getFloat(columnName): null;
    		case SHORT: return (rs.getObject(columnName) != null) ? rs.getShort(columnName): null;
    		case TIMESTAMP: return rs.getTimestamp(columnName);
    		case SQL_DATE: return rs.getDate(columnName);
    		case UTIL_DATE: return rs.getTimestamp(columnName);
    		case LOCALDATE: return null != rs.getDate(columnName) ? rs.getDate(columnName).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnName) ? rs.getTimestamp(columnName).toLocalDateTime() : null; // TODO this should be fixed
    		case LOCALTIME: return null != rs.getTime(columnName) ? null != rs.getTime(columnName).toLocalTime() : null;
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
	 * Oracle mapping is not straight forward so we map according to the type
	 * 
	 * @see com.centimia.orm.jaqu.SQLDialect#getValueByType(com.centimia.orm.jaqu.Types, java.sql.ResultSet, java.lang.String)
	 */
	public Object getValueByType(Types type, ResultSet rs, int columnNumber) throws SQLException {
		switch (type) {
    		case INTEGER: return (rs.getObject(columnNumber) != null) ? rs.getInt(columnNumber): null;
    		case LONG: return (rs.getObject(columnNumber) != null) ? rs.getLong(columnNumber): null;
    		case BIGDECIMAL: return rs.getBigDecimal(columnNumber);
    		case BOOLEAN: return (rs.getObject(columnNumber) != null) && rs.getBoolean(columnNumber);
    		case BLOB: return rs.getObject(columnNumber);
    		case CLOB: return rs.getClob(columnNumber);
    		case BYTE: return (rs.getObject(columnNumber) != null) ? rs.getByte(columnNumber): null;
    		case STRING: return rs.getString(columnNumber);
    		case ENUM: return rs.getString(columnNumber);
    		case ENUM_INT: return rs.getInt(columnNumber);
    		case DOUBLE: return (rs.getObject(columnNumber) != null) ? rs.getDouble(columnNumber): null;
    		case FLOAT: return (rs.getObject(columnNumber) != null) ? rs.getFloat(columnNumber): null;
    		case SHORT: return (rs.getObject(columnNumber) != null) ? rs.getShort(columnNumber): null;
    		case TIMESTAMP: return rs.getTimestamp(columnNumber);
    		case SQL_DATE: return rs.getDate(columnNumber);
    		case UTIL_DATE: return rs.getTimestamp(columnNumber);
    		case LOCALDATE: return null != rs.getDate(columnNumber) ? rs.getDate(columnNumber).toLocalDate() : null;
    		case LOCALDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null;
    		case ZONEDDATETIME: return null != rs.getTimestamp(columnNumber) ? rs.getTimestamp(columnNumber).toLocalDateTime() : null; // TODO this should be fixed
    		case LOCALTIME: return null != rs.getTime(columnNumber) ? null != rs.getTime(columnNumber).toLocalTime() : null;
    		case TIME: return rs.getTime(columnNumber);
    		case FK: {
    			Object o = rs.getObject(columnNumber); 
    			if (o != null) {
    				if (o instanceof BigDecimal) {
	    				if (((BigDecimal)o).scale() == 0) {
	    					if (((BigDecimal)o).precision() <= 10)
	    						return rs.getInt(columnNumber);
	    					else
	    						return rs.getLong(columnNumber);
	    				}
	    				else if (((BigDecimal)o).scale() == 14) {
	    					return rs.getFloat(columnNumber);
	    				}
	    				else
	    					return rs.getDouble(columnNumber);
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

	/*
	 * @see com.centimia.orm.jaqu.SQLDialect#getIdentitySuppliment()
	 */
	@Override
	public String getIdentitySuppliment() {
		return "GENERATED ALWAYS AS IDENTITY";
	}

	/*
	 * @see com.centimia.orm.jaqu.SQLDialect#getSequnceQuery()
	 */
	@Override
	public String getSequnceQuery(String seqName) {
		StatementBuilder builder = new StatementBuilder("SELECT ").append(seqName).append(".nextval from dual");
		return builder.toString();
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
		return db.executeQuery(query, ResultSet::next);
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
		StringBuilder query = new StringBuilder();
		if (name.length() == 0){
			name = columns[0] + "_" + (Math.random() * 10000) + 1;
		}
		if (unique)
			query.append("CREATE UNIQUE INDEX ");
		else
			query.append("CREATE INDEX ");
		query.append(tableName).append(".").append(name).append(" ON (");
		for (int i = 0; i < columns.length; i++){
			if (i > 0){
				query.append(",");
			}
			query.append(columns[i]).append(" ASC");
		}
		query.append(")");
		return query.toString();
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
	 
	@Override
	public String getQueryStyleDate(Date date) {
		if (null == date)
			return "null";
		else {
			if (java.sql.Date.class.isAssignableFrom(date.getClass()))
				return "toDate('yyyy-MM-dd','" + new SimpleDateFormat("yyyy-MM-dd").format(date) + "')";
			else
				return "toDate(yyyy-MM-dd HH:mm:ss','" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + "')";
		}
	}
	
	@Override
	public String getQueryStyleDate(TemporalAccessor temporal) {
		if (null == temporal)
			return "null";
		else {
			if (LocalDate.class.isAssignableFrom(temporal.getClass()))
				return "toDate('yyyy-MM-dd','" + DateTimeFormatter.ISO_LOCAL_DATE.format(temporal) + "')";
			if (LocalDateTime.class.isAssignableFrom(temporal.getClass()))
				return "toDate('yyyy-MM-dd HH:mm:ss','" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(temporal) + "')";
		}
		return "null";
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.SQLDialect#wrapDeleteQuery(com.centimia.orm.jaqu.util.StatementBuilder, java.lang.String, java.lang.String)
	 */
	public StatementBuilder wrapDeleteQuery(StatementBuilder innerDelete, String tableName, String as) {
		return new StatementBuilder("DELETE FROM ").append(tableName).append(" ").append(as).append(" ").append(innerDelete);
	}
}
