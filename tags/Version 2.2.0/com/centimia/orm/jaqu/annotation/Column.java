/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *  
 * Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group, Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 22/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation if column name in underlying storage is different the name of the field.
 * @author shai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/** the name of the field. If annotation is used, this must have a value */
	String name() default "";
	
	/** Denotes the maximum length of the field. Optional */
	int length() default -1;

	/** If true then the field will be created as unique in the underlying DB. Has no meaning if the Table is not created by jaqu */
	boolean unique() default false;

	/** If true then this field will be created with not null. */
	boolean notNull() default false;
}
