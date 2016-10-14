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

import com.centimia.jaqu.test.JaquTest;

import junit.framework.TestResult;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestSimpleRightHandOuterJoin extends JaquTest {

	public String getName() {
		return "Simple right hand left outer join test";
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
			final TestTable2 t2Desc = new TestTable2();			
			List<String> descs = db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).selectRightHandJoin(t2Desc, t2Desc.getDescription());
			
			// we have 5 entries here, 2 that were not in the inner join because these don't have a match in TestTable2.
			assertEquals(2, descs.size());
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
