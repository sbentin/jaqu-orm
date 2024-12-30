/**
 * 
 */
package com.centimia.jaqu.test.entity;

import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.PrimaryKey;
import com.centimia.orm.jaqu.annotation.Table;

/**
 * @author shai
 *
 */
@Entity
@Table(name="Phone")
public class PhoneTable {

	@PrimaryKey
	private Long id;
	private String num;
	private Boolean isPrimary;
	private Boolean bool1;	
	private Boolean bool2;
	private Person owner;
	
	public PhoneTable(){
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public Person getOwner() {
		return owner;
	}

	public void setOwner(Person owner) {
		this.owner = owner;
	}

	/**
	 * @return the isPrimary
	 */
	public Boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * @param isPrimary the isPrimary to set
	 */
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
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
}
