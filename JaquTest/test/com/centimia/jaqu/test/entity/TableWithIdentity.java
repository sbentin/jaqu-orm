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
 * 11/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.entity;

import java.util.List;

import com.centimia.orm.jaqu.CascadeType;
import com.centimia.orm.jaqu.GeneratorType;
import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.One2Many;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.annotation.Table;

import com.centimia.jaqu.test.inheritance.Child;

/**
 * 
 * @author Shai Bentin
 */
@Entity
@Table(name="TABLE_WITH_IDENTITY")
public class TableWithIdentity {
	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	private String name;
	
	@One2Many(cascadeType=CascadeType.DELETE,childType=Child.class,childPkType=Long.class,relationFieldName="tableId")
	private List<Child> children;
	
	public TableWithIdentity() {
		
	}
	
	public TableWithIdentity(String name) {
		this.id = null;
		this.name = name;
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
	 * @param children the children to set
	 */
	public void setChildren(List<Child> children) {
		this.children = children;
	}

	/**
	 * @return the children
	 */
	public List<Child> getChildren() {
		return children;
	}
}
