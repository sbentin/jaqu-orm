/**
 * 
 */
package com.centimia.jaqu.test.simple;

import java.math.BigDecimal;
import java.util.List;

import junit.framework.TestResult;

import com.centimia.jaqu.test.JaquTest;
import com.centimia.orm.jaqu.Function;

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
			assertEquals(48.0D, new BigDecimal(tff.getValue()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
			
			// get the sum in the table
			tff = db.from(desc).selectFirst(new TableForFunctions(){
				{
					id = Function.count();
					name = desc.getName();
					value = desc.getValue();
				}
			});
			assertEquals(15, tff.getId().longValue());
			
			tff = db.from(desc).selectFirst(new TableForFunctions(){
				{
					id = desc.getId();
					name = desc.getName();
					value = Function.avg(desc.getValue());
				}
			});
			assertEquals(3.2D,  new BigDecimal(tff.getValue()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
			
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
			
			// get all that their name is like 'name1'
			List<TestObject> testObjects = db.from(desc).select(new TestObject(){
				{
					id = desc.getId();
					name = desc.getName();
					testResult = Function.like(desc.getName(), "%me1");
					value = desc.value;
				}
			});
			for (int i = 0; i < 5; i++) {
				assertEquals(true, testObjects.get(i).testResult.booleanValue());
			}
			for (int i = 5; i < 15; i++) {
				assertEquals(false, testObjects.get(i).testResult.booleanValue());
			}
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
