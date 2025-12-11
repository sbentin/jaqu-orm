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
package com.centimia.orm.jaqu;

/**
 * All DB types supported by Jaqu. Corresponds with the Dialect classes
 *
 * @see SQLDialect
 * @author Shai Bentin
 */
public enum Types {
	NONE,
	INTEGER,
	LONG,
	FLOAT,
	DOUBLE,
	BOOLEAN,
	BIGDECIMAL,
	STRING,
	UTIL_DATE,
	SQL_DATE,
	TIMESTAMP,
	BYTE,
	SHORT,
	BLOB,
	CLOB,
	ARRAY,
	TIME,
	COLLECTION,
	FK,
	ENUM,
	ENUM_INT,
	UUID,
	LOCALDATE,
	LOCALDATETIME,
	ZONEDDATETIME,
	LOCALTIME
}
