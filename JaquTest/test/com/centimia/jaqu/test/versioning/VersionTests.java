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
package com.centimia.jaqu.test.versioning;

import java.sql.Connection;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.Dialect;
import com.centimia.orm.jaqu.JaquConcurrencyException;
import com.centimia.orm.jaqu.JaquSessionFactory;

import junit.framework.AssertionFailedError;
import junit.framework.TestResult;

/**
 * 
 * @author shai
 */
public class VersionTests extends JaquTest {

	/* (non-Javadoc)
	 * @see com.centimia.jaqu.test.JaquTest#tearDown()
	 */
	@Override
	protected void tearDown() {
		System.out.println(String.format("Ending %s --> session opened for: %s milliseconds. Connection closed!!!", getName(), (System.currentTimeMillis() - time)));
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Versioning Behavior Test";
	}	
	
	protected void setup() {
		com.mysql.jdbc.jdbc2.optional.MysqlDataSource pool = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
		pool.setURL("jdbc:mysql://10.18.100.13:3306/JAQU?useUnicode=true&amp;characterEncoding=UTF8");
		pool.setUser("jaqu");
		pool.setPassword("jaqu");
		
	
		sessionFactory = new JaquSessionFactory(pool, false, Connection.TRANSACTION_READ_COMMITTED);
		sessionFactory.setShowSQL(true);
		sessionFactory.setDialect(Dialect.MYSQL);
		
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
			TesterThread aThread = new TesterThread(result, "Versioning Thread A", null);
			TesterThread bThread = new TesterThread(result, "Versioning Thread B", aThread);
			Thread A = new Thread(aThread);
			Thread B = new Thread(bThread);
			
			B.start();
			Thread.sleep(1000);
			A.start();
			
			A.join();
			tearDown();
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
	}
	
	class TesterThread implements Runnable {

		private final TestResult result;
		private final String name;
		private final TesterThread other;
		public Long testerId;
		
		private volatile boolean cont = true;
		
		public TesterThread(TestResult result, String name, TesterThread other) {
			this.result = result;
			this.name = name;
			this.other = other;
		}
		
		public void run() {
			try (Db dbSession = sessionFactory.getSession()) {
				System.out.println("Running thread " + name);

				if (null != other) {
					System.out.printf("Thread %s is setting thread %s to stop at wait...\n", this.name, other.name);
					other.cont = false;
				}
				
				VersionedObject tester = new VersionedObject();
				tester.setValueToUpdate("FirstValue");
				
				dbSession.insert(tester);				
				assertNotNull(tester.getVersion());
				testerId = tester.getId();
				
				assertEquals(Integer.valueOf(1), tester.getVersion());				
				dbSession.commit();
				
				if (null != other) {
					tester = dbSession.from(new VersionedObject()).primaryKey().is(testerId).selectFirst();
					Thread.sleep(1000); // wait a little so the other thread will get the current row before it is changed
				}
				else
					tester = dbSession.from(new VersionedObject()).primaryKey().is(testerId - 1).selectFirst();
				
				System.out.printf("Thread %s is before wait...\n", this.name);
				while (!this.cont);
				System.out.printf("Thread %s is after wait...\n", this.name);
				
				tester.setValueToUpdate("SecondValue");
				
				try {
					System.out.println("Thread " + name + " " + tester);
					dbSession.update(tester);
					if (null == other)
						result.addFailure(VersionTests.this, new AssertionFailedError("Expecting update to fail on concurrency Exception"));
				}
				catch (JaquConcurrencyException jce) {
					if (null != other)
						result.addFailure(VersionTests.this, new AssertionFailedError("Unexpected concurrency Exception"));
					System.out.println(jce.getMessage());
					return;
				}
				finally {
					dbSession.commit();
				}
				assertEquals(Integer.valueOf(2), tester.getVersion());
								
				VersionedObject alias = new VersionedObject();
				Integer versionFromDb = dbSession.from(alias).primaryKey().is(tester.getId()).selectFirst(alias.getVersion());
				assertEquals(Integer.valueOf(2), versionFromDb);
			}
			catch (Throwable e) {
				e.printStackTrace();
				result.addError(VersionTests.this, e);
			}
			finally {
				if (null != other) {
					other.cont = true;
					System.out.printf("Thread %s is setting thread %s to continue...\n", this.name, other.name);
				}
			}
		}
		
		public void setCont(boolean cont) {
			this.cont = cont;
		}
	}
}
