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
 */
public class PhoneTable implements Table {

	private Long id;
	private String num;
	private Long owner;
	
	public PhoneTable() {
		
	}
	
	public PhoneTable(Long id, String num, Long owner) {
		super();
		this.id = id;
		this.num = num;
		this.owner = owner;
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
	 * @return the num
	 */
	public String getNum() {
		return num;
	}

	/**
	 * @param num the num to set
	 */
	public void setNum(String num) {
		this.num = num;
	}

	/**
	 * @return the owner
	 */
	public Long getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Long owner) {
		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see org.h2.jaqu.Table#define()
	 */
	@Override
	public void define() {
		Define.tableName("phone");
		Define.primaryKey(id);
	}
}
