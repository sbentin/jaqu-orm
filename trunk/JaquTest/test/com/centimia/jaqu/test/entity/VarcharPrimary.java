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

import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.PrimaryKey;

/**
 * @author shai
 *
 */
@Entity
public class VarcharPrimary implements Serializable {

	private static final long	serialVersionUID	= 8779591688603511277L;

	@PrimaryKey
	@Column(name="PRIM_ARY", length=20)
	private String primary;
	
	@Column(name="SOME_VALUE")
	private String someValue;
	
	public VarcharPrimary() {}

	/**
	 * @return the primary
	 */
	public String getPrimary() {
		return this.primary;
	}

	/**
	 * @param primary the primary to set
	 */
	public void setPrimary(String primary) {
		this.primary = primary;
	}

	/**
	 * @return the someValue
	 */
	public String getSomeValue() {
		return this.someValue;
	}

	/**
	 * @param someValue the someValue to set
	 */
	public void setSomeValue(String someValue) {
		this.someValue = someValue;
	}
}
