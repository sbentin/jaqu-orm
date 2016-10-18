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
 * Only supports single table for class hierarchy with a DiscriminatorColumn and a DiscriminatorValue.
 * This strategy, though not the only possible strategy, is the most performent strategy in a relational DB as it requires no joins or unions, and it allows for
 * AUTO incrementors like IDENTITY and SEQUENCE. Because of the unique way Jaqu works with queries DBs, if you have an existing DB and you need to reflect these tables
 * in objects you have the option of declaring an object for each table and using a join query (These tables do not need to be entities but they can if you also want to
 * maintain relationship). A more complex option is to declare a third object which is a combination of the two and again map it using a join)
 * 
 * The strategy used here manes that there is a super class, which has no Entity mapping, i.e. it's a mapped supper class which is announced by the inherited child using extends.
 * Mapping of the super class fields should be done on the super class. These mappings will only be checked if the super class will have a @MappedSuperclass annotation. This is 
 * because the hierarchy could be deeper then one parent and not all parents are always mapped. 
 * 
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
