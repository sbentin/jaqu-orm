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
public class TestSimpleInnerJoin extends JaquTest {

	public String getName() {
		return "Simple inner join test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			// create test table 2 and insert some rows of test table2.
			db.insertAll(TestTable2.getSomeData());
			final TestTable1 t1Desc = new TestTable1();
			final TestTable2 t2Desc = new TestTable2();
			// the following returns a list of the left hand side of join. (TestTable1)
			System.out.println(db.from(t1Desc).innerJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).getSQL());
			List<TestTable1> t1Results = db.from(t1Desc).innerJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).and(t2Desc.getSeason()).is(SEASON.SPRING).select();			
			assertTrue(t1Results.size() == 3);
			
			t1Results = null;
			
			t1Results = db.from(t1Desc).innerJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).where(t2Desc.getDescription()).isNotNull().and(t2Desc.getSeason()).is(SEASON.SPRING).select();
			assertTrue(t1Results.size() == 3);
			
			// to get the right side of the join. You can do this on a multiple join as well.
			List<TestTable2> t2Results = db.from(t1Desc).innerJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).selectRightHandJoin(t2Desc);
			for (Object t2Result: t2Results) {
				assertNotNull(((TestTable2)t2Result).getDescription());
			}
			
			// This is how to select some of the relation fields not related to the initial tables (field names must match)
			List<joinResult> joins = db.from(t1Desc).innerJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).select(new joinResult() {
				{
					id = t1Desc.getId();
					name = t1Desc.getName();
					description = t2Desc.getDescription();
				}				
			});
			for (joinResult join: joins) {
				assertNotNull(join.description);
			}
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
	
	class joinResult {
		public Long id;
		public String description;
		public String name;
	}
	
}
