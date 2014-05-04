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

import com.centimia.orm.jaqu.annotation.PrimaryKey;

/**
 * 
 * @author Shai Bentin
 *
 */
public class TestTable1 {
	@PrimaryKey
	private Long id;
	private String name;
	private String value;
	private Boolean bool;
	private SEASON season = SEASON.SPRING;
	
	public TestTable1(){
		
	}
	
	public TestTable1(Long id, String name, String value, boolean bool) {
		super();
		this.setId(id);
		this.setName(name);
		this.setValue(value);
		this.setBool(bool);
	}
	
	public static List<TestTable1> getSomeData(){
		ArrayList<TestTable1> data = new ArrayList<TestTable1>();
		data.add(new TestTable1(1L, "name1", "value1", true));
		data.add(new TestTable1(2L, "name2", "value2", true));
		data.add(new TestTable1(3L, "name3", "value3", false));
		return data;
	}
	
	public static List<TestTable1> getSomeData2(){
		ArrayList<TestTable1> data = new ArrayList<TestTable1>();
		data.add(new TestTable1(10L, "name10", "value10", true));
		data.add(new TestTable1(11L, "name11", "value11", true));
		data.add(new TestTable1(12L, "name12", "value12", false));
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

	/**
	 * @param bool the bool to set
	 */
	public void setBool(Boolean bool) {
		this.bool = bool;
	}

	/**
	 * @return the bool
	 */
	public Boolean getBool() {
		return bool;
	}

	/**
	 * @param season the season to set
	 */
	public void setSeason(SEASON season) {
		this.season = season;
	}

	/**
	 * @return the season
	 */
	public SEASON getSeason() {
		return season;
	}
}