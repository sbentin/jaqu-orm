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
Created		   Nov 1, 2012			shai

*/
package com.centimia.orm.jaqu;

/**
 * The mode of like
 * <ol>
 * <li>EXACT - look for exactly the string received (if the string includes '%' they will effect the result)</li>
 * <li>START - put a '%' at the beginning of the pattern</li>
 * <li>END - put a '%' at the end of the pattern</li>
 * <li>ANYWHERE - put '%' both at the beginning and end of pattern</li>
 * </ol>
 * @author shai
 */
public enum LikeMode {
	EXACT, START, END, ANYWHERE
}
