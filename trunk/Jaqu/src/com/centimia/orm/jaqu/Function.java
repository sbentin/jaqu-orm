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

import com.centimia.orm.jaqu.dialect.Functions;
import com.centimia.orm.jaqu.util.Utils;

/**
 * This class provides static methods that represents common SQL functions.
 * Use as a select method parameter, or in an inner class parameter of selct methods.<br/>
 * example:<pre>
 * db.from(p).where(p.id).biggerThan(8).select(Function.max(p.name));
 * </pre>
 * This will give a list with a single String object that has the maximum value of name for records with Id bigger than 8.
 * Same as:
 * <pre>
 * select max(name) from [table] where id > 8;
 * </pre>
 */
public class Function implements Token {

    // must be a new instance
    private static final Long COUNT_STAR = Long.valueOf(0);

    protected Object[] x;
    protected String name;

    protected Function(String name, Object... x) {
        this.name = name;
        this.x = x;
    }

    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        stat.appendSQL(name).appendSQL("(");
        int i = 0;
        for (Object o : x) {
            if (i++ > 0) {
                stat.appendSQL(",");
            }
            query.appendSQL(stat, o, o.getClass().isEnum(), o.getClass());
        }
        stat.appendSQL(")");
    }

    public static Long count() {
        return COUNT_STAR;
    }

    /**
     * SQL Funcation 'LENGTH'
     * @param x
     * @return Integer
     */
    public static Integer length(Object x) {
        return Db.registerToken(
            Utils.newObject(Integer.class), new Function("LENGTH", x));
    }

    /**
     * SQL function Sum
     * @param x
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T sum(T x) {
        return (T) Db.registerToken(
            Utils.newObject(x.getClass()), new Function("SUM", x));
    }

    public static Long count(Object x) {
        return Db.registerToken(
            Utils.newObject(Long.class), new Function("COUNT", x));
    }

    public static Boolean isNull(Object x) {
        return Db.registerToken(
            Utils.newObject(Boolean.class), new Function("", x) {
                public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                	query.appendSQL(stat, x[0], x[0].getClass().isEnum(), x[0].getClass());
                    stat.appendSQL(" IS NULL");
                }
            });
    }

    public static Boolean isNotNull(Object x) {
        return Db.registerToken(
            Utils.newObject(Boolean.class), new Function("", x) {
                public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                	query.appendSQL(stat, x[0], x[0].getClass().isEnum(), x[0].getClass());
                    stat.appendSQL(" IS NOT NULL");
                }
            });
    }

    public static Boolean not(Boolean x) {
        return Db.registerToken(
            Utils.newObject(Boolean.class), new Function("", x) {
                public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                    stat.appendSQL("NOT ");
                    query.appendSQL(stat, x[0], x[0].getClass().isEnum(), x[0].getClass());
                }
            });
    }

    public static Boolean or(Boolean... x) {
        return Db.registerToken(
                Utils.newObject(Boolean.class),
                new Function("", (Object[]) x) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                int i = 0;
                for (Object o : x) {
                    if (i++ > 0) {
                        stat.appendSQL(" OR ");
                    }
                    query.appendSQL(stat, o, o.getClass().isEnum(), o.getClass());
                }
            }
        });
    }

    public static Boolean and(Boolean... x) {
        return Db.registerToken(
                Utils.newObject(Boolean.class),
                new Function("", (Object[]) x) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                int i = 0;
                for (Object o : x) {
                    if (i++ > 0) {
                        stat.appendSQL(" AND ");
                    }
                    query.appendSQL(stat, o, o.getClass().isEnum(), o.getClass());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <X> X min(X x) {
        Class<X> clazz = (Class<X>) x.getClass();    
        if (clazz.isEnum()) {
        	X o = handleEnum(x, clazz);
        	return Db.registerToken(o, new Function("MIN", x));
        }
        X o = Utils.newObject(clazz);
        return Db.registerToken(o, new Function("MIN", x));
    }

    @SuppressWarnings("unchecked")
    public static <X> X max(X x) {
        Class<X> clazz = (Class<X>) x.getClass();
        if (clazz.isEnum()) {
        	X o = handleEnum(x, clazz);
        	return Db.registerToken(o, new Function("MAX", x));
        }
        X o = Utils.newObject(clazz);
        return Db.registerToken(o, new Function("MAX", x));
    }

    @SuppressWarnings("unchecked")
    public static <X extends Number> X avg(X x) {
        Class<X> clazz = (Class<X>) x.getClass();
        X o = Utils.newObject(clazz);
        return Db.registerToken(o, new Function("AVG", x));
    }
    
    /**
     * creates a function that if 'checkExpression' is null then put 'replacementValue'
     * 
     * @param <X>
     * @param checkExpression
     * @param replacementValue
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <X> X ifNull(X checkExpression, final Object replacementValue){
    	Class<X> clazz = (Class<X>) checkExpression.getClass();
    	X o = Utils.newObject(clazz);
    	
    	return Db.registerToken(o, new Function("IFNULL", checkExpression, replacementValue){
    		public <T> void appendSQL(SQLStatement stat, Query<T> query){
    			Dialect d = query.getDb().factory.getDialect();
    			name = d.getFunction(Functions.IFNULL);
    			stat.appendSQL(name).appendSQL("(");
    			// check_expression
    			query.appendSQL(stat, x[0], x[0].getClass().isEnum(), x[0].getClass());
    			stat.appendSQL(", ");
    			
    			// replacement value
    			if (null == replacementValue){
    				if (name.equals("COALESCE"))
    					stat.appendSQL("''");
    				else
    					stat.appendSQL("null");
    			}
    			else if (String.class.isAssignableFrom(replacementValue.getClass())){
    				stat.appendSQL("'" + replacementValue + "'");
    			}
    			else
    				stat.appendSQL(replacementValue.toString());
    			stat.appendSQL(")");
    		}
    	});
    }
    
    /**
     * This performs a like operation at column level and returns in that column a true or false value depending on what was checked.
     * @param x
     * @param pattern
     * @return Boolean
     */
    public static Boolean like(String x, String pattern) {
        Boolean o = Utils.newObject(Boolean.class);
        return Db.registerToken(o, new Function("LIKE", x, pattern) {
            public <T> void appendSQL(SQLStatement stat, Query<T> query) {
                stat.appendSQL("(");
                query.appendSQL(stat, x[0], x[0].getClass().isEnum(), x[0].getClass());
                stat.appendSQL(" LIKE ");
                query.appendSQL(stat, x[1], x[1].getClass().isEnum(), x[1].getClass());
                stat.appendSQL(")");
            }
        });
    }

    /**
	 * @param x
	 * @param clazz
	 * @return
	 */
	private static <X> X handleEnum(X x, Class<X> clazz) {
		X o = Utils.newObject(clazz);
		if (o == x)
			o = Utils.newEnum(clazz, 1);
		
		if (null == o)
			throw new JaquError("Doing an aggragate function on an enum type with a single value makes no sense");
		return o;
	}
}