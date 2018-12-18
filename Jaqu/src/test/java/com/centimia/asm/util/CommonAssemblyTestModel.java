/*
 * Copyright (c) 2008-2012 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.asm.util;

import java.util.List;

import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Transient;

/**
 * @author shai
 *
 */
@Entity 
public class CommonAssemblyTestModel {

	private String name;
	
	private String lastName;
	
	private List<TestB> list1;
	
	@Transient
	private List<TestB> list2;
	
	public CommonAssemblyTestModel() {}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the list1
	 */
	public List<TestB> getList1() {
		return list1;
	}

	/**
	 * @param list1 the list1 to set
	 */
	public void setList1(List<TestB> list1) {
		this.list1 = list1;
	}

	/**
	 * @return the list2
	 */
	public List<TestB> getList2() {
		return list2;
	}

	/**
	 * @param list2 the list2 to set
	 */
	public void setList2(List<TestB> list2) {
		this.list2 = list2;
	}
}
