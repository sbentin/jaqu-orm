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
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Oct 22, 2012			shai

*/
package com.centimia.orm.jaqu;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Wrapper;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;


/**
 * Wrapper interface for 
 * @author shai
 */
class DatasourceWrapper implements DataSource, XADataSource {

	private final CommonDataSource datasource;
	private final boolean isXA;
	
	public DatasourceWrapper(CommonDataSource datasource) {
		if (DataSource.class.isAssignableFrom(datasource.getClass())) {
			this.datasource = datasource;
			this.isXA = false;
		}
		else if (XADataSource.class.isAssignableFrom(datasource.getClass())) {
			this.datasource = datasource;
			this.isXA = true;
		}
		else
			throw new JaquError("%s Not a legal datasource. Must extend either javax.sql.XADatasource or javax.sql.Datasource", datasource.getClass());
	}

	/* (non-Javadoc)
	 * @see javax.sql.XADataSource#getXAConnection()
	 */
	public XAConnection getXAConnection() throws SQLException {
		if (isXA)
			return ((XADataSource)datasource).getXAConnection();
		throw new JaquError("% is not an XADatasource!", datasource.getClass());
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		if (!isXA)
			return ((DataSource)datasource).getConnection();
		throw new JaquError("% is not a Datasource!", datasource.getClass());
	}
	
	/* (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return datasource.getLogWriter();
	}

	/* (non-Javadoc)
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		datasource.setLogWriter(out);		
	}

	/* (non-Javadoc)
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		datasource.setLoginTimeout(seconds);		
	}

	/* (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return datasource.getLoginTimeout();
	}

	/* (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return ((Wrapper)datasource).unwrap(iface);
	}

	/* (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException, ClassCastException {
		return ((Wrapper)datasource).isWrapperFor(iface);
	}

	/* (non-Javadoc)
	 * @see javax.sql.XADataSource#getXAConnection(java.lang.String, java.lang.String)
	 */
	public XAConnection getXAConnection(String user, String password) throws SQLException {
		if (isXA)
			return ((XADataSource)datasource).getXAConnection(user, password);
		throw new JaquError("% is not an XADatasource!", datasource.getClass());
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		if (!isXA)
			return ((DataSource)datasource).getConnection(username, password);
		throw new JaquError("% is not a Datasource!", datasource.getClass());
	}
	
	/**
	 * True if wrapped an XA datasource
	 * @return
	 */
	boolean isXA() {
		return this.isXA;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.CommonDataSource#getParentLogger()
	 */
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return this.datasource.getParentLogger();
	}
}
