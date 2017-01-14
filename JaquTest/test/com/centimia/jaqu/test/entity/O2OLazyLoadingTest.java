/*
 * Copyright (c) 2008-2012 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.jaqu.test.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.jaqu.test.JaquTestSuite;

import junit.framework.TestResult;

/**
 * @author shai
 *
 */
public class O2OLazyLoadingTest extends JaquTest {

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "O2O Lazy loading Test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.Test#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		Logger logger = Logger.getLogger(O2OLazyLoadingTest.class.getName());
		logger.setLevel(Level.FINEST);
		
		result.startTest(this);
		try {
			setUp();
			// first we create two objects
			TableC tableC = new TableC("Test Lazy");
			db.insert(tableC);
			assertNotNull(tableC.getId());
			
			TableD tableD = new TableD("Test Lazy Parent", tableC);
			db.insert(tableD);
			assertNotNull(tableD.getId());
			db.commit();
			
			// because we are caching objects on within session we now have to close the session
			db.close();
			
			db = sessionFactory.getSession();
			// now we reRead these objects and check that initially we don't receive c and after calling get we do.
			TableD desc = new TableD();
			desc = db.from(desc).primaryKey().is(tableD.getId()).selectFirst();
			assertNotNull(desc);
			assertNull(desc.tableC);
			
			TableC c = desc.getTableC();
			assertNotNull(c);
			assertEquals(tableC.getId(), c.getId());
		}
		catch (Exception e) {
			db.rollback();
			result.addError(this, e);
		}
		finally {
			tearDown();
		}
	}
}