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
 * 07/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

/**
 * Type of generator Jaqu supports for primary keys on Entities.
 * 
 * @author Shai Bentin
 */
public enum GeneratorType {
	SEQUENCE, IDENTITY, NONE;
}
