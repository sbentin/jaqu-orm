/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;

import org.h2.jaqu.annotation.Entity;

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
     * @see org.h2.jaqu.Token#appendSQL(SQLStatement, Query)
     */
    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
    	query.appendSQL(stat, x);
        stat.appendSQL(" ");
        stat.appendSQL(compareType.getString());
        if (compareType.hasRightExpression()) {
            stat.appendSQL(" ");
            // check if a relation type
            if (y != null && y.getClass().getAnnotation(Entity.class) != null)
            	query.appendSQL(stat, query.getDb().factory.getPrimaryKey(y));
            else
            	query.appendSQL(stat, y);
        }
    }
}
