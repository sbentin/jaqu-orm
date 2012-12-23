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
public class TestSimpleSelect extends JaquTest {

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Simple 'LIKE' Tests ";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
				
			// Select all rows from the Testtable1 in the DB
			TableForFunctions desc = new TableForFunctions();
			List<TableForFunctions> rows = db.from(desc).where(desc.getName()).like("%me1").select();
			assertEquals(5, rows.size());
			
			tearDown();
		}
		catch (Throwable e){
			db.rollback();
			result.addError(this, e);
		}
	}
}
