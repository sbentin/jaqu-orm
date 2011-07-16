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

import java.util.HashSet;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 *
 */
public class EntityMergeTetst extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Entity Merge tests";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			// for relationship we support List type and Set type. In the entity you just use List or Set, never use a concrete object like ArrayList or HashSet.
			HashSet<Phone> phones = new HashSet<Phone>();

			phones.add(new Phone(1L, "26546345")); // this is an existing ID different number
			phones.add(new Phone(5L, "987098234")); // new Phone
			
			// notice all work was done out of session. Calling an update on person will update the person, but since it has a relationship it will merge the relationships. i.e.
			// one phone will be updated and one will be inserted. however we go about this in a different way. we merge phones
			for (Phone phone: phones) {
				// using update here will update the first phone but do nothing with the second
				// using merge will update the first and insert the second.
				db.merge(phone);
			}
			
			final Person pDesc = new Person();
			Person me = db.from(pDesc).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			
			// getting the phones will yield the same two phones because the relationship is maintained from person only (single sided)
			// updating the phone only does not affect the relationship. To delete the relationship we have to do it from person.
			assertEquals(2, me.getPhones().size());
						
			// now lets add the relation to the missing phones.
			me.getPhones().addAll(phones); 
			// here update is enough because me exists so we can update and it's relations are always merged...
			db.update(me);
			// get from db again
			me = db.from(pDesc).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			assertEquals(3, me.getPhones().size());
			
			// lets check the value of phone 1
			for (Phone phone: me.getPhones()) {
				if (phone.getId().equals(1L)) {
					assertEquals("26546345", phone.getNum());
				}
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
