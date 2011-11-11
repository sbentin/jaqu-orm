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
 * 28/02/2010		shai				 create
 */
package org.h2.jaqu;

/**
 * The type of inheritance supported by Jaqu fro entities.<br>
 * <ol>
 * <li>TABLE_PER_CLASS - each child class has its own table with all the fields from the parent</li>
 * <li>FIDCRIMINATOR - Single table for all children and parent, each discriminated using its own discriminator letter. </li>
 * </ol>
 * 
 * @author shai
 */
public enum InheritedType {
	TABLE_PER_CLASS, DISCRIMINATOR
}
