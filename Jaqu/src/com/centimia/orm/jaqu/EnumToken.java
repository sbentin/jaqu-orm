/* Copyright (c) 2010-2016 Centimia Ltd. 
 * All rights reserved. Unpublished -- rights reserved
 * 
 * Use of a copyright notice is precautionary only, 
 * and does not imply publication or disclosure.
 * 
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION 
 * AND TRADE SECRETS OF CENTIMIA. USE, DISCLOSURE, 
 * OR REPRODUCTION IS PROHIBITED WITHOUT THE
 * PRIOR EXPRESS WRITTEN PERMISSION OF CENTIMIA Ltd. 
 */

/*
 * ISSUE    DATE   AUTHOR
 * ------- ------ -------- 
 * Created May 29, 2012 shai 
 */
package com.centimia.orm.jaqu;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;

/**
 * @author shai
 */
class EnumToken implements Token {

	private final CompareType	comapreType;
	private final Enum<?>[]	values;
	private final FieldDefinition	fDef;
	private final String as;
	
	public EnumToken(FieldDefinition fDef, CompareType comapreType, String as, Enum<?> ... values) {
		this.fDef = fDef;
		this.comapreType = comapreType;
		this.as = as;
		this.values = values;		
	}
	
	public <U> void appendSQL(SQLStatement stat, Query<U> query) {
		switch (comapreType) {
			case IN:
			case NOT_IN: {
				stat.appendSQL(as + "." + fDef.columnName + comapreType.getString());
				stat.appendSQL(" (");
				int i = 0;
				for (Enum<?> value : values) {
					if (i != 0) {
						stat.appendSQL(",");
					}
					stat.appendSQL("'" + value.name() + "'");
					i++;
				}
				stat.appendSQL(")");
				break;
			}
			case BETWEEN:
			case LIKE:
			case SMALLER:
			case SMALLER_EQUAL: {
				throw new JaquError("{%s} Is not supported for enum type", comapreType.name());
			}
			case IS_NULL:
			case IS_NOT_NULL: {
				stat.appendSQL(as + "." + fDef.columnName + comapreType.getString());
				break;
			}
			default: {
				stat.appendSQL(as + "." + fDef.columnName + comapreType.getString() + "'" + values[0].name() + "'");
				break;
			}
		}
	}
}
