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
 * 11/02/2010		Shai Bentin			 create
 */
package com.centimia.jaqu.test.table;

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.jaqu.test.entity.Address;

/**
 * 
 * @author shai
 *
 */
public class TableTests extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Table Tests";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			final PersonTable pDesc = new PersonTable();
			PersonTable pt = db.from(pDesc).primaryKey().is(1L).selectFirst();
			assertNotNull(pt);
			
			// lets get the address belonging to a person based on their relation
			Address_For_Person afp = new Address_For_Person();
			Address addrDesc = new Address();
			System.out.println(db.from(pDesc).innerJoin(afp).on(pDesc.getId()).is(afp.person).innerJoin(addrDesc).on(afp.address).is(addrDesc.getId()).where(pDesc.getId()).is(1L).getSQL());
			List<Address> addresses = db.from(pDesc).innerJoin(afp).on(pDesc.getId()).is(afp.person).innerJoin(addrDesc).on(afp.address).is(addrDesc.getId()).where(pDesc.getId()).is(1L).selectRightHandJoin(addrDesc);
			// we got two addresses
			assertEquals(2, addresses.size());
			
			// add a phone to phone table (using the 1 at the end creates a relation to person 1, but now only through the table more the object)
			PhoneTable phone = new PhoneTable(6L, "97235413423", 1L);
			db.insert(phone);
			final PhoneTable phDesc = new PhoneTable();
			List<PhoneTable> phones = db.from(pDesc).innerJoin(phDesc).on(pDesc.getId()).is(phDesc.getOwner()).where(phDesc.getOwner()).is(1L).selectRightHandJoin(phDesc);
			assertEquals(4, phones.size());
			
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

	class Address_For_Person {
		public Long person;
		public Long address;
	}
}
