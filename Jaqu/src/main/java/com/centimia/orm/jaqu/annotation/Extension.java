/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *  
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 2.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group, Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 08/06/2010		shai				 create
 */
package com.centimia.orm.jaqu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used on object fields which Jaqu persistence should ignore for insert and update and normal select.
 * <p>
 * Since these fields do not exist in the persistence db layer the only way to populate them using jaqu is by doing an extended select i.e:
 * <pre>
 * select.from()....select(new [The name of this entity with extensions](){
 * 	{static block where assignment happens}}
 * );
 * </pre>
 * 
 * Example:<br>
 * Say you have the following Object Entity:<br>
 * <pre>
 * private class Person {
 *  &#64PrimaryKey(GenerationType = GenerationType.IDENTITY)
 * 	private Long id;
 * 
 *  private String firstName;
 *  
 *  private String lastName;
 * 
 *  &#64Extension
 *  private String fullName;
 *  
 *  public Person() {}
 *  ... Getters and Setters
 *  }
 *  </pre>
 *  normal work with Person object will not populate fullName, it will simply be ignored. But you can do this:
 *  <pre>
 *  try (Db select = factory.getSession()) {
 *  	Person p = new Person();
 *  	List<Person> res = select.from(p).where(p.getId()).is(3).select(new Person() {
 *  		{
 *  			firstName = p.getFirstName();
 *  			lastName = p.getLastName();
 *  			fullName = Function.concat(select, null, p.getFirstName(), " ", p.getLastName());
 *  		}
 *  	});
 *  </pre>
 *  
 * @author shai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Extension {}
