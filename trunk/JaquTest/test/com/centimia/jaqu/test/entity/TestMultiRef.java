/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
 */

/*
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Dec 24, 2013			shai

*/
package com.centimia.jaqu.test.entity;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * @author shai
 *
 */
public class TestMultiRef extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Test two objects referencing the same third entity";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			TableC c = new TableC();
			c.setName("Shai Bentin");
			
			TableB b = new TableB();
			b.setaC(c);
			
			TableB b1 = new TableB();
			b1.setaC(c);
			
			TableA a = new TableA();
			a.setaB(b);
			a.setAnotherB(b1);
			
			db.insert(a);
			db.commit();
			
			TableA desc = new TableA();
			a = db.from(desc).primaryKey().is(a.getId()).selectFirst();
			
			assertSame(a.getaB().getaC(), a.getAnotherB().getaC());
			tearDown();
		}
		catch (Throwable t) {
			db.rollback();
			result.addError(this, t);
		}
	}
}
