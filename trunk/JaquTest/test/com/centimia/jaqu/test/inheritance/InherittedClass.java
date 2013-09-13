/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
 */

/*
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Jul 7, 2013			shai

*/
package com.centimia.jaqu.test.inheritance;

import com.centimia.orm.jaqu.InheritedType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Inherited;
import com.centimia.orm.jaqu.annotation.Many2One;
import com.centimia.orm.jaqu.annotation.Table;

/**
 * @author shai
 *
 */
@Entity
@Table(name="DISCRIMINATOR")
@Inherited(inheritedType=InheritedType.DISCRIMINATOR, DiscriminatorValue='I')
public class InherittedClass extends SuperClass {

	@Column(name="AGE")
	private Integer age;
	
	@Many2One(relationFieldName="children")
	private SuperClass superClass;
	
	public InherittedClass() {}

	/**
	 * @return the age
	 */
	public Integer getAge() {
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(Integer age) {
		this.age = age;
	}

	/**
	 * @return the superClass
	 */
	public SuperClass getSuperClass() {
		return superClass;
	}

	/**
	 * @param superClass the superClass to set
	 */
	public void setSuperClass(SuperClass superClass) {
		this.superClass = superClass;
	}
}
