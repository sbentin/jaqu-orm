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
package com.centimia.orm.jaqu.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is a utility class with JDBC helper functions.
 */
public class JdbcUtils {

	private JdbcUtils() {}

	/**
	 * Close a statement without throwing an exception.
	 * 
	 * @param stat the statement or null
	 */
	public static void closeSilently(Statement stat) {
		if (stat != null) {
			try {
				stat.close();
			}
			catch (SQLException e) {
				// ignore
			}
		}
	}

	/**
	 * Close a connection without throwing an exception.
	 * 
	 * @param conn the connection or null
	 */
	public static void closeSilently(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			}
			catch (SQLException e) {
				// ignore
			}
		}
	}

	/**
	 * Close a result set without throwing an exception.
	 * 
	 * @param rs the result set or null
	 */
	public static void closeSilently(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			}
			catch (SQLException e) {
				// ignore
			}
		}
	}
}
