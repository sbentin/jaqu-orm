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
}
