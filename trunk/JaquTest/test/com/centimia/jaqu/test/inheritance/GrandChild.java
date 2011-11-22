package com.centimia.jaqu.test.inheritance;
import java.util.Date;

import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.annotation.Inherited;
import org.h2.jaqu.annotation.Table;


/**
 * 
 */

/**
 * 
 * @author shai
 */
@Entity
@Inherited
@Table(name="GRAND_CHILD")
public class GrandChild extends Child {
	private Integer age;
	
	public GrandChild(){
		
	}
	
	public GrandChild(int age, String name){
		super(null, new Date(System.currentTimeMillis()), name);
		this.setAge(age);
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(Integer age) {
		this.age = age;
	}

	/**
	 * @return the age
	 */
	public Integer getAge() {
		return age;
	}
}
