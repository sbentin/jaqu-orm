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
package com.centimia.jaqu.test.entity;

import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Lazy;
import com.centimia.orm.jaqu.annotation.PrimaryKey;

/**
 * @author shai
 *
 */
@Entity
public class TableD {
	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	
	private String value;
	
	@Lazy
	@Column(name="TABLE_C")
	public TableC tableC;

	/*
	 * Jaqu default constructor
	 */
	public TableD(){}

	public TableD(String value, TableC tableC) {
		this.value = value;
		this.tableC = tableC;
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the tableC
	 */
	public TableC getTableC() {
		return tableC;
	}

	/**
	 * @param tableC the tableC to set
	 */
	public void setTableC(TableC tableC) {
		this.tableC = tableC;
	}
}
