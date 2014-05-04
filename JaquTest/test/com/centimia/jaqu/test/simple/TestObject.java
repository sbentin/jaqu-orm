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
Created		   Dec 31, 2013			shai

*/
package com.centimia.jaqu.test.simple;

/**
 * @author shai
 *
 */
public class TestObject {
	Long id;
	String name;
	Boolean testResult;
	Double value;
	
	public TestObject() {}
	
	
	public TestObject(Long id, String name, Boolean testResult, Double value) {
		super();
		this.id = id;
		this.name = name;
		this.testResult = testResult;
		this.value = value;
	}

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
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the testResult
	 */
	public Boolean getTestResult() {
		return this.testResult;
	}

	/**
	 * @param testResult the testResult to set
	 */
	public void setTestResult(Boolean testResult) {
		this.testResult = testResult;
	}

	/**
	 * @return the value
	 */
	public Double getValue() {
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}
	
	
}
