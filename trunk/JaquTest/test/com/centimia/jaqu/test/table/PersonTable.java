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
 * 11/02/2010		Shai Bentin			 create
 */
package com.centimia.jaqu.test.table;

import org.h2.jaqu.Define;
import org.h2.jaqu.Table;

/**
 * 
 * @author shai
 *
 */
public class PersonTable implements Table {

	private Long id;
	private String firstName;
	private String lastName;
	private Long parent;
	
	public PersonTable() {
		
	}	
	
	public PersonTable(Long id, String firstName, String lastName, Long parent) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.parent = parent;
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
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
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
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the parent
	 */
	public Long getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Long parent) {
		this.parent = parent;
	}

	@Override
	public void define() {
		Define.tableName("person");
		Define.primaryKey(id);		
	}
}
