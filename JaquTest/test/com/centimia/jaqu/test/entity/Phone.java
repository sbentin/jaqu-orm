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
 * 09/02/2010		Shai Bentin				 create
 */
package com.centimia.jaqu.test.entity;

import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.annotation.PrimaryKey;

/**
 * 
 * @author Shai Bentin
 */
@Entity
public class Phone {
	@PrimaryKey
	private Long id;
	private String num;
	private Person owner;
	
	public Phone() {
		
	}
	
	public Phone(Long id, String number, Person owner) {
		super();
		this.id = id;
		this.num = number;
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
	public void setNum(String number) {
		this.num = number;
	}

	/**
	 * @return the owner
	 */
	public Person getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Person owner) {
		this.owner = owner;
	}
}
