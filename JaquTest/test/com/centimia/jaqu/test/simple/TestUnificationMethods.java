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
public class TestUnificationMethods extends JaquTest {

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "select unification tests Tests ";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			
			// union rows
			TableForFunctions desc = new TableForFunctions();
			List<TableForFunctions> rows = db.from(desc).where(desc.getName()).like("name1").union(db.from(desc).where(desc.getName()).like("name2"));
			assertEquals(10, rows.size());
			
			final TestTable1 table1 = new TestTable1();
			final TestTable3 table3 = new TestTable3();
			
			List<UnionObject> results = db.from(table1).where(table1.getName()).is("name2").union(db.from(table3).where(table3.getName()).is("name2"), new UnionObject() {
				{
					id = table1.getId();
					name = table1.getName();
					value = table3.getValue();
				}				
			});
			assertEquals(2, results.size());
			tearDown();
		}
		catch (Throwable e){
			db.rollback();
			result.addError(this, e);
		}
	}
	
	class UnionObject {
		public Long id;
		public String name;
		public String value;
	}
}
