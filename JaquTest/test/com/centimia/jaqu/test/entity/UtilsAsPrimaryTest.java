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
import com.centimia.orm.jaqu.Db;

import junit.framework.TestResult;

/**
 * @author shai
 *
 */
public class UtilsAsPrimaryTest extends JaquTest {

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Utils asPrimary Test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.Test#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		Logger logger = Logger.getLogger(UtilsAsPrimaryTest.class.getName());
		logger.setLevel(Level.FINEST);
		
		result.startTest(this);
		try {
			setUp();
			
			// first we create two objects
			TableA tableA = db.from(new TableA()).primaryKey().is(1L).selectFirst();
			TableC tableC = new TableC("Test Util asPrimary");
			tableC.setaId(tableA);
			db.insert(tableC);
			assertNotNull(tableC.getId());
			db.commit();
			
			TableA defA = new TableA();
			TableC defC = new TableC();
			
			defA = db.from(defA).innerJoin(defC).on(Db.asPrimaryKey(defC.getaId(), Long.class)).is(defA.getId())
					.where(defC.getId()).is(tableC.getId()).selectFirst();

			assertNotNull(defA);
			assertEquals(defA.getId().longValue(), 1);
			
			defA = new TableA();
			defA = db.from(defA).innerJoin(defC).on(defA).is(defC.getaId()).where(defC.getId()).is(tableC.getId()).selectFirst();
			assertNotNull(defA);
			assertEquals(defA.getId().longValue(), 1);			
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