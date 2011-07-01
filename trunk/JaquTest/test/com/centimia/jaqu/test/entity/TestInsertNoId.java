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
 * 30/10/2010		shai				 create
 */
package com.centimia.jaqu.test.entity;

import java.util.ArrayList;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.jaqu.test.inheritance.Child;

/**
 * 
 * @author shai
 *
 */
public class TestInsertNoId extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Insert with relation with no id Test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			Child c = new Child(null, new java.util.Date(System.currentTimeMillis()), "c2");
			TableWithIdentity tbi = new TableWithIdentity();
			tbi.setName("Test2");
			ArrayList<Child> children = new ArrayList<Child>();
			children.add(c);
			tbi.setChildren(children);
			
			db.insert(tbi);
			db.commit();
			
			Child desc = new Child();
			Child cResult = db.from(desc).where(desc.getName()).is("c2").selectFirst();
			
			assertNotNull(cResult);
			if (cResult != null)
				assertNotNull(cResult.getTableId());
			tearDown();
		}
		catch (Exception e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
