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
 * 01/03/2010		shai				 create
 */
package com.centimia.jaqu.test.inheritance;

import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * 
 * @author shai
 *
 */
public class TestDiscriminator extends JaquTest {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	@Override
	public String getName() {
		return "Discriminator Table Inheritance Test";
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			
			// add the discriminator class
			db.executeUpdate("CREATE TABLE IF NOT EXISTS DISCRIMINATOR(id BIGINT NOT NULL AUTO_INCREMENT, FIRST_NAME VARCHAR(256), LAST_NAME VARCHAR(256), AGE INTEGER, partner BIGINT, PRIMARY KEY(id))");
			
			InherittedClass iClass = new InherittedClass();
			iClass.setFirstName("inherited1");
			iClass.setLastName("inherited1");
			iClass.setAge(22);
			
			InherittedClass iClass2 = new InherittedClass();
			iClass2.setFirstName("inherited2");
			iClass2.setLastName("inherited2");
			iClass2.setAge(21);
			
			InherittedClass iClass3 = new InherittedClass();
			iClass3.setFirstName("inherited3");
			iClass3.setLastName("inherited3");
			iClass3.setAge(20);
			
			// first I insert an inherittedClass so the table will be built correctly.
			db.insert(iClass);
			db.insert(iClass2);
			db.insert(iClass3);

			iClass2.setPartner(iClass3);
			iClass3.setPartner(iClass2);
			db.update(iClass2);
			db.update(iClass3);
			
			SuperClass superOne = new SuperClass();
			superOne.setFirstName("super1");
			superOne.setLastName("Super1");
			
			SuperClass superTwo = new SuperClass();
			superTwo.setFirstName("super2");
			superTwo.setLastName("Super2");
			
			db.insert(superOne);
			db.insert(superTwo);
			
			superOne.setPartner(superTwo);
			superTwo.setPartner(superOne);
			
			db.commit();
			
			// because we're still in session we cannot simply add our own collection we should call on get from the entity.
			List<InherittedClass> children = superOne.getChildren();
			children.add(iClass);
			children.add(iClass2);
			
			db.update(superTwo);
			db.update(superOne);
			db.commit();
			
			// lets see if fetch  is working correctly
			SuperClass superClass = new SuperClass();
			superClass = db.from(superClass).primaryKey().is(4L).selectFirst();
			
			assertEquals(superClass.getPartner().getId(), superTwo.getId());
			assertTrue(superClass.getChildren().size() == 2);
			
			for (InherittedClass child: superClass.getChildren()) {
				assertEquals(child.getSuperClass().getId(), superClass.getId());
				if (child.getId() == 2)
					assertEquals(child.getPartner().getId(), iClass3.getId());
			}
			tearDown();
		}
		catch (Exception e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
