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
import java.util.IdentityHashMap;
import java.util.Map;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
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
    private String as;
    private TableDefinition<T> aliasDef;
    private JOIN_TYPE joinType;
    private ArrayList<Token> joinConditions = Utils.newArrayList();
    /** Holds the descriptor object */
    private T alias;

    @SuppressWarnings("unchecked")
    SelectTable(Query<T> query, T alias, JOIN_TYPE outerJoin) {
        this.alias = alias;
        this.query = query;
        this.joinType = outerJoin;
        aliasDef = (TableDefinition<T>) query.getDb().factory.getTableDefinition(alias.getClass());
        /** written this way to solve generic syntax problems */
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
        stat.appendSQL(" " + joinType.type + " ");
        appendSQL(stat);
        if (!joinConditions.isEmpty()) {
            stat.appendSQL(" ON ");
            for (Token token : joinConditions) {
                token.appendSQL(stat, q);
                stat.appendSQL(" ");
            }
        }
    }

    /*
     * find the column from the value
     */
    void appendSqlColumnFromField(SQLStatement stat, Object descValue) {
    	for (FieldDefinition def: aliasDef.getFields()) {
    		if (def.isSilent)
				continue;
    		if (descValue.equals(def.getValue(alias))){
    			// this is the field we're looking for
    			stat.appendSQL(as + "." + def.columnName);
    			return;
    		}
    	}
    }
    
    Query<T> getQuery() {
        return query;
    }

    void addConditionToken(Token condition) {
        joinConditions.add(condition);
    }

    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.ISelectTable#getJoins()
     */
    public Map<Object, String> getJoins(){
    	IdentityHashMap<Object, String> asList = new IdentityHashMap<Object, String>();
    	if (query.isJoin()){
    		for (SelectTable<?> jointTable: query.getJoins()){
    			asList.put(jointTable.getAlias(), jointTable.getAs());
    		}
    	}
    	return asList;
    }
    
    /*
     * (non-Javadoc)
     * @see com.centimia.orm.jaqu.ISelectTable#getJoinType()
     */
    public JOIN_TYPE getJoinType() {
        return joinType;
    }
    
    /* (non-Javadoc)
	 * @see com.centimia.orm.jaqu.ISelectTable#getAs()
	 */
    public String getAs() {
        return as;
    }
}