/**
 * 
 */
package com.centimia.orm.jaqu;

import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author shai
 *
 */
public class SimpleArray implements Array {

	private Db conn;
	private Object[] array;
	
	public SimpleArray(Db conn, Object array) {
		this.conn = conn;
		this.array = (Object[])array;
	}	
	
	/*
	 * 
	 * @see java.sql.Array#getBaseTypeName()
	 */
	@Override
	public String getBaseTypeName() throws SQLException {
		return conn.factory.getDialect().getDataType(array.getClass().getComponentType());
	}

	/*
	 * 
	 * @see java.sql.Array#getBaseType()
	 */
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

	/*
	 * 
	 * @see java.sql.Array#getArray()
	 */
	@Override
	public Object getArray() throws SQLException {
		return array;
	}

	/*
	 * 
	 * @see java.sql.Array#getArray(java.util.Map)
	 */
	@Override
	public Object getArray(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * @see java.sql.Array#getArray(long, int)
	 */
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

	/*
	 * 
	 * @see java.sql.Array#getArray(long, int, java.util.Map)
	 */
	@Override
	public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * @see java.sql.Array#getResultSet()
	 */
	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * @see java.sql.Array#getResultSet(java.util.Map)
	 */
	@Override
	public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * @see java.sql.Array#getResultSet(long, int)
	 */
	@Override
	public ResultSet getResultSet(long index, int count) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * @see java.sql.Array#getResultSet(long, int, java.util.Map)
	 */
	@Override
	public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * @see java.sql.Array#free()
	 */
	@Override
	public void free() throws SQLException {
		
	}

}
