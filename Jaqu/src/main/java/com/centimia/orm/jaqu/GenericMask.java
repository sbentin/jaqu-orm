/*
 * Copyright (c) 2020-2024 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.orm.jaqu;

/**
 * @author shai
 */
public final class GenericMask<K, A> {

	private final K o;
	private final Class<A> primaryKeyClass;

	public GenericMask(K o, Class<A> primaryKeyClass) {
		this.o = o;
		this.primaryKeyClass = primaryKeyClass;
	}

	/**
	 * returns the primaryKey class
	 * @return Class&lt;A&gt;
	 */
	public Class<A> mask() {
		return this.primaryKeyClass;
	}

	/**
	 *
	 * @return K - the object
	 */
	public K orig() {
		return this.o;
	}
}
