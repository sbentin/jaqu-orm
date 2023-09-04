package com.centimia.asm.util;

import java.lang.reflect.Method;
import java.util.List;

import com.centimia.orm.jaqu.Db;
import com.centimia.orm.jaqu.annotation.Transient;

public class TestASM {
	private Double num;
	
	private TestB numB;
	
	private List<TestB> children;
	
	Db db;
	
	@Transient
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
	
	private Class<?>[] types = new Class<?>[] {Object.class};
	
	public TestB getNumB() {
		if (numB != null && numB.isLazy) {
			try {
				if (null == db || db.isClosed())
					return null;
				
				TestASM parent = this.getClass().getConstructor().newInstance();
				Class<?>[] innerTypes = new Class<?>[] {Object.class, TestB.class, Db.class, TestASM.class, Long.class};
				// get the primary key
				Object pk = db.getPrimaryKey(this);
				TestB result = null;
				for (Class<?> innerType: innerTypes) {
					TestB o = (TestB) innerType.getConstructor().newInstance();
					result = db.from(o).innerJoin(parent).on(parent.numB).is(o).where((Object)db.getPrimaryKey(parent)).is(pk).selectFirst();
					if (null != result) 
						break;
				}
				// get the object
				numB = result;
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
					return $orig_getChildren();
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
