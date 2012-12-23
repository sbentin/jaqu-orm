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
 * 08/02/2010		Shai Bentin			 create
 */
package com.centimia.jaqu.test.simple;

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestSimpleDelete extends JaquTest {

	public String getName() {
		return "Simple Delete Test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			// we add three more rows to the DB
			db.insertAll(TestTable1.getSomeData());
			// Since TestTable1 is not a Table and thus does not declare a primary key to delete it we need to use an delete SQL command like the following and not db.delete(A obj)
			final TestTable1 descriptor = new TestTable1();
			// notice that if the table holds more then one row with id of '2' both will be deleted.
			int t1 = db.from(descriptor).where(descriptor.getId()).is(1L).and(descriptor.getName()).is("updatedName").delete();
			assertEquals(1, t1);
			db.commit();
			
			// select all rows with id of '2' should be none. There is no cache, this is a real select to the DB, although caching would be faster its not supported
			List<TestTable1> results = db.from(descriptor).where(descriptor.getName()).is("updatedName").select();
			assertTrue(results.isEmpty());
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
