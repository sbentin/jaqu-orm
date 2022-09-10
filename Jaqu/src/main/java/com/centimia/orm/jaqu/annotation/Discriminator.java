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

/**
 * Use this annotation if your entity is a super type in an inheritance graph and you need to use a discriminator.<br>
 * <b>To be clear, if your class is not a {@link MappedSuperclass} which makes it abstract and is inherited by another {@link Entity} and the
 * inheritance strategy is discriminator use this annotation to let jaqu know what is the discriminator key for this entity.</b>
 *
 * @author Shai Bentin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Discriminator {
	/** the column in the DB holding the discriminator. The default is 'class'. If this column does not exist it will be created */
	String DiscriminatorColumn() default "class";
	
	/** the discriminator word to be used in the discriminator column to distinguish one object child from another. */
	char DiscriminatorValue() default ' ';
}
