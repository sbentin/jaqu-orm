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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;



/**
 * 
 * @author Shai Bentin
 */
public class EntityDeleteTest extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Entity Delete test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			Person p = new Person(5L, "child1", "lastName2");
			// insert this object into the DB.
			db.insert(p);
			
			final Person desc = new Person();
			// select from db see that this child was inserted
			Person child = db.from(desc).primaryKey().is(5L).selectFirst();
			assertNotNull(child);
			
			db.delete(child);
			// select again from db see that it was removed.
			child = db.from(desc).primaryKey().is(5L).selectFirst();
			assertNull(child);
			
			// cascade delete on entities
			Person me = db.from(desc).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			
			// when you are a parent all your relations are also deleted, if your relation is not cascade delete, only the relation is broken the child object stays in the DB.
			db.delete(me);
			me = db.from(desc).primaryKey().is(1L).selectFirst();
			// me was deleted
			assertNull(me);
			
			final Address aDesc = new Address();
			// get all addresses. Because we only entered addresses of person me into the DB, and the relation is cascade delete there should be not addresses in there
			List<Address> addrList = db.from(aDesc).select();
			// all the addresses of one should be erased. Only addresses belonging to 2 exist
			assertEquals(2, addrList.size());
			Person e = db.from(desc).primaryKey().is(2L).selectFirst();
			List<Address> addr2List = e.getAddresses();
			
			for (Address a: addrList) {
				assertTrue(addr2List.contains(a));
			}
			
			// Simple delete on entities...
			final Phone pdesc = new Phone();
			final WorkPlace wDesc = new WorkPlace();
			int deleted = db.from(pdesc).delete();
			// one phone was added in an update
			assertEquals(4, deleted);
			
			// workPlace is a many to many relationship, when deleted all relationships are deleted from the relation table as well.
			deleted = db.from(wDesc).where(wDesc.getId()).is(1L).delete();
			assertEquals(1, deleted);
			
			// reinsert person 1L into db
			me = new Person(1L, "Shai", "Bentin");
			HashSet<Phone> phoneList = new HashSet<Phone>();
			phoneList.add(new Phone(1L, "1234567"));
			phoneList.add(new Phone(2L, "98765432"));
			me.setPhones(phoneList);
			ArrayList<Address> addresses = new ArrayList<Address>();
			addresses.add(new Address(1L, "street1", "city1", "Somewhere"));
			addresses.add(new Address(2L, "street2", "city2", "Nowhere"));
			me.setAddresses(addresses);
			ArrayList<WorkPlace> workplaces = new ArrayList<WorkPlace>();
			workplaces.add(new WorkPlace(1L, "unemployed"));
			me.setWorkPlaces(workplaces);
			
			db.insert(me);
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

	
}
