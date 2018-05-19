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
 * 23/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use to set a field for a many to many relationship.
 * <p><b>Notes:<ol><li>Relation and Table must have a primary key, and primary key must be defined before relationship</li>
 * <li>The generic type is used to infer the other object, used to get the primary key type.</li>
 * <li>Lazy loading is always used, and the relationship is always two sided. (can be seen from both objects)</li></ol>
 * 
 * @author Shai Bentin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Many2Many {
	/** 
	 * The class type of the related object. 
	 */
	Class<?> childType() default Object.class;
	/** 
	 * The name of the DB table maintaining the relationship. Default no relation table 
	 */
	String joinTableName();
	/**
	 * The name of the column representing the other side of the relation. Must have value!
	 */
	String relationColumnName();
	/**
	 * The name of the field in the target object holding the other side of the relationship. If null 'this' tableName will be used!
	 */
	String relationFieldName();
	/**
	 * The other side of the relation primary key field could be named here. Correlates to the relation table. If not present JaQu attempts to find it. or fail
	 */
	Class<?> childPkType() default Object.class;
}
