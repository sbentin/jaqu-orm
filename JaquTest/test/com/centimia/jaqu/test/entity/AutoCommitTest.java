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
Created		   Nov 5, 2012			shai

*/
package com.centimia.jaqu.test.entity;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * @author shai
 *
 */
public class AutoCommitTest extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "AutoCommit test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			Person p = new Person(12L, "shlom", "aleichem");
			db.insert(p);
			
			Phone ph = new Phone(12L, "1234567");
			Set<Phone> phones = new HashSet<Phone>();
			phones.add(ph);
			p.setPhones(phones);
			p.setId(13L);
			db.insert(p);
			tearDown();
		}
		catch (Throwable t) {
			db.rollback();
			result.addError(this, t);
		}
	}
	
	
}
