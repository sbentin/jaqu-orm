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
 * 10/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.entity;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.h2.jaqu.Db;
import org.h2.jaqu.JaquSessionFactory;

/**
 * 
 * @author Shai Bentin
 *
 */
public class EntitySessionTests extends TestCase {

	JaquSessionFactory sessionFactory = null;
	Db db;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
//		JdbcDataSource pool = new JdbcDataSource();
//		pool.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); //jdbc:h2:tcp://localhost/~/test;SCHEMA=MIRACLE
//		pool.setUser("sa");
//		pool.setPassword("ei01nt");
		
//		oracle.jdbc.pool.OracleDataSource pool = new oracle.jdbc.pool.OracleDataSource();
//		pool.setURL("jdbc:oracle:thin:@normanserver:1521:EMIDEV"); 
//		pool.setUser("JAQU");
//		pool.setPassword("JAQU");

		com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource pool = new com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource();
		pool.setURL("jdbc:mysql://12.0.0.7:3306/JAQU?useUnicode=true&amp;characterEncoding=UTF8"); 
		pool.setUser("JAQU");
		pool.setPassword("JAQU");
		
		sessionFactory = new JaquSessionFactory(pool, false, Connection.TRANSACTION_READ_COMMITTED);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Entity session tests";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			db = sessionFactory.getSession();
			final Person pDesc = new Person();
			try {
				pDesc.getAddresses();
				result.addError(this, null);
			}
			catch (Exception e) {
				// an exception should be thrown as we don't have a session
				System.err.println(getName() + " --> An Expected error occured as Person is not associated to a session yet - " + e.getMessage());
			}
			Person me = db.from(pDesc).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			
			db.commit();
			db.close();
			// me is still alive, so are his relations but the session is gone.
			try {
				me.getAddresses();
				result.addError(this, null);
			}
			catch (Exception e) {
				// an exception should be thrown as we don't have a session
				System.err.println(getName() + " --> An Expected error occured as db session was closed - " + e.getMessage());
			}
			
			// lets do some address work anyway, like adding two new addresses.
			List<Address> addrList = new ArrayList<Address>();
			addrList.add(new Address(5L, "street1" ,"city1", "no_country"));
			addrList.add(new Address(6L, "street2" ,"city2", "some_country"));
			// we added the new addresses to the person. 'me' has a DB id but no session. when we update it the addresses in the DB are not on 'me' so they are not effected
			// the result is that we've added the two new addresses to the DB. Here there is a known bug as 'me' is on session but does not have the correct list of addresses
			me.setAddresses(addrList);
			
			db = sessionFactory.getSession();
			db.update(me);
			me = db.from(pDesc).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			assertEquals(4, me.getAddresses().size());
			db.commit();
			db.close();
			
			// test some removes. me has lazy loaded the addresses so we can work on them
			// outside the session we can remove using an iterator or by remove(object), on list, removing by index is not available outside the session.
			Iterator<Address> addressIter = me.getAddresses().iterator();
			while (addressIter.hasNext()) {
				Address addr = addressIter.next();
				if (addr.getId() > 3)
					addressIter.remove();
			}
			
			db = sessionFactory.getSession();
			db.update(me);
			// now me should have two addresses. This object is actually usable because it is in sync with the DB (Unlike the case before where I described a bug)
			assertEquals(2, me.getAddresses().size());
			
			// lets check in the db as well, now select by name not primary key...
			me = db.from(pDesc).where(pDesc.getFirstName()).is("Shai").selectFirst();
			assertNotNull(me);
			
			assertEquals(2, me.getAddresses().size());
			db.commit();
			db.close();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
