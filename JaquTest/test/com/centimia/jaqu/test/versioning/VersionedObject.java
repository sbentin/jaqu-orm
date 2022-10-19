/*
 * Copyright (c) 2020-2024 Shai Bentin & Centimia Inc..
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
package com.centimia.jaqu.test.versioning;

import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.annotation.Column;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.annotation.Table;
import com.centimia.orm.jaqu.annotation.Version;

/**
 * @author shai
 */
@Entity
@Table(name="VERSIONING")
public class VersionedObject {

	@PrimaryKey(generatorType = GeneratorType.IDENTITY)
	protected Long id;
	
	@Version
	@Column
	protected Integer version;
	
	@Column(name="VALUE")
	protected String valueToUpdate;
	
	public VersionedObject() {}

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
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * @return the valueToUpdate
	 */
	public String getValueToUpdate() {
		return valueToUpdate;
	}

	/**
	 * @param valueToUpdate the valueToUpdate to set
	 */
	public void setValueToUpdate(String valueToUpdate) {
		this.valueToUpdate = valueToUpdate;
	}

	/*
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {		
		return "id: " + id + ", version: " + version + ", value: " + valueToUpdate;
	}
}
