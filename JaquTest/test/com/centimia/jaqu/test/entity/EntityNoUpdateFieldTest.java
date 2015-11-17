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
 * 09/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.entity;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author Shai Bentin
 *
 */
public class EntityNoUpdateFieldTest extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Entity No Update Field test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			TableNoUpdate desc = new TableNoUpdate();
			TableC cFromDb = new TableC();
			cFromDb.name = "oldName";
			desc.setaC(cFromDb);
			db.insert(desc);
			db.commit();
			
			// we expect desc to have Id and cFromDb not to have one.
			assertNotNull(desc.getId());
			assertNull(desc.getaC().getId());
			
			db.insert(cFromDb);
			assertNotNull(cFromDb.getId());
			assertNotNull(desc.getaC().getId());
			// now we update the table, this time we should have an Id for C in the DB.
			db.update(desc);
			db.commit();
			
			desc = new TableNoUpdate();			
			TableNoUpdate me = db.from(desc).primaryKey().is(1L).selectFirst();
			assertNotNull(me);
			assertNotNull(me.getaC());
			
			TableC noUpdateField = me.getaC();
			assertNotNull(noUpdateField.name);
			
			noUpdateField.setName("newName"); // change the name
			assertEquals("newName", me.getaC().name); // make sure the new name is in the object.
			
			// save the object to the Db
			db.update(me);
			db.commit();
			
			cFromDb = db.from(new TableC()).primaryKey().is(noUpdateField.getId()).selectFirst();
			assertNotNull(cFromDb);
			
			assertFalse("newName".equals(cFromDb.name));
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
