/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Properties;
import javax.naming.Context;
import javax.sql.DataSource;

import org.h2.jaqu.constant.ErrorCode;
import org.h2.jaqu.jdbc.JdbcSQLException;

/**
 * This is a utility class with JDBC helper functions.
 */
public class JdbcUtils {

	private static final Properties MESSAGES = new Properties();

	private static final String[] DRIVERS = { "h2:", "org.h2.Driver", "Cache:", "com.intersys.jdbc.CacheDriver", "daffodilDB://",
			"in.co.daffodil.db.rmi.RmiDaffodilDBDriver", "daffodil", "in.co.daffodil.db.jdbc.DaffodilDBDriver", "db2:",
			"COM.ibm.db2.jdbc.net.DB2Driver", "derby:net:", "org.apache.derby.jdbc.ClientDriver", "derby://",
			"org.apache.derby.jdbc.ClientDriver", "derby:", "org.apache.derby.jdbc.EmbeddedDriver", "FrontBase:",
			"com.frontbase.jdbc.FBJDriver", "firebirdsql:", "org.firebirdsql.jdbc.FBDriver", "hsqldb:", "org.hsqldb.jdbcDriver",
			"informix-sqli:", "com.informix.jdbc.IfxDriver", "jtds:", "net.sourceforge.jtds.jdbc.Driver", "microsoft:",
			"com.microsoft.jdbc.sqlserver.SQLServerDriver", "mimer:", "com.mimer.jdbc.Driver", "mysql:", "com.mysql.jdbc.Driver", "odbc:",
			"sun.jdbc.odbc.JdbcOdbcDriver", "oracle:", "oracle.jdbc.driver.OracleDriver", "pervasive:", "com.pervasive.jdbc.v2.Driver",
			"pointbase:micro:", "com.pointbase.me.jdbc.jdbcDriver", "pointbase:", "com.pointbase.jdbc.jdbcUniversalDriver", "postgresql:",
			"org.postgresql.Driver", "sybase:", "com.sybase.jdbc3.jdbc.SybDriver", "sqlserver:",
			"com.microsoft.sqlserver.jdbc.SQLServerDriver", "teradata:", "com.ncr.teradata.TeraDriver", };

	private JdbcUtils() {
	// utility class
	}

	/**
	 * Close a statement without throwing an exception.
	 * 
	 * @param stat
	 *            the statement or null
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
	 * @param conn
	 *            the connection or null
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
	 * @param rs
	 *            the result set or null
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

	/**
	 * Open a new database connection with the given settings.
	 * 
	 * @param driver
	 *            the driver class name
	 * @param url
	 *            the database URL
	 * @param user
	 *            the user name
	 * @param password
	 *            the password
	 * @return the database connection
	 */
	public static Connection getConnection(String driver, String url, String user, String password) throws SQLException {
		Properties prop = new Properties();
		if (user != null) {
			prop.setProperty("user", user);
		}
		if (password != null) {
			prop.setProperty("password", password);
		}
		return getConnection(driver, url, prop);
	}

	/**
	 * Escape table or schema patterns used for DatabaseMetaData functions.
	 * 
	 * @param pattern
	 *            the pattern
	 * @return the escaped pattern
	 */
	public static String escapeMetaDataPattern(String pattern) {
		if (pattern == null || pattern.length() == 0) {
			return pattern;
		}
		return StringUtils.replaceAll(pattern, "\\", "\\\\");
	}

	/**
	 * Open a new database connection with the given settings.
	 * 
	 * @param driver
	 *            the driver class name
	 * @param url
	 *            the database URL
	 * @param prop the properties containing at least the user name and password
	 * @return the database connection
	 */
	public static Connection getConnection(String driver, String url, Properties prop) throws SQLException {
		if (StringUtils.isNullOrEmpty(driver)) {
			JdbcUtils.load(url);
		}
		else {
			Class<?> d = Utils.loadUserClass(driver);
			if (java.sql.Driver.class.isAssignableFrom(d)) {
				return DriverManager.getConnection(url, prop);
			}
			else if (javax.naming.Context.class.isAssignableFrom(d)) {
				// JNDI context
				try {
					Context context = (Context) d.newInstance();
					DataSource ds = (DataSource) context.lookup(url);
					String user = prop.getProperty("user");
					String password = prop.getProperty("password");
					if (StringUtils.isNullOrEmpty(user) && StringUtils.isNullOrEmpty(password)) {
						return ds.getConnection();
					}
					return ds.getConnection(user, password);
				}
				catch (Exception e) {
					throw toSQLException(e);
				}
			}
			else {
				// Don't know, but maybe it loaded a JDBC Driver
				return DriverManager.getConnection(url, prop);
			}
		}
		return DriverManager.getConnection(url, prop);
	}

	/**
	 * Get the driver class name for the given URL, or null if the URL is unknown.
	 * 
	 * @param url
	 *            the database URL
	 * @return the driver class name
	 */
	public static String getDriver(String url) {
		if (url.startsWith("jdbc:")) {
			url = url.substring("jdbc:".length());
			for (int i = 0; i < DRIVERS.length; i += 2) {
				String prefix = DRIVERS[i];
				if (url.startsWith(prefix)) {
					return DRIVERS[i + 1];
				}
			}
		}
		return null;
	}

	/**
	 * Load the driver class for the given URL, if the database URL is known.
	 * 
	 * @param url the database URL
	 * @throws JdbcSQLException 
	 */
	public static void load(String url) throws JdbcSQLException {
		String driver = getDriver(url);
		if (driver != null) {
			Utils.loadUserClass(driver);
		}
	}
	
	/**
	 * Convert an exception to a SQL exception using the default mapping.
	 * 
	 * @param e
	 *            the root cause
	 * @return the SQL exception object
	 */
	public static SQLException toSQLException(Exception e) {
		if (e instanceof SQLException) {
			return (SQLException) e;
		}
		return convert(e);
	}

	/**
     * Convert a throwable to an SQL exception using the default mapping. All
     * errors except the following are re-thrown: StackOverflowError,
     * LinkageError.
     *
     * @param e the root cause
     * @return the exception object
     */
	public static SQLException convert(Throwable e) {
		if (e instanceof InvocationTargetException) {
			return convertInvocation((InvocationTargetException) e, null);
		}
		else if (e instanceof IOException) {
			return getJdbcSQLException(ErrorCode.IO_EXCEPTION_1, e, e.toString());
		}
		else if (e instanceof OutOfMemoryError) {
			return getJdbcSQLException(ErrorCode.OUT_OF_MEMORY, e);
		}
		else if (e instanceof StackOverflowError || e instanceof LinkageError) {
			return getJdbcSQLException(ErrorCode.GENERAL_ERROR_1, e, e.toString());
		}
		else if (e instanceof Error) {
			throw (Error) e;
		}
		return getJdbcSQLException(ErrorCode.GENERAL_ERROR_1, e, e.toString());
	}

	/**
     * Convert an InvocationTarget exception to a database exception.
     *
     * @param te the root cause
     * @param message the added message or null
     * @return the SQLException object
     */
    public static SQLException convertInvocation(InvocationTargetException te, String message) {
        Throwable t = te.getTargetException();
        if (t instanceof SQLException) {
            return (SQLException)t;
        }
        message = message == null ? t.getMessage() : message + ": " + t.getMessage();
        return getJdbcSQLException(ErrorCode.EXCEPTION_IN_FUNCTION_1, t, message);
    }

    
    /**
     * Gets the SQL exception object for a specific error code.
     *
     * @param errorCode the error code
     * @param cause the cause of the exception
     * @param params the list of parameters of the message
     * @return the SQLException object
     */
    public static JdbcSQLException getJdbcSQLException(int errorCode, Throwable cause, String... params) {
        String sqlstate = ErrorCode.getState(errorCode);
        String message = translate(sqlstate, params);
        return new JdbcSQLException(message, null, sqlstate, errorCode, cause, null);
    }
    
    private static String translate(String key, String... params) {
        String message = null;
        if (MESSAGES != null) {
            // Tomcat sets final static fields to null sometimes
            message = MESSAGES.getProperty(key);
        }
        if (message == null) {
            message = "(Message " + key + " not found)";
        }
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String s = params[i];
                if (s != null && s.length() > 0) {
                    params[i] = StringUtils.quoteIdentifier(s);
                }
            }
            message = MessageFormat.format(message, (Object[]) params);
        }
        return message;
    }


}
