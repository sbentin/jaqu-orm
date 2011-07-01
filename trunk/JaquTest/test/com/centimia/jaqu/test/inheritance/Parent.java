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

import org.h2.jaqu.GeneratorType;
import org.h2.jaqu.annotation.MappedSuperclass;
import org.h2.jaqu.annotation.PrimaryKey;

/**
 * 
 * @author shai
 */
@MappedSuperclass
public class Parent {
	@PrimaryKey(generatorType=GeneratorType.IDENTITY)
	private Long id;
	
	private Date aDate;
	
	public Parent () {
		
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
	 * @return the date
	 */
	public Date getDate() {
		return aDate;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.aDate = date;
	}

}
