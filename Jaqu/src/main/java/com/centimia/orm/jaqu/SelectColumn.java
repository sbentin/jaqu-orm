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

/**
 * This class represents a column of a table in a query.
 *
 * @param <T> the table data type
 */
class SelectColumn<T> {
    private SelectTable<T> selectTable;
    private FieldDefinition fieldDef;

    SelectColumn(SelectTable<T> table, FieldDefinition fieldDef) {
        this.selectTable = table;
        this.fieldDef = fieldDef;
    }

    void appendSQL(SQLStatement stat, String as) {
        if (selectTable.getQuery().isJoin()) {
            stat.appendSQL(selectTable.getAs() + "." + fieldDef.columnName);
        } else {
            stat.appendSQL(as + "." + fieldDef.columnName);
        }
    }

    FieldDefinition getFieldDefinition() {
        return fieldDef;
    }

    SelectTable<T> getSelectTable() {
        return selectTable;
    }
}