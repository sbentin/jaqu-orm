/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
 */

/*
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Jul 8, 2013			shai

*/
package com.centimia.orm.jaqu;


/**
 * A condition created specificly in order to identify cases where the query involves a definit and known primary key.
 * Used for dicriminator type inheritence queries.
 * 
 * @author shai
 */
class PkCondition<A> extends Condition<A> {

    PkCondition(A x, A y, CompareType compareType) {
        super(x, y, compareType);
    }
}
