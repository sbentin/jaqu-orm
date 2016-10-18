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
			if (fDef.isSilent)
				continue; // this field can not be selected upon.
			else if (fDef.fieldType != FieldType.NORMAL) {
				addExcludeProp(fDef.field.getName());
			}
		}
	}
}