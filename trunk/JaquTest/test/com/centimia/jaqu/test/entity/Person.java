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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.h2.jaqu.CascadeType;
import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.annotation.Many2Many;
import org.h2.jaqu.annotation.One2Many;
import org.h2.jaqu.annotation.PrimaryKey;

/**
 * 
 * @author Shai Bentin
 *
 */
@Entity
public class Person implements Serializable{

	private static final long serialVersionUID = -3092309534462507642L;
	
	@PrimaryKey
	private Long id;
	private String firstName;
	private String lastName;
	private Person parent;
	
	/* Demonstrates a relationship to the same object via an existing other side field */
	@One2Many(relationFieldName="parent")
	private List<Person> children;
	
	/* Demonstrates a single sided relationship, the FK is maintained in the DB only */
	@One2Many(relationFieldName="owner", eagerLoad=true)
	private Set<Phone> phones;
	
	/* Demonstrates a relationship which is maintained by a relation table */
	@One2Many(joinTableName="address_for_person", relationColumnName="address", cascadeType=CascadeType.DELETE)
	private List<Address> addresses;
	
	@Many2Many(joinTableName="workplace_for_person", relationColumnName="workPlaces", relationFieldName="persons")
	private List<WorkPlace> workPlaces;
	
	public Person() {}
	
	public Person(Long id, String firstName, String lastName) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public static List<Person> getSomeData(){
		ArrayList<Person> persons = new ArrayList<Person>();
		Person shai = new Person(1L, "Shai", "Bentin");
		HashSet<Phone> phoneList = new HashSet<Phone>();
		phoneList.add(new Phone(1L, "1234567"));
		phoneList.add(new Phone(2L, "98765432"));
		shai.setPhones(phoneList);
		ArrayList<Address> addresses = new ArrayList<Address>();
		addresses.add(new Address(1L, "street1", "city1", "Somewhere"));
		addresses.add(new Address(2L, "street2", "city2", "Nowhere"));
		shai.setAddresses(addresses);
		ArrayList<WorkPlace> workplaces = new ArrayList<WorkPlace>();
		workplaces.add(new WorkPlace(1L, "unemployed"));
		shai.setWorkPlaces(workplaces);
		persons.add(shai);
		
		Person einat = new Person(2L, "Einat", "Bentin");
		phoneList = new HashSet<Phone>();
		phoneList.add(new Phone(3L, "1234567"));
		phoneList.add(new Phone(4L, "98765432"));
		einat.setPhones(phoneList);
		addresses = new ArrayList<Address>();
		addresses.add(new Address(3L, "street1", "city1", "Somewhere"));
		addresses.add(new Address(4L, "street2", "city2", "Nowhere"));
		einat.setAddresses(addresses);
		workplaces = new ArrayList<WorkPlace>();
		workplaces.add(new WorkPlace(2L, "employed"));
		einat.setWorkPlaces(workplaces);
		persons.add(einat);
		
		Person roee = new Person(3L, "Roee", "Bentin");
		roee.parent = einat;
		persons.add(roee);
		
		return persons;
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
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the parent
	 */
	public Person getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Person parent) {
		this.parent = parent;
	}

	/**
	 * @return the children
	 */
	public List<Person> getChildren() {
		return children;
	}
	
	/**
	 * @param children the children to set
	 */
	public void setChildren(List<Person> children) {
		this.children = children;
	}

	/**
	 * @return the phones
	 */
	public Set<Phone> getPhones() {
		return phones;
	}

	/**
	 * @param phones the phones to set
	 */
	public void setPhones(Set<Phone> phones) {
		this.phones = phones;
	}

	/**
	 * @return the addresses
	 */
	public List<Address> getAddresses() {
		return addresses;
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return the workPlaces
	 */
	public List<WorkPlace> getWorkPlaces() {
		return workPlaces;
	}

	/**
	 * @param workPlaces the workPlaces to set
	 */
	public void setWorkPlaces(List<WorkPlace> workPlaces) {
		this.workPlaces = workPlaces;
	}
}
