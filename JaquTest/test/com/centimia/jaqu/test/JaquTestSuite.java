/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure. 
 *  
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group, Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 08/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.centimia.jaqu.test.entity.AutoCommitTest;
import com.centimia.jaqu.test.entity.EntityDeleteTest;
import com.centimia.jaqu.test.entity.EntityInsertTest;
import com.centimia.jaqu.test.entity.EntityMergeTetst;
import com.centimia.jaqu.test.entity.EntityNoUpdateFieldTest;
import com.centimia.jaqu.test.entity.EntitySequenceIdentityTest;
import com.centimia.jaqu.test.entity.EntitySessionTests;
import com.centimia.jaqu.test.entity.EntityUpdateTest;
import com.centimia.jaqu.test.entity.RelationWitVarcharPrimaryTest;
import com.centimia.jaqu.test.entity.TestInsertNoId;
import com.centimia.jaqu.test.entity.TestMultiRef;
import com.centimia.jaqu.test.inheritance.TestDiscriminator;
import com.centimia.jaqu.test.inheritance.TestInheritance;
import com.centimia.jaqu.test.simple.TestEnumType;
import com.centimia.jaqu.test.simple.TestFunctions;
import com.centimia.jaqu.test.simple.TestPojoUtils;
import com.centimia.jaqu.test.simple.TestQueryByExample;
import com.centimia.jaqu.test.simple.TestSelectAsMap;
import com.centimia.jaqu.test.simple.TestSimpleDelete;
import com.centimia.jaqu.test.simple.TestSimpleHaving;
import com.centimia.jaqu.test.simple.TestSimpleInnerJoin;
import com.centimia.jaqu.test.simple.TestSimpleInsert;
import com.centimia.jaqu.test.simple.TestSimpleObjectUpdate;
import com.centimia.jaqu.test.simple.TestSimpleOuterJoin;
import com.centimia.jaqu.test.simple.TestSimpleRightHandOuterJoin;
import com.centimia.jaqu.test.simple.TestSimpleSelect;
import com.centimia.jaqu.test.simple.TestUnificationMethods;
import com.centimia.jaqu.test.transaction.TransactionTests;
import com.centimia.orm.jaqu.StatementLogger;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/**
 * 
 * @author Shai Bentin
 *
 */
public class JaquTestSuite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getGlobal();
		logger.setLevel(Level.FINEST);
		
		junit.framework.TestSuite suite = suite();
		TestResult result = new TestResult();
		suite.run(result);
		StatementLogger.printStats();
		System.out.println("\n\nNumber of tests run: " + result.runCount());
		System.out.println("Number of tests successeded: " + (result.runCount() - result.failureCount() - result.errorCount()));
		if (!result.wasSuccessful()) {
			System.out.println("The following tests failed: ");
			Enumeration<TestFailure> enumeration = result.errors();
			while (enumeration.hasMoreElements()) {
				TestFailure failure = enumeration.nextElement();
				System.out.println(((TestCase)failure.failedTest()).getName() + " --> " + failure.exceptionMessage() + "\n\t\t\t" + failure.trace());
				System.out.println("----------------------------------------------------------------------------------------------------------\n");
			}
		}
	}

	public static junit.framework.TestSuite suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite("Test for com.centimia.jaqu.test");
		// clean all tables
		suite.addTest(new DropAll());
		// Test with simple objects. Each object represents a table, or a select result. These are plain pojos, they can have public or private
		// fields with accessors. Even if the tables underneath have PKs, the objects don't know about them so we can only do Sql operations and
		// the way to create relationships is by doing joins. The merge operation is not available.
		suite.addTest(new TestSimpleInsert());
		suite.addTest(new TestSimpleObjectUpdate());
		suite.addTest(new TestSimpleDelete());
		suite.addTest(new TestSimpleInnerJoin());
		suite.addTest(new TestSimpleRightHandOuterJoin());
		suite.addTest(new TestSimpleOuterJoin());		
		suite.addTest(new TestSimpleHaving());
		suite.addTest(new TestEnumType());
		suite.addTest(new TestFunctions());
		suite.addTest(new TestSimpleSelect());
		suite.addTest(new TestUnificationMethods());
		suite.addTest(new TestSelectAsMap());
		suite.addTest(new TestPojoUtils());
		
		// Test with entity objects.
		// You can also use them as entities and maintain object relations. If you want to use them as entities you first need to instrument them with the special ant task to 
		// create the classes for the framework to use.
		// You can use relationships between objects, thus you don't need to do the joins, these are done for you. One to One relations are always eagerly loaded.
		// One 2 Many can be eager loaded but the default is lazy loading. Many 2 Many are always lazy loaded. Cascade delete is also supported. The default is Cascade.NONE;
		suite.addTest(new EntityInsertTest());
		suite.addTest(new AutoCommitTest());
		suite.addTest(new EntityUpdateTest());
		suite.addTest(new EntityDeleteTest());
		suite.addTest(new EntityMergeTetst());
		suite.addTest(new EntityNoUpdateFieldTest());
		suite.addTest(new EntitySessionTests());
		suite.addTest(new EntitySequenceIdentityTest());
		suite.addTest(new TestInsertNoId());
		suite.addTest(new RelationWitVarcharPrimaryTest());
		suite.addTest(new TestMultiRef());
		
		// general test (work both on entities and pojos
		suite.addTest(new TestQueryByExample());
		
		// Inheritance Tests
		suite.addTest(new TestInheritance());
		suite.addTest(new TestDiscriminator());
		
		// Transactional Behavior Testing
		suite.addTest(new TransactionTests());
		
		return suite;
	}
}
