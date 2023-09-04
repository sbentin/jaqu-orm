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
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Jul 8, 2013			shai

*/
package com.centimia.orm.jaqu;

/**
 *
 * @author shai
 */
class PkInCondition<A> extends InCondition<A> {

	PkInCondition(Object x, A[] y, CompareType compareType) {
		super(x, y, compareType);
	}
}
