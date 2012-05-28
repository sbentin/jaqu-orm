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

import java.util.ArrayList;

import com.centimia.orm.jaqu.util.ClassUtils;
import com.centimia.orm.jaqu.util.Utils;

/**
 * This class represents a table in a query.
 *
 * @param <T> the table class
 */
class SelectTable<T> implements ISelectTable<T> {

    private static int asCounter;
    private Query<T> query;
    private Class<T> clazz;
    private T current;
    private String as;
    private TableDefinition<T> aliasDef;
    private boolean outerJoin;
    private ArrayList<Token> joinConditions = Utils.newArrayList();
    /** Holds the descriptor object */
    private T alias;

    @SuppressWarnings("unchecked")
    SelectTable(Query<T> query, T alias, boolean outerJoin) {
        this.alias = alias;
        this.query = query;
        this.outerJoin = outerJoin;
        aliasDef = (TableDefinition<T>) query.getDb().factory.getTableDefinition(alias.getClass());
        /** written this way to solve generics syntax problems */
        clazz = ClassUtils.getClass(alias);
        as = "T" + asCounter++;
    }

    T getAlias() {
        return alias;
    }

    T newObject() {
        return Utils.newObject(clazz);
    }

    TableDefinition<T> getAliasDefinition() {
        return aliasDef;
    }

    void appendSQL(SQLStatement stat) {
    	stat.appendSQL(aliasDef.tableName + " " + as);
    }

    void appendSQLAsJoin(SQLStatement stat, Query<T> q) {
        if (outerJoin) {
            stat.appendSQL(" LEFT OUTER JOIN ");
        } else {
            stat.appendSQL(" INNER JOIN ");
        }
        appendSQL(stat);
        if (!joinConditions.isEmpty()) {
            stat.appendSQL(" ON ");
            for (Token token : joinConditions) {
                token.appendSQL(stat, q);
                stat.appendSQL(" ");
            }
        }
    }

    boolean getOuterJoin() {
        return outerJoin;
    }

    Query<T> getQuery() {
        return query;
    }

    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ISelectTable#getAs()
	 */
    public String getAs() {
        return as;
    }

    void addConditionToken(Token condition) {
        joinConditions.add(condition);
    }

    T getCurrent() {
        return current;
    }

    void setCurrent(T current) {
        this.current = current;
    }
}