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
package com.centimia.jaqu.test.simple;

import com.centimia.jaqu.test.JaquTest;

import junit.framework.TestResult;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestSimpleObjectUpdate extends JaquTest {

	public String getName() {
		return "Simple Update test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);		
		try {
			setUp();
			// Since TestTable1 is not a Table and thus does not declare a primary key to update it we need to use an update SQL command like the following and not db.Update(A obj)
			final TestTable1 descriptor = new TestTable1(); // the descriptor is a class used to describe the table, no data is actually stored there for further use.
			int t1 = db.from(descriptor).set(descriptor.getName(), "updatedName").and(descriptor.getValue(), "updatedValue").where(descriptor.getId()).is(1L).update();
			assertEquals(1, t1); // one row updated
			
			t1 = (int)db.from(descriptor).where(descriptor.getBool()).is(true).selectCount();
			assertEquals(2, t1); // two rows selected
			
			// load the object from the DB so we can see the reflected change from the db			
			TestTable1 t2 = db.from(descriptor).where(descriptor.getId()).is(1L).selectFirst();
			assertEquals("updatedName", t2.getName());
			assertEquals("updatedValue", t2.getValue());
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
