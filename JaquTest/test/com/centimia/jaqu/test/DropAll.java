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
package com.centimia.jaqu.test;

import junit.framework.TestResult;

/**
 * 
 * @author Shai Bentin
 *
 */
public class DropAll extends JaquTest {

	private String[] dropStrings = new String[] {
			"drop table address",
			"drop table address_for_person",
			"drop table child",
			"drop table enumuser",
			"drop table grand_child",
			"drop table person",
			"drop table phone",
			"drop table testTable1",
			"drop table testTable2",
			"drop table testTable3",
			"drop table TableForFunctions",			
			"drop table workplace",
			"drop table workplace_for_person",
			"drop table TABLE_WITH_SEQ",
			"drop table TABLE_WITH_IDENTITY",
			"drop table parent",			
			"drop table DISCRIMINATOR",
			"drop table parent_to_child",
			"drop table tablea",
			"drop table tableb",
			"drop table tablec",
			"drop table tabled",
			"drop table VERSIONING",
			"drop sequence MY_SEQ"
			
	};

	public String getName() {
		return "drop all tables";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		// here we do not count as part of he tests
		try {
			setUp();
			for (String drop: dropStrings ) {
				try {
					db.executeUpdate(drop);
				}
				catch (Exception e) {
					System.out.println(drop + " --> drop failed!!!");
				}
			}
			tearDown();
		}
		catch (Exception e) {
			
		}
	}

}
