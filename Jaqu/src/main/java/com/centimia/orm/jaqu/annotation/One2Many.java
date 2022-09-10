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
 * 22/02/2010		shai				 create
 */
package com.centimia.orm.jaqu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.centimia.orm.jaqu.CascadeType;

/**
 * Use to set a field for a one to many relationship.
 * <p><b><ol><li>Relation and Table must have a primary key, and primary key must be defined before relationship</li>
 * <li>If using a relation table there is no sense of having the Parent object as a field of the child. <u>Doing so will yield an error!</u></li></ol></b></p>
 * <p>
 * <b>Note:</b> The generic type if exists, is used to infer the other object, otherwise use the 'childType' property.
 * In oneToMany, lazyLoading is the default, but eager load can be done. The relationship can be Two sided (can be seen from both objects) or one sided (can be seen from parent only).
 * Also, implementation of the relation can be done by a relation table or by a foreign key relation, both are supported, if no relation table is given a foreign key is assumed.
 * 
 * @author Shai Bentin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface One2Many {
	/** 
	 * Optional - the class type of the related object. Inferred by the generic type if exists.
	 */
	Class<?>[] childType() default {java.lang.Object.class};
	/** 
	 * the name of the DB table maintaining the relationship. (when it is maintained in a relation table and not by Foreign Key)<br> 
	 * Default no relation table 
	 */
	String joinTableName() default "";
	/**
	 * <b>Relevant only when using a relation table, since when using a Foreign Key the relation is maintained in the<br>child table and jaQu knows this from the child type</b><br>
	 * The name of the column representing the other side of the relation. If not present JaQu will use the simple java class name of the child entity.
	 */
	String relationColumnName() default "";
	/**
	 * The name of the field in the many side object holding the other side of the relationship.<br> 
	 * If left empty jaqu will assume that either there is a field on the other object which has a name identical to 'this' tableName or there is a column in the Db with 'this' tableName<p>
	 * <b>Note:</b> <ol><li>When using a relation table this is the name of the column representing the parent object in the relation table.</li>
	 * <li>In a single sided relationship, When not using a relationship table, a column FK must exist in the child table,<br>even if it does not exist in the object. In such a case instead of a field name one can write the column-name.</li>
	 * </ol></p> 
	 */
	String relationFieldName() default "";
	/**
	 * When using a relationTable to maintain the relationship, the other side of the relation primary key field java type could be named here. 
	 * If not named JaQu will attempt to find it, or fail to set up the relation table.
	 */
	Class<?> childPkType() default Object.class;
	/**
	 * Set to true if data should be loaded on object load. Be careful when using eager load so you don't pool to much of the DB on each call.
	 * Default false
	 */
	boolean eagerLoad() default false;
	/**
	 * Only Cascade Delete is supported. Default is CascadeType.NONE, which means the related objects are not deleted when parent is deleted.
	 */
	CascadeType cascadeType() default CascadeType.NONE;
	/** the name of a column in the other side of the relation that determines the order of the response */
	String orderBy() default "";
	/** the direction of order by. Default is ASC" */
	String direction() default "ASC";
}
