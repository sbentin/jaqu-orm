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

import com.centimia.orm.jaqu.util.Utils;

/**
 * This class represents an incomplete condition.
 *
 * @param <A> the incomplete condition data type
 */
public class TestCondition<A> {

    private A x;

    public TestCondition(A x) {
        this.x = x;
    }

    public Boolean is(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("=", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0]);
                stat.appendSQL(" = ");
                query.appendSQL(stat, x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean bigger(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function(">", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0]);
                stat.appendSQL(" > ");
                query.appendSQL(stat, x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean biggerEqual(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function(">=", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0]);
                stat.appendSQL(" >= ");
                query.appendSQL(stat, x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean smaller(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("<", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0]);
                stat.appendSQL(" < ");
                query.appendSQL(stat, x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean smallerEqual(A y) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("<=", x, y) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0]);
                stat.appendSQL(" <= ");
                query.appendSQL(stat, x[1]);
                stat.appendSQL(")");
            }
        });
    }

    public Boolean like(A pattern) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("LIKE", x, pattern) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0]);
                stat.appendSQL(" LIKE ");
                query.appendSQL(stat, x[1]);
                stat.appendSQL(")");
            }
        });
    }

}