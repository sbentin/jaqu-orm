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

import java.util.List;

import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.InheritedType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Inherited;
import com.centimia.orm.jaqu.annotation.One2Many;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.annotation.Table;

/**
 * @author shai
 *
 */
@Entity
@Table(name="DISCRIMINATOR")
@Inherited(inheritedType=InheritedType.DISCRIMINATOR, DiscriminatorValue='S')
public class SuperClass {

	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	
	@Column(name="FIRST_NAME")
	private String firstName;
	
	@Column(name="LAST_NAME")
	private String lastName;
	
	private SuperClass partner;
	
	@One2Many(childPkType=Long.class, eagerLoad=true, joinTableName="PARENT_TO_CHILD", relationFieldName="superClass")
	private List<InherittedClass> children;
	
	public SuperClass() {}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return this.lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the partner
	 */
	public SuperClass getPartner() {
		return partner;
	}

	/**
	 * @param partner the partner to set
	 */
	public void setPartner(SuperClass partner) {
		this.partner = partner;
	}

	/**
	 * @return the children
	 */
	public List<InherittedClass> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<InherittedClass> children) {
		this.children = children;
	}
}
