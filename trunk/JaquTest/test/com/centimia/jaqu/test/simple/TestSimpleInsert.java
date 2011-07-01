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

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestSimpleInsert extends JaquTest {

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Simple Insert Test ";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			// The following will create a table Testtable1 in DB and insert 3 rows into it.
			db.insertAll(TestTable1.getSomeData());
			// Commit the info to the DB. Because we're running on the same connection putting commit here or after the select makes not difference.
			db.commit();
			
			// Select all rows from the Testtable1 in the DB
			System.out.println(db.from(new TestTable1()).getSQL());
			List<TestTable1> rows = db.from(new TestTable1()).select();
			assertEquals(3, rows.size());
			
			tearDown();
		}
		catch (Throwable e){
			db.rollback();
			result.addError(this, e);
		}
	}
}
