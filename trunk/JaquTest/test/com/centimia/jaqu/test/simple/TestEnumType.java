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
 * 12/07/2011		shai				 create
 */
package com.centimia.jaqu.test.simple;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.orm.jaqu.CompareType;

/**
 * 
 * @author shai
 *
 */
public class TestEnumType extends JaquTest {
	
	public String getName() {
		return "Enum Type support Test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			
			EnumUser u = new EnumUser(SEASON.SPRING, "spring", "SP");
			db.insert(u);
			db.commit();
			
			EnumUser d = new EnumUser();
			EnumUser otherUser = db.from(d).whereEnum("season", CompareType.EQUAL, SEASON.SPRING).selectFirst();
			assertEquals(SEASON.SPRING, otherUser.getSeason());
			
			db.close();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
