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
Created		   Oct 7, 2013			shai

*/
package com.centimia.jaqu.test.entity;

import java.io.Serializable;

import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.PrimaryKey;

/**
 * 
 * @author shai
 */
@Entity
public class PrimaryRelation implements Serializable {
	
	private static final long	serialVersionUID	= -7955426768146796780L;

	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	
	@Column(name="MY_VALUE")
	private String myValue;
	
	@Column(name="VARCHAR_PRIMARY")
	private VarcharPrimary primary;
	
	public PrimaryRelation() {}

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
	 * @return the myValue
	 */
	public String getMyValue() {
		return this.myValue;
	}

	/**
	 * @param myValue the myValue to set
	 */
	public void setMyValue(String myValue) {
		this.myValue = myValue;
	}

	/**
	 * @return the primary
	 */
	public VarcharPrimary getPrimary() {
		return this.primary;
	}

	/**
	 * @param primary the primary to set
	 */
	public void setPrimary(VarcharPrimary primary) {
		this.primary = primary;
	}
}
