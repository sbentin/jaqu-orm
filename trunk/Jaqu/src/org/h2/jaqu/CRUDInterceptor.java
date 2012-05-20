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
Created		   Apr 23, 2012			shai

*/
package org.h2.jaqu;

/**
 * @author shai
 *
 */
public interface CRUDInterceptor {

	public abstract void onInsert(Object t);
	
	public abstract void onMerge(Object t);
	
	public abstract void onUpdate(Object t);
	
	public abstract void onDelete(Object t);
}
