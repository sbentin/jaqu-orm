/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
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
public class TestTable2 {

	private Long id;
	private String description;

	public TestTable2() {
		
	}
	
	TestTable2(Long testTable1Id, String desc) {
		super();
		this.id = testTable1Id;
		this.description = desc;
	}

	public static List<TestTable2> getSomeData() {
		ArrayList<TestTable2> data = new ArrayList<TestTable2>();
		data.add(new TestTable2(1L, "Description of 1"));
		data.add(new TestTable2(3L, "Description of 3"));
		return data;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long testTable1Id) {
		this.id = testTable1Id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String desc) {
		this.description = desc;
	}

}
