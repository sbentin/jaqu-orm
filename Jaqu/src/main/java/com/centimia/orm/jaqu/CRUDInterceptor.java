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
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   Apr 23, 2012			shai

*/
package com.centimia.orm.jaqu;

import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.Interceptor;

/**
 * Implementations of this interface are used for intercepting CRUD operations on Entities {@link Entity}. It can also be used with POJOs.<br>
 * In order to use this interceptor you must mark your bean with the @interceptor {@link Interceptor} annotation.
 * 
 * @author shai
 * @since 2.0.0
 */
public interface CRUDInterceptor<K> {

	/**
	 * executed on entities before the entity is inserted
	 * @param t
	 */
	public abstract void onInsert(K t);
	
	/**
	 * executes on entities before the entity is merged
	 * @param t
	 */
	public abstract void onMerge(K t);
	
	/**
	 * executes on entities before the entity is updated
	 * @param t
	 */
	public abstract void onUpdate(K t);
	
	/**
	 * executes on entities before the entity is deleted
	 * @param t
	 */
	public abstract void onDelete(K t);
}
