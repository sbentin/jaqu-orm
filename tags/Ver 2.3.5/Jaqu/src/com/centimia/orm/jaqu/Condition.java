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
package com.centimia.orm.jaqu;

import com.centimia.orm.jaqu.annotation.Entity;

/**
 * A condition contains one or two operands and a compare operation.
 *
 * @param <A> the operand type
 */
class Condition<A> implements Token {
    CompareType compareType;
    A x, y;

    Condition(A x, A y, CompareType compareType) {
        this.compareType = compareType;
        this.x = x;
        this.y = y;
    }

    /**
     * @see com.centimia.orm.jaqu.Token#appendSQL(SQLStatement, Query)
     */
    @SuppressWarnings("rawtypes")
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
    	query.appendSQL(stat, x);
        stat.appendSQL(" ");
        stat.appendSQL(compareType.getString());
        if (compareType.hasRightExpression()) {
            stat.appendSQL(" ");
            // check if a relation type
            if (y != null && y.getClass().getAnnotation(Entity.class) != null)
            	query.appendSQL(stat, query.getDb().factory.getPrimaryKey(y));
            else if (y != null && y.getClass().isEnum()) {            	
            	switch (query.getSelectColumn(x).getFieldDefinition().type) {
            		case ENUM: query.appendSQL(stat, y.toString()); break;
            		case ENUM_INT: query.appendSQL(stat, ((Enum)y).ordinal()); break;
            		default: query.appendSQL(stat, y); break;
            	}            	
            }
            else
            	query.appendSQL(stat, y);
        }
    }
}
