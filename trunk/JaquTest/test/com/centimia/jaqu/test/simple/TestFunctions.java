/**
 * 
 */
package com.centimia.jaqu.test.simple;

import java.util.List;

import com.centimia.orm.jaqu.Function;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;

/**
 * Checks aggaragate function usage.
 * 
 * @author shai
 */
public class TestFunctions extends JaquTest {

	public String getName() {
		return "Aggragate Functions Test";
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		result.startTest(this);
		try {
			setUp();
			// we add rows to the DB
			db.insertAll(TableForFunctions.getData());
			
			db.commit();
			
			final TableForFunctions desc = new TableForFunctions();
			// get the minimum row in the table
			TableForFunctions tff = db.from(desc).selectFirst(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.min(desc.getValue());
				}
			});
			assertEquals(1.1D, tff.getValue());
			
			// get the maximum row in the table
			tff = db.from(desc).selectFirst(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.max(desc.getValue());
				}
			});
			assertEquals(5.3D, tff.getValue());
			
			// get the sum in the table
			tff = db.from(desc).selectFirst(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.sum(desc.getValue());
				}
			});
			assertEquals(48.0D, tff.getValue());
			
			// get the sum in the table
			tff = db.from(desc).selectFirst(new TableForFunctions(){
				{
					id = Function.count();
					name = desc.getName();
					value = desc.getValue();
				}
			});
			assertEquals(15, tff.getId().longValue());
			
			List<TableForFunctions> tffs= db.from(desc).groupBy(desc.getName()).select(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.min(desc.getValue());
				}
			});
			assertEquals(3, tffs.size());
			
			// get the maximum row in the table
			tffs = db.from(desc).groupBy(desc.getName()).select(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.max(desc.getValue());
				}
			});
			assertEquals(3, tffs.size());
			
			// get the sum in the table
			tffs = db.from(desc).groupBy(desc.getName()).select(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.sum(desc.getValue());
				}
			});
			assertEquals(3, tffs.size());
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
	
	class TestObject {
		Long id;
		String name;
		Boolean testResult;
		Double value;
	}
}
