/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
 */

/*
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Feb 12, 2014			shai

*/
package com.centimia.jaqu.test.simple;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.orm.jaqu.constant.StatementType;

/**
 * @author shai
 *
 */
public class TestPojoUtils extends JaquTest {

	public String getName() {
		return "Pojo Utilitis Test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			List<TestTable1> tables =TestTable1.getSomeData2();
			for (TestTable1 table: tables) {
				db.pojoUtils().addBatch(table, StatementType.INSERT);
			}
			db.pojoUtils().executeBatch(TestTable1.class);
			
			db.commit();
			
			// now we should have three new objects in the db
			TestTable1 table1 = new TestTable1();
			tables = db.from(table1).where(table1.getId()).bigger(9L).select();
			Assert.assertEquals(3, tables.size());
			
			for (TestTable1 table: tables) {
				table.setName("Changed");
				db.pojoUtils().addBatch(table, StatementType.UPDATE);
			}
			// update
			int[] res = db.pojoUtils().executeBatch(TestTable1.class, StatementType.UPDATE);
			Assert.assertEquals(3, res.length);	
			db.commit();
			
			tables = db.from(table1).where(table1.getName()).is("changed").select();
			Assert.assertEquals(3, tables.size());
						
			for (TestTable1 table: tables) {
				db.pojoUtils().addBatch(table, StatementType.DELETE);
			}
			
			res = db.pojoUtils().executeBatch(TestTable1.class, StatementType.DELETE);
			Assert.assertEquals(3, res.length);
			db.commit();
			tables = db.from(table1).where(table1.getName()).is("changed").select();
			Assert.assertEquals(0, tables.size());
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
