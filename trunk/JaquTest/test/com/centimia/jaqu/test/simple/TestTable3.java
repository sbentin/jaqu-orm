/*
 * Copyright (c) 2007-2008 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA LTD. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA, INC.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 08/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestTable3 {

	private Long id;
	private String name;
	private String value;
	
	public TestTable3(){
		
	}
	
	public TestTable3(Long id, String name, String value) {
		super();
		this.setId(id);
		this.setName(name);
		this.setValue(value);
	}
	
	public static List<TestTable3> getSomeData(){
		ArrayList<TestTable3> data = new ArrayList<TestTable3>();
		data.add(new TestTable3(1L, "name1", "valueFromTestTable1"));
		data.add(new TestTable3(2L, "name2", "valueFromTestTable2"));
		data.add(new TestTable3(3L, "name3", "valueFromTestTable3"));
		return data;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}