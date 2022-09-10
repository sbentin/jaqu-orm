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
package com.centimia.orm.jaqu.util;

import java.lang.reflect.Field;
import java.util.Comparator;

import com.centimia.orm.jaqu.JaquError;

/**
 * @author shai
 */
public class FieldComperator implements Comparator<Object> {

	private final Field field;
	
	public FieldComperator(Class<?> c, String fieldName) {
		try {
			this.field = ClassUtils.findField(c, fieldName);
			field.setAccessible(true);
		}
		catch (NoSuchFieldException | SecurityException e) {
			throw new JaquError("Field %s on class %s for sorting purposes.", fieldName, c.getName());
		}
	}
	
	/*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compare(Object o1, Object o2) {		
		try {
			Comparable a = (Comparable)field.get(o1);
			Comparable b = (Comparable)field.get(o2);
			if (null == a) {
				if (null == b)
					return 0;
				return 1;
			}
			if (null == b)
				return -1;
			return a.compareTo(b);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			throw new JaquError("Field %s on class %s must be of comparable type for sorting purposes.", 
					field.getName(), o1.getClass().getName());
		}
	}

}
