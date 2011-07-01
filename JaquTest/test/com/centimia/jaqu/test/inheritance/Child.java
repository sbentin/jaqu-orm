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
 * 01/03/2010		shai				 create
 */
package com.centimia.jaqu.test.inheritance;

import java.util.Date;

import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.annotation.Inherited;

import com.centimia.jaqu.test.entity.TableWithIdentity;

/**
 * 
 * @author shai
 */
@Entity
@Inherited
public class Child extends Parent {
	private String name;
	private TableWithIdentity tableId;
	
	public Child() {};
	
	public Child(Long id, Date date, String name) {
		this.setId(id);
		this.setDate(date);
		this.name = name;
	}

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
	 * @param tableId the tableId to set
	 */
	public void setTableId(TableWithIdentity tableId) {
		this.tableId = tableId;
	}

	/**
	 * @return the tableId
	 */
	public TableWithIdentity getTableId() {
		return tableId;
	}
}
