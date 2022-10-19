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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.centimia.jaqu.test.JaquTest;

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
			
			TableD tableD = new TableD("Test Lazy Parent", null);
			db.insert(tableD);
			assertNotNull(tableD.getId());
			Long tableDId = tableD.getId();
			db.commit();
			
			// because we are caching objects on within session we now have to close the session
			db.close();
			
			db = sessionFactory.getSession();
			// now we reRead these objects and check that initially we don't receive c and after calling get we do.
			TableD desc = new TableD();
			desc = db.from(desc).primaryKey().is(tableDId).selectFirst();
			assertNotNull(desc);
			assertNotNull(desc.tableC);
			
			Field isLazyField = desc.tableC.getClass().getDeclaredField("isLazy");
			boolean isLazy = isLazyField.getBoolean(desc.tableC);
			assertTrue(isLazy);
			db.close();
			
			TableC shouldBeNull = desc.getTableC();
			assertNull(shouldBeNull);

			db = sessionFactory.getSession();
			desc.setTableC(tableC);
			db.update(desc);
			db.commit();
			db.close();
			
			db = sessionFactory.getSession();
			desc = db.from(desc).primaryKey().is(tableDId).selectFirst();
			assertNotNull(desc.tableC);
			isLazyField = desc.tableC.getClass().getDeclaredField("isLazy");
			isLazy = isLazyField.getBoolean(desc.tableC);
			assertTrue(isLazy);
			
			TableC c = desc.getTableC();
			assertNotNull(c);
			assertEquals(tableC.getId(), c.getId());
			Long tableCId = c.getId();
			db.close();
			
			// test delete of lazy relation
			db = sessionFactory.getSession();
			TableD deleteMe = new TableD();
			deleteMe.setId(tableDId);
			// we're deleting the object and its cascades by the given primaryKey only
			db.delete(deleteMe);
			db.commit();
			db.close();
			
			db = sessionFactory.getSession();
			desc = new TableD();
			desc = db.from(desc).primaryKey().is(tableDId).selectFirst();
			assertNull(desc);
			c = new TableC();
			c = db.from(c).primaryKey().is(tableCId).selectFirst();
			assertNull(c);
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