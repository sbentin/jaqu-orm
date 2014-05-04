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
Created		   Dec 24, 2013			shai

*/
package com.centimia.jaqu.test.entity;

import java.io.Serializable;

import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.PrimaryKey;

/**
 * @author shai
 *
 */
@Entity
public class TableA implements Serializable {

	private static final long	serialVersionUID	= 3008823651586218238L;

	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	
	@Column(name="TableB")
	private TableB aB;
	
	@Column(name="Another")
	private TableB anotherB;
	
	public TableA() {
		
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
	 * @return the aB
	 */
	public TableB getaB() {
		return this.aB;
	}

	/**
	 * @param aB the aB to set
	 */
	public void setaB(TableB aB) {
		this.aB = aB;
	}

	/**
	 * @return the anotherB
	 */
	public TableB getAnotherB() {
		return this.anotherB;
	}

	/**
	 * @param anotherB the anotherB to set
	 */
	public void setAnotherB(TableB anotherB) {
		this.anotherB = anotherB;
	}
}
