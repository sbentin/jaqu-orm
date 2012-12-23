/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 08/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test;

import java.sql.Connection;

import junit.framework.TestCase;

import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.Dialect;
import com.centimia.orm.jaqu.JaquSessionFactory;


/**
 * 
 * @author Shai Bentin
 *
 */
public abstract class JaquTest extends TestCase {

	protected Db db;
	protected static JaquSessionFactory sessionFactory = null;
	protected long time;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		if (sessionFactory == null) {
		
//			JdbcDataSource pool = new JdbcDataSource();
//			pool.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");  //jdbc:h2:tcp://localhost/~/test;SCHEMA=MIRACLE
//			pool.setUser("sa");
//			pool.setPassword("ei01nt");


//			oracle.jdbc.pool.OracleConnectionPoolDataSource pool = new oracle.jdbc.pool.OracleConnectionPoolDataSource();
//			pool.setURL("jdbc:oracle:thin:@normanserver:1521:EMIDEV"); 
//			pool.setUser("JAQU");
//			pool.setPassword("JAQU");

			com.mysql.jdbc.jdbc2.optional.MysqlDataSource pool = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
			pool.setURL("jdbc:mysql://tnv-db:3306/JAQU?useUnicode=true&amp;characterEncoding=UTF8");
			pool.setUser("jaqu");
			pool.setPassword("jaqu");
			
		
			sessionFactory = new JaquSessionFactory(pool, false, Connection.TRANSACTION_READ_COMMITTED);
			sessionFactory.setShowSQL(true);
			sessionFactory.setDialect(Dialect.MYSQL);
		}
		time = System.currentTimeMillis();
		db = sessionFactory.getSession();
		System.out.println(String.format("\nStarting %s --> db connection took: %s milliseconds", getName(), (System.currentTimeMillis() - time)));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		db.close();
		db = null;
		System.out.println(String.format("Ending %s --> session opened for: %s milliseconds. Connection closed!!!", getName(), (System.currentTimeMillis() - time)));
	}
}
