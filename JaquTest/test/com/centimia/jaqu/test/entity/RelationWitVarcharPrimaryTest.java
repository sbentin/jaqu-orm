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
Created		   Oct 7, 2013			shai

*/
package com.centimia.jaqu.test.entity;

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.orm.jaqu.Db;

/**
 * @author shai
 *
 */
public class RelationWitVarcharPrimaryTest extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Testing forign key relation to enityt with varchar primary key";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			db.createTable(PrimaryRelation.class);
			
			VarcharPrimary vp = new VarcharPrimary();
			vp.setPrimary("MY_NAME");
			vp.setSomeValue("SOME_VALUE");
			
			PrimaryRelation pr = new PrimaryRelation();
			pr.setMyValue("MY_VALUE");
			pr.setPrimary(vp);
			
			db.insert(pr);
			PrimaryRelation prDesc = new PrimaryRelation();
			prDesc = db.from(prDesc).where(Db.asPrimaryKey(prDesc.getPrimary(), String.class)).is(vp.getPrimary()).selectFirst();
			assertNotNull(prDesc);
			
			List<PrimaryRelation> prelations = db.selectByExample(pr);
			assertEquals(1, prelations.size());
			assertNotNull(prelations.get(0).getPrimary());
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
