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
 * 09/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.entity;

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 *
 */
public class EntityUpdateTest extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Entity Update test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			Phone ph = new Phone(3L, "23454323");
			final Person descriptor = new Person();

			Person me = db.from(descriptor).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			int size = me.getPhones().size();
			me.getPhones().add(ph);
			
			// check and see that before update nothing is changed on the db... Remember no caching so this is from the DB
			Person anotherMe = db.from(descriptor).primaryKey().is(1L).selectFirst();
			assertSame(me, anotherMe); // Proves multi Call cache.
			assertEquals(2L, size); // the number of phones before update
			
			// update the new entry
			db.update(me);
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			result.addError(this, e);
		}
		try {	
			setUp();
			final Person descriptor = new Person();			
			
			// now we see that Db was updated. NOTICE that there are three different objects of Person, each with a different state... In
			// real life, you can keep using the 'me' instance after update because it is in sync with the db, the connection is open and holds locking... no dirty reads, until commit!
			Person thirdMe = db.from(descriptor).primaryKey().is(1L).selectFirst();
			assertNotNull(thirdMe);
			assertEquals(3L, thirdMe.getPhones().size());
			
			// simple update on entities
			final Address aDesc = new Address();
			int numChanged = db.from(aDesc).set(aDesc.getCity(), "newCity").where(aDesc.getId()).smaller(3L).update();
			assertEquals(2, numChanged);
			
			// check the change, select all addresses
			List<Address> addResuls = db.from(aDesc).where(aDesc.getId()).smaller(3L).select();
			assertFalse(addResuls.isEmpty());
			for (Address addr: addResuls) {
				assertEquals("newCity", addr.getCity());
			}
			
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
