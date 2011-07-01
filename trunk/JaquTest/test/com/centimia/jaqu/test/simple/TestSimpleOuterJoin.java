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
public class TestSimpleOuterJoin extends JaquTest {

	public String getName() {
		return "Simple left outer join test";
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
			// the following returns a list of the left hand side of join. (TestTable1)
			System.out.println(db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).getSQL());
			List<?> t1Results = db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).select();			
			// we have 5 entries here, 2 that were not in the inner join because these don't have a match in TestTable2.
			assertEquals(5, t1Results.size());
			
			// to get the right side of the join. You can do this on a multiple join as well.
			// here we again get 5 entries (result set was 5 rows), however two of the results are actually empty. Problem here is that the framework will no return
			// null, but we get Objects with empty fields
			List<TestTable2> t2Results = db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).selectRightHandJoin(t2Desc);
			assertEquals(5, t2Results.size());
			
			// this way we narrow down the select to fit only what we require as we would do in a regular select.
			System.out.println(db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).where(t2Desc.getId()).isNotNull().getSQL());
			List<TestTable2> t3Results = db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).where(t2Desc.getId()).isNotNull().selectRightHandJoin(t2Desc);
			for (Object t3Result: t3Results) {
				assertNotNull(((TestTable2)t3Result).getDescription());
			}
			
			// This is how to select some of the relation fields not related to the initial tables
			List<joinResult> joins = db.from(t1Desc).leftOuterJoin(t2Desc).on(t1Desc.getId()).is(t2Desc.getId()).where(t2Desc.getId()).isNotNull().select(new joinResult() {
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
