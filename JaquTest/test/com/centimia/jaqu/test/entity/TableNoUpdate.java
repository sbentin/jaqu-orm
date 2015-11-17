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
package com.centimia.jaqu.test.entity;

import java.io.Serializable;

import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.NoUpdateOnSave;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.annotation.Table;

/**
 * @author shai
 */
@Entity
@Table(name="TABLEB") 
public class TableNoUpdate implements Serializable {
	private static final long serialVersionUID = -8885669951926659772L;

	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	
	@Column(name="tableC")
	@NoUpdateOnSave
	private TableC aC;
	
	public TableNoUpdate() {
		
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
	 * @return the aC
	 */
	public TableC getaC() {
		return this.aC;
	}

	/**
	 * @param aC the aC to set
	 */
	public void setaC(TableC aC) {
		this.aC = aC;
	}
}
