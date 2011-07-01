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
 * 11/02/2010		Shai Bentin			 create
 */
package com.centimia.jaqu.test.entity;

import junit.framework.TestResult;

import org.h2.jaqu.Dialect;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 */
public class EntitySequenceIdentityTest extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Sequence and Identity test";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// for sequence to work you first need to build a table in the DB, and a sequence..
		if (sessionFactory.getDialect() != Dialect.MYSQL) {
			db.createTable(TableWithSequence.class);			
			db.executeUpdate("CREATE SEQUENCE MY_SEQ");
		}
		if (sessionFactory.getDialect() != Dialect.ORACLE) {
			// for oracle there is no identity field type, to create identity you need to use a sequence and a trigger on the field.
			// I don't cover this here, but it works just the same from Jaqu point of view. Currently we support H2 for identity.
			db.createTable(TableWithIdentity.class);
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			if (sessionFactory.getDialect() != Dialect.MYSQL) {
				TableWithSequence tws = new TableWithSequence("someName");
				db.insert(tws);
				
				// first we should see that our object was updated with an ID
				assertNotNull(tws.getId());
				
				// now let see if it'sn the DB
				final TableWithSequence desc = new TableWithSequence();
				tws = db.from(desc).where(desc.getName()).is("someName").selectFirst();
				assertNotNull(tws);
			}
			
			if (sessionFactory.getDialect() != Dialect.ORACLE) {
				TableWithIdentity twi = new TableWithIdentity("anotherName");
				db.insert(twi);
				
				// first we should see that our object was updated with an ID
				assertNotNull(twi.getId());
				
				// now let see if it'sn the DB
				final TableWithIdentity descI = new TableWithIdentity();
				twi = db.from(descI).where(descI.getName()).is("anotherName").selectFirst();
				assertNotNull(twi);
			}
			db.commit();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
