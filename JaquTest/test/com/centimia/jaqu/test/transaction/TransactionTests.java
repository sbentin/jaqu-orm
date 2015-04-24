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
 * 30/04/2010		shai				 create
 */
package com.centimia.jaqu.test.transaction;

import java.sql.Connection;

import javax.transaction.SystemException;

import junit.framework.TestResult;

import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.Dialect;
import com.centimia.orm.jaqu.JaquSessionFactory;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.jaqu.test.entity.Person;

/**
 * 
 * @author shai
 *
 */
public class TransactionTests extends JaquTest {

	/* (non-Javadoc)
	 * @see com.centimia.jaqu.test.JaquTest#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.out.println(String.format("Ending %s --> session opened for: %s milliseconds. Connection closed!!!", getName(), (System.currentTimeMillis() - time)));
	}

	BitronixTransactionManager btm = null;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Transaction Behavior Test";
	}	
	
	protected void setup() {
//		PoolingDataSource pool = new PoolingDataSource();
//		pool.setAllowLocalTransactions(true);
		
//		pool.setClassName("com.centimia.orm.jdbcx.JdbcDataSource");
//		pool.getDriverProperties().setProperty("URL", "jdbc:h2:tcp://localhost/~/test;SCHEMA=MIRACLE;LOCK_TIMEOUT=30000");
//		pool.getDriverProperties().setProperty("user", "sa");
//		pool.getDriverProperties().setProperty("password", "ei01nt");
		
//		pool.setClassName("oracle.jdbc.xa.client.OracleXADataSource");
//		pool.getDriverProperties().setProperty("URL", "jdbc:oracle:thin:@normanserver:1521:EMIDEV");
//		pool.getDriverProperties().setProperty("user", "JAQU");
//		pool.getDriverProperties().setProperty("password", "JAQU");
//		pool.setMaxPoolSize(5);
//		pool.setUniqueName("JAQU");
//		pool.init();

		PoolingDataSource pool = new PoolingDataSource();
		pool.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		pool.setUniqueName("mySqlDB");
		pool.setMaxPoolSize(3);
		pool.setAllowLocalTransactions(true);
		pool.getDriverProperties().setProperty("user", "jaqu");
		pool.getDriverProperties().setProperty("password", "jaqu");
		pool.getDriverProperties().setProperty("URL", "jdbc:mysql://192.168.13.7:3306/JAQU?useUnicode=true&amp;characterEncoding=UTF8");

		
//		com.mysql.jdbc.jdbc2.optional.MysqlDataSource pool = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
//		pool.setURL("jdbc:mysql://192.168.13.7:3306/JAQU?useUnicode=true&amp;characterEncoding=UTF8");
//		pool.setUser("JAQU");
//		pool.setPassword("JAQU");

		sessionFactory = new JaquSessionFactory(pool, false, Connection.TRANSACTION_READ_COMMITTED);
		sessionFactory.setDialect(Dialect.MYSQL);
		sessionFactory.setCreateTable(false);
		
		btm = TransactionManagerServices.getTransactionManager();
		try {
			btm.setTransactionTimeout(40);
		}
		catch (SystemException e) {
			e.printStackTrace();
		}
		time = System.currentTimeMillis();
		System.out.println(String.format("Starting %s --> ", getName()));
	}
	

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setup();
			// the following test reading changed data within the same connection and transaction.
			Thread A = new Thread(new TesterThread(result, "Thread A"));
			Thread B = new Thread(new TesterThread(result, "Thread B"));
			
			A.start();
			Thread.sleep(3000);
			
			B.start();
			
			B.join();
			tearDown();
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
	}
	
	class TesterThread implements Runnable {

		private TestResult result;
		private String name;
		private Db dbSession = null;
		
		public TesterThread(TestResult result, String name) {
			this.result = result;
			this.name = name;
		}
		
		public void run() {
			try {
				System.out.println("Running thread " + name);
				btm.begin();
				dbSession = sessionFactory.getSession();
				// first read
				Person desc = new Person();
				Person person = dbSession.from(desc).primaryKey().is(1L).selectFirst();
				
				assertEquals("Shai", person.getFirstName());
				dbSession.from(desc).set(desc.getFirstName(), "NewShai"+name).where(desc.getId()).is(1L).update();

				System.out.println(name + " --> " + "update Done!!!");
				person = dbSession.from(desc).primaryKey().is(1L).selectFirst();
				
				assertEquals("NewShai" + name, person.getFirstName());
				
				System.out.println("Thead " + name + " Completed the update but not committed!!");
				if (name.equals("Thread A"))
					Thread.sleep(10000);
				btm.commit();
				System.out.println(name + " --> " + "commit Done!!!");
			}
			catch (Throwable e) {
				try {
					if (btm != null)
					btm.rollback();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
				Throwable t = new Throwable(name + " --> " + e.getMessage());
				t.setStackTrace(e.getStackTrace());
				result.addError(TransactionTests.this, t);
			}
			finally {
				if (dbSession != null)
					dbSession.close();
			}
		}		
	}
}
