/*
 * Copyright (c) 2007-2016 Centimia Ltd.
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
package com.centimia.orm.jaqu.util;

/**
 * This utility class contains functions related to class loading. There is a mechanism to restrict class loading.
 */
public class ClassUtils {

	private ClassUtils() {
	// utility class
	}

	@SuppressWarnings("unchecked")
	public static <X> Class<X> getClass(X x) {
		return (Class<X>) x.getClass();
	}

	public static Class<?> loadClass(String className) {
		try {
			return Class.forName(className);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
