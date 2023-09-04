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
public class JaquConcurrencyException extends JaquError {
	private static final long serialVersionUID = -2668426367009536266L;

	public JaquConcurrencyException(String tableName, Class<?> class1, Object primaryKey, Number version) {
		super("Concurrency error on table: %s of %s with primaryKey %s. Looing for version %s but was different",
				tableName, class1, primaryKey, version);
	}
}
