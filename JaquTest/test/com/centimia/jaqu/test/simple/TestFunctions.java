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
			Double minValue = db.from(desc).selectFirst(Function.min(desc.getValue(), db));
			assertEquals(0D, minValue);
			
			// get the minimum row in the table
			SEASON minSeason = db.from(desc).where(desc.name).is("name1").selectFirst(Function.min(desc.season, db));
			assertEquals(SEASON.AUTOMN, minSeason);
			
			SEASON maxSeason = db.from(desc).where(desc.name).is("name1").selectFirst(Function.max(desc.season, db));
			assertEquals(SEASON.WINTER, maxSeason);
			
			TableForFunctions tff = db.from(desc).where(desc.name).is("name1").selectFirst(new TableForFunctions() {
				{
					id = desc.id;
					name = desc.name;
					season = Function.min(desc.season, db);
					value = desc.value;
				}
			});
			
			// get the maximum row in the table
			Double maxValue = db.from(desc).selectFirst(Function.max(desc.getValue(), db));
			assertEquals(5.3D, maxValue);
						
			// get the sum in the table
			Double sumValue = db.from(desc).selectFirst(Function.sum(desc.getValue(), db));
			assertEquals(48.0D, new BigDecimal(sumValue).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
						
			// get the count in the table
			// the following allows doing a count with some kind of a where clause to filter the table.
			Long countValue = db.from(desc).where(desc.getName()).isNotNull().selectFirst(Function.count(desc.getValue(), db));
			assertEquals(15, countValue.longValue());
			
			// same in a simpler way, by using count(*)
			countValue = db.from(desc).where(desc.getName()).isNotNull().selectCount();
			assertEquals(15, countValue.longValue());
			
			// the following does select count(*) on all the table
			countValue = db.from(desc).selectCount();
			assertEquals(16, countValue.longValue());
			
			// get the average value
			Double avgValue = db.from(desc).selectFirst(Function.avg(desc.getValue(), db));
			assertEquals(3.0D,  new BigDecimal(avgValue).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
			
			// Unlike the filter like this like returns true or false on condition match. Test all that their name is like 'name1' and return true
			List<TestObject> testObjects = db.from(desc).select(new TestObject() {;
				{
					id = desc.getId();
					name = desc.getName();
					testResult = Function.like(desc.getName(), "%me1", db);
					value = desc.value;
				}
			});
			
			for (int i = 0; i < 5; i++) {
				assertEquals(true, testObjects.get(i).getTestResult().booleanValue());
			}
			for (int i = 5; i < 15; i++) {
				assertEquals(false, testObjects.get(i).getTestResult().booleanValue());
			}
			
			// cool function that replaces 'null' values with some other default value you give on the fly (i.e. no default embedded in the object itself)
			tff = db.from(desc).where(desc.id).is(16L).selectFirst(new TableForFunctions() {
				{
					id = desc.getId();
					name = Function.ifNull(desc.getName(), "newName", db);
					value = desc.getValue();
				}
			});
			
			assertEquals(tff.getName(), "newName");
		}
		catch (Throwable e) {
			db.rollback();
			result.addError(this, e);
		}
	}
}
