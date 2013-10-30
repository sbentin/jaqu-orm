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
 * 12/07/2011		shai				 create
 */
package com.centimia.jaqu.test.simple;

import com.centimia.orm.jaqu.Types;
import com.centimia.orm.jaqu.annotation.Column;

/**
 * 
 * @author shai
 *
 */
public class EnumUser {
	
	@Column(enumType=Types.ENUM_INT)
	private SEASON season;
	
	private String name;
	
	private String id;
	
	public EnumUser() {
		
	}

	public EnumUser(SEASON season, String name, String id) {
		super();
		this.season = season;
		this.name = name;
		this.id = id;
	}

	/**
	 * @return the season
	 */
	public SEASON getSeason() {
		return season;
	}

	/**
	 * @param season the season to set
	 */
	public void setSeason(SEASON season) {
		this.season = season;
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
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
