/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu;


/**
 * This class provides utility methods to define primary keys, indexes, and set
 * the name of the table.
 */
public class Define {

    private static TableDefinition<?> currentTableDefinition;
    private static Table currentTable;

    /**
     * Defines the primary key columns for this table
     * @param columns
     */
    public static void primaryKey(Object... columns) {
        checkInDefine();
        currentTableDefinition.setPrimaryKey(columns);
    }

    /**
     * Defines the index columns for this table. Used when jaqu creates the tables.
     * @param columns
     */
    public static void index(Object... columns) {
        checkInDefine();
        currentTableDefinition.addIndex(columns);
    }

    public static void maxLength(Object column, int length) {
        checkInDefine();
        currentTableDefinition.setMaxLength(column, length);
    }

    /**
     * Use this method if table name in the DB is different then the object's name.<br>
     * Default GenerationType - NONE<br>
     *  
     * @param tableName
     */
    public static void tableName(String tableName) {
        currentTableDefinition.setTableName(tableName);
    }
    
    /**
     * Use this method to set the GenerationType of the table/ object!<br>
     * GenerationType.NONE, GenerationType.IDENTITY, GenerationType.SEQUENCE
     * 
     * @param genType
     * @param sequenceName
     */
    public static void tableName(GeneratorType genType, String sequenceName) {
        currentTableDefinition.setGenerationType(genType, sequenceName);
    }
    
    /**
     * Use this method if table name in the DB is different then the object's name, when there is a sequence or identity generation type for primary key.
     * <p>
     * <b>Note: IDENTITY and SEQUENCE are supported provided that the underlying DB supports the following calls:<br>
     * <ol>
     * <li> for sequence- select [sequenceName].nextval from dual</li>
     * <li> for identity - CALL IDENTITY()</li>
     * </ol>
     * </b>
     * </p>
     * @param tableName - if null the name of the class is used for table name
     * @param genType - the primary key generation type. If null, GenerationType.NONE is used
     * @param sequenceName - the name of the sequence to use. Use only when genType is sequence, other wise disregarded.
     */
    public static void tableName(String tableName, GeneratorType genType, String sequenceName) {
        currentTableDefinition.setTableName(tableName);
        currentTableDefinition.setGenerationType(genType, sequenceName);
    }
    
    static synchronized <T> void define(TableDefinition<T> tableDefinition, Table table) {
        currentTableDefinition = tableDefinition;
        currentTable = table;
        tableDefinition.mapObject(table);
        table.define();
        currentTable = null;
    }

    private static void checkInDefine() {
        if (currentTable == null) {
            throw new RuntimeException("This method may only be called " +
                "from within the define() method, and the define() method " +
                "is called by the framework.");
        }
    }
}
