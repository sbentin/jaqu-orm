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

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.TableDefinition.FieldType;

/**
 * An example option that skips all relationships and only works on then immediate fields.
 * @author shai
 */
public class BasicExampleOptions extends GeneralExampleOptions {

	public BasicExampleOptions(Object example, Db db) {		
		super(null);
		
		TableDefinition<?> tDef = JaquSessionFactory.define(example.getClass(), db);
		for (FieldDefinition fDef: tDef.getFields()) {
			if (fDef.isSilent || fDef.isExtension)
				continue; // this field can not be selected upon.
			else if (fDef.fieldType != FieldType.NORMAL) {
				addExcludeProp(fDef.field.getName());
			}
		}
	}
}