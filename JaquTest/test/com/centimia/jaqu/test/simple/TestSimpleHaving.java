/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 */

package com.centimia.jaqu.test.simple;

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.orm.jaqu.HavingFunctions;

/**
 * @author shai
 */
public class TestSimpleHaving extends JaquTest {

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Simple Having function test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			final TestTable1 t1Desc = new TestTable1();
			System.out.println(db.from(t1Desc).groupBy(t1Desc.getName()).having(t1Desc.getSeason()).is(SEASON.SPRING).getSQL());
			List<?> t1Results = db.from(t1Desc).groupBy(t1Desc.getName()).having(t1Desc.getSeason()).is(SEASON.SPRING).select();
			assertEquals(2, t1Results.size());
			
			System.out.println(db.from(t1Desc).where(t1Desc.getName()).isNotNull().groupBy(t1Desc.getName()).having(HavingFunctions.MAX, t1Desc.getId()).is(3L).getSQL());
			t1Results = db.from(t1Desc).groupBy(t1Desc.getName()).having(HavingFunctions.MAX, t1Desc.getId()).is(3L).select();
			assertEquals(1, t1Results.size());
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}