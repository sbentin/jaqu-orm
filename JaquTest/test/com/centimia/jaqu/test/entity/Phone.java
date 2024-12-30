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

import java.io.Serializable;
import java.util.Objects;

import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.PrimaryKey;

/**
 * 
 * @author Shai Bentin
 */
@Entity
public class Phone implements Serializable {
	private static final long serialVersionUID = 738984308823986886L;

	@PrimaryKey
	private Long id;
	
	private String num;
	
	private Boolean isPrimary;
	
	private Boolean bool1;
	
	private Boolean bool2;
	
	public Phone() {
		
	}
	
	public Phone(Long id, String number) {
		super();
		this.id = id;
		this.num = number;
	}

	public Phone(Long id, String number, boolean isPrimary, boolean bool1, boolean bool2) {
		super();
		this.id = id;
		this.num = number;
		this.isPrimary = isPrimary;
		this.bool1 = bool1;
		this.bool2 = bool2;
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

	/*
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * @return the primary
	 */
	public Boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * @param primary the primary to set
	 */
	public void setIsPrimary(boolean primary) {
		this.isPrimary = primary;
	}

	/**
	 * @return the bool1
	 */
	public Boolean isBool1() {
		return bool1;
	}

	/**
	 * @param bool1 the bool1 to set
	 */
	public void setBool1(boolean bool1) {
		this.bool1 = bool1;
	}

	/**
	 * @return the bool2
	 */
	public Boolean isBool2() {
		return bool2;
	}

	/**
	 * @param bool2 the bool2 to set
	 */
	public void setBool2(boolean bool2) {
		this.bool2 = bool2;
	}

	/*
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Phone other = (Phone) obj;
		return Objects.equals(id, other.id);
	}
}
