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
import java.util.Set;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 */
public class EntityInsertTest extends JaquTest {

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Entity insert test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			// notice person is outside the DB session to start with.
			db.insertAll(Person.getSomeData());
			
			Person descriptor = new Person();
			Person me = db.from(descriptor).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			// since we marked phones as being eager loaded, if we break point here we should see them
			Set<Phone> phones = me.getPhones();
			assertEquals(2, phones.size());
			
			// addresses are marked as lazy loaded which means that at this point no addresses exist in the me instance.
			List<Address> addresses = me.getAddresses();
			assertEquals(2, addresses.size());
			
			List<WorkPlace> works = me.getWorkPlaces();
			assertEquals(1, works.size());
			assertEquals("unemployed", works.get(0).getName());
			
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
