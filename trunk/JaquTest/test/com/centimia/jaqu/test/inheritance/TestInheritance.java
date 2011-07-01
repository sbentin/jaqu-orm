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
 * 01/03/2010		shai				 create
 */
package com.centimia.jaqu.test.inheritance;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author shai
 *
 */
public class TestInheritance extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Single Table Inheritance Test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			Child c = new Child(null, new java.util.Date(System.currentTimeMillis()), "c1");
			db.insert(c);
			db.commit();
			
			Child desc = new Child();
			Child cResult = db.from(desc).where(desc.getName()).is("c1").selectFirst();
			
			assertNotNull(cResult.getId());
			
			assertEquals("c1", cResult.getName());
			
			tearDown();
		}
		catch (Exception e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
