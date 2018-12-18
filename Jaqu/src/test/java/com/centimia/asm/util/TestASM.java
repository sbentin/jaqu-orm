package com.centimia.asm.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.annotation.JaquIgnore;

public class TestASM {
	private Double num;
	
	private TestB numB;
	
	private List<TestB> children;
	
	Db db;
	
	@JaquIgnore
	public boolean isLazy;
	
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
		if (numB != null && numB.isLazy) {
			try {
				if (null == db || db.isClosed())
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
	
	public List<TestB> getChildren() {
		if (children == null) {
			try {
				if (null == db || db.isClosed())
					throw new RuntimeException(
							"Cannot initialize 'Relation' outside an open session!!!. Try initializing field directly within the class.");
				Method method = db.getClass().getDeclaredMethod("getRelationFromDb", String.class, Object.class, Class.class);
				method.setAccessible(true);
				children = (List<TestB>) method.invoke(db, "children", this, TestB.class);
				method.setAccessible(false);
			} 
			catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return $orig_getChildren();
	}
	
	
	private List<TestB> $orig_getChildren() {
		return children;
	}

	public TestB $orig_getNumB() {
		return numB;
	}
}
