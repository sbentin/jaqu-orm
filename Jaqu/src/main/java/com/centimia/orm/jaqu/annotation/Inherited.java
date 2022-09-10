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
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 24/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.centimia.orm.jaqu.InheritedType;

/**
 * <b>Must use this annotation if your entity is inherited from another {@link Entity} or a {@link MappedSuperclass}. 
 * To be clear, if your class is a {@link MappedSuperclass} that inherits another {@link MappedSuperclass} you must also add this annotation.</b>
 * <p>
 * Using this annotation you can also supply more information as to the inheritance strategy your going to deploy in the underlying relational database.<p>
 * 
 * <ul>
 * <li>
 * <div><b>Table Per Class</b><br>
 * This is the default inheritance and the easiest to implement. It means that each entity has its own table with all the fields, including the inherited ones, in it.
 * It allows for AUTO incrementors like IDENTITY and SEQUENCE.
 * </li>
 * <br>
 * <li><b>Table with Discriminator</b><br>
 * This strategy, though not the only possible strategy, is also a performent strategy in a relational DB as it requires no joins or unions, and it allows for
 * AUTO incrementors like IDENTITY and SEQUENCE. When using this stratgety you should also supply the DiscriminatorColumn and DiscriminatorValue() value.
 * </li>
 * <p>
 * @author Shai Bentin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Inherited {
	/** Determines how the inheritance is mapped to the persistence store. In both cases there is a single table. One with Discriminator the other assumes only a single object is mapped per table (TABLE_PER_CLASS)*/
	InheritedType inheritedType() default InheritedType.TABLE_PER_CLASS;
	
	/** the column in the DB holding the discriminator. The default is 'class'. If this column does not exist it will be created */
	String DiscriminatorColumn() default "class";
	
	/** the discriminator word to be used in the discriminator column to distinguish one object child from another. */
	char DiscriminatorValue() default ' ';
}
