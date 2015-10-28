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
import com.centimia.jaqu.test.entity.Person;
import com.centimia.orm.jaqu.GeneralExampleOptions;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestQueryByExample extends JaquTest {

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Query by example Tests ";
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
			TableForFunctions example = new TableForFunctions();
			example.setName("name1");
			example.setValue(4.1D);
			List<TableForFunctions> tffs = db.selectByExample(example);
			assertEquals(1, tffs.size());
			
			tffs = db.selectByExample(example, new GeneralExampleOptions(new String[] {"value"}).setExcludeNulls(false));
			assertEquals(0, tffs.size());
			
			Person p = new Person();
			Person parent = new Person();
			
			// parent.setFirstName("Einat");
			p.setParent(parent);
			
			List<Person> people = db.selectByExample(p, new GeneralExampleOptions(null));
			assertEquals(2, people.size());
			
			tearDown();
		}
		catch (Throwable e){
			db.rollback();
			result.addError(this, e);
		}
	}
}
