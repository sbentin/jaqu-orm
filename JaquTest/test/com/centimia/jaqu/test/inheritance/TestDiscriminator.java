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

import java.util.ArrayList;
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
			
			// currently both the object and the db have no record of a relationship between super and children.
			// we can do one of two things.
			// One - we can call superOne.getChildren() this will return an empty JaquList you can use to add the children to.
			// Two - we can create our own new List. Because there is no relationships Jaqu can handle this as well. Note that if there
			//		 where relationships in the db this action will basically replace those relationships.
			List<InherittedClass> children = new ArrayList<>();
			children.add(iClass);
			children.add(iClass2);
			superOne.setChildren(children);
			
			db.update(superTwo);
			db.update(superOne);
			db.commit();
			
			// currently doing an update/insert/delete does not effect the multi call cache.
			// therefore, while before we inserted the "InherittedClass" child objects and 
			// inserted and updated superClass object we have two issues. 
			// 1. The object tree we inserted should be taken care of by the developer. As an example,
			// 	  the InherittedClass child holds its parent, however when we inserted the child into
			// 	  the SuperClass parent we did not insert the SuperClass parent into the child which
			//	  makes our model not consistent. Note that the update and insert them selves do create
			// 	  a consistent persistent model of our entities thus the next bit of code will get a new
			//	  SuperClass instance and its children will exist and each child will have its parent, unlike
			//	  the original SuperClass object we saved. Our next test shows this:			
			for (InherittedClass child: superOne.getChildren()) {
				// the saved model is not consistent
				assertNull(child.getSuperClass());
			}
			// 2. The second issue is that when our flow is as it is here in this test, the model saved and
			// 	  the model retrieved from the database will not be the same instances. This was a design decision
			//	  because we think that a scenario when you hold an entity that has your business model and save it
			// 	  and then you read the same exact data from the database in the same session, does not exist.
			SuperClass superClass = new SuperClass();
			superClass = db.from(superClass).primaryKey().is(superOne.getId()).selectFirst();
			
			assertNotSame(superClass, superOne);
			assertEquals(superClass.getPartner().getId(), superTwo.getId());
			assertEquals(2, superClass.getChildren().size());
			
			for (InherittedClass child: superClass.getChildren()) {
				assertNotNull(child.getSuperClass());
				assertEquals(child.getSuperClass().getId(), superClass.getId());
				if (2 == child.getId())
					assertEquals(child.getPartner().getId(), iClass3.getId());
			}
			db.close();
			tearDown();
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}

}
