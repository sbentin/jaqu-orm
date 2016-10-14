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
 * 14/10/2016		Shai Bentin			 create
 */
package com.centimia.jaqu.test.simple;

import java.util.Map;

import com.centimia.jaqu.test.JaquTest;

import junit.framework.TestResult;

/**
 * @author shai
 */
public class TestSelectAsMap extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "select unification tests Tests ";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			
			TestTable1 table1 = new TestTable1();
			Map<String, String> results = db.from(table1).selectAsMap(table1.getName(), table1.getValue());
			
			assertEquals(2, results.size());
			assertEquals("value3", results.get("name3"));
		}
		catch (Throwable e){
			db.rollback();
			result.addError(this, e);
		}
	}
}