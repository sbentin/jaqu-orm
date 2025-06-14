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
package com.centimia.orm.jaqu;

import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author shai
 */
public class SimpleArray implements Array {

	private Db conn;
	private Object[] array;

	public SimpleArray(Db conn, Object array) {
		this.conn = conn;
		this.array = (Object[])array;
	}

	@Override
	public String getBaseTypeName() throws SQLException {
		return conn.factory.getDialect().getDataType(array.getClass().getComponentType());
	}

	@Override
	public int getBaseType() throws SQLException {
		try {
			String dataType =  conn.factory.getDialect().getDataType(array.getClass().getComponentType());
			Field f = java.sql.Types.class.getField(dataType);
			return (int)f.get(null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return 0;
		}
	}

	@Override
	public Object getArray() throws SQLException {
		return array;
	}

	@Override
	public Object getArray(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getArray(long index, int count) throws SQLException {
		Object[] objects = new Object[count];
		try {
			System.arraycopy(array, (int)index, objects, 0, count);
		}
		catch (Exception e) {
			throw new SQLException(e);
		}
		return null;
	}

	@Override
	public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(long index, int count) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void free() throws SQLException {

	}
}
