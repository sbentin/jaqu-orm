package com.centimia.asm.util;

import com.centimia.orm.jaqu.Db;

public class TestASM {
	private Double num;
	
	private TestB numB;
	
	Db db;
	
	public TestASM() {
		
	}

	public Double getNum() {
		return num;
	}

	public void setNum(Double num) {
		this.num = num;
	}

	public void setNumB(TestB numB) {
		this.numB = numB;
	}
	
	public TestB getNumB() {
		if (numB == null) {
			try {
				if (null == db)
					throw new RuntimeException("Cannot initialize 'Relation' outside an open session!!!. Try initializing field directly within the class.");
				
				TestASM parent = this.getClass().newInstance();
				TestB desc = TestB.class.newInstance();
				
				// get the primary key
				Object pk = db.getPrimaryKey(this);
				
				// get the object
				numB = db.from(desc).innerJoin(parent).on(parent.numB).is(desc).where(db.getPrimaryKey(parent)).is(pk).selectFirst();
			}
			catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException)e;
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return $orig_getNumB();
	}
	
	public TestB $orig_getNumB() {
		return numB;
	}
}
