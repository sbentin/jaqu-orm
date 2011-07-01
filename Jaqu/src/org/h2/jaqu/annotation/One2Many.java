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
 * Initial Developer: Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 22/02/2010		shai				 create
 */
package org.h2.jaqu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.h2.jaqu.CascadeType;

/**
 * Use to set a field for a one to many relationship.
 * <p><b>Notes:<br><ol><li>Relation and Table must have a primary key, and primary key must be defined before relationship</li>
 * <li>If using a relation table there is no sense of having the Parent object as a field of the child. Doing so will yield an error!</li></ol></b></p>
 * <p>
 * <b>Note:</b> The generic type is used to infer the other object, used to get the primary key type.
 * In oneToMany, lazyLoading is the default, but eager load can be done. The relationship can be Two sided (can be seen from both objects) or one sided (can be seen from parent only).
 * Also, implementation of the relation can be done by a relation table or by a foreign key relation, both are supported, if no relation table is given a foreign key is assumed.
 * 
 * @author Shai Bentin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface One2Many {
	/** 
	 * the class type of the related object. 
	 */
	Class<?> childType();
	/** 
	 * the name of the DB table maintaining the relationship. Default no relation table 
	 */
	String joinTableName() default "";
	/** 
	 * Relevant only when using a relation table. If null then column name will be used, otherwise denotes the name of the column representing 'this'
     *  side in the relationship table. 
     */
	String myFieldNameInRelation() default "";
	/**
	 * Relevant only when using a relation table, or when using a one way relationship from parent to child.<br>
	 * The name of the column representing the other side of the relation. (used in both cases, FK or relation table). Must have value!
	 */
	String relationColumnName() default "";
	/**
	 * The name of the field in the target object holding the other side of the relationship. If null 'this' tableName will be used! <b>Note:</b> In a single sided relationship,
	 * without a relationship, a column FK must exist in the child table, even if it does not exist in the object, and you must define relationColumnName
	 */
	String relationFieldName();
	/**
	 * When using a relationTable, the other side of the relation primary key field should be named here. otherwise null.
	 */
	Class<?> childPkType() default Object.class;
	/**
	 * Set to true if data should be loaded on object load. Default false
	 */
	boolean eagerLoad() default false;
	/**
	 * Only Cascade Delete is supported. Default is CascadeType.NONE, which means the related objects are not deleted when parent is deleted.
	 */
	CascadeType cascadeType() default CascadeType.NONE;
}
