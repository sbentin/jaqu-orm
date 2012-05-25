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
package com.centimia.orm.jaqu.util;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class to optionally log generated statements to an output stream.<br>
 * Default output stream is System.out.<br>
 * Statement logging is disabled by default.
 * <p>
 * This class also tracks the counts for generated statements by major type.
 *
 */
public class StatementLogger {

    private static final AtomicLong SELECT_COUNT = new AtomicLong();
    private static final AtomicLong CREATE_COUNT = new AtomicLong();
    private static final AtomicLong INSERT_COUNT = new AtomicLong();
    private static final AtomicLong UPDATE_COUNT = new AtomicLong();
    private static final AtomicLong MERGE_COUNT = new AtomicLong();
    private static final AtomicLong DELETE_COUNT = new AtomicLong();
    private static final AtomicLong ALTER_COUNT = new AtomicLong();

    public static void create(String statement) {
        CREATE_COUNT.incrementAndGet();
        log(statement);
    }

    public static void insert(String statement) {
        INSERT_COUNT.incrementAndGet();
        log(statement);
    }

    public static void update(String statement) {
        UPDATE_COUNT.incrementAndGet();
        log(statement);
    }

    public static void merge(String statement) {
        MERGE_COUNT.incrementAndGet();
        log(statement);
    }

    public static void delete(String statement) {
        DELETE_COUNT.incrementAndGet();
        log(statement);
    }

    public static void select(String statement) {
        SELECT_COUNT.incrementAndGet();
        log(statement);
    }
    
    public static void alter(String statement) {
        ALTER_COUNT.incrementAndGet();
        log(statement);
    }

    private static void log(String statement) {
    	System.out.println(statement);
    }

    public static void printStats() {
    	System.out.println("JaQu Runtime Statistics");
    	System.out.println("=======================");
        printStat("CREATE", CREATE_COUNT);
        printStat("INSERT", INSERT_COUNT);
        printStat("UPDATE", UPDATE_COUNT);
        printStat("MERGE", MERGE_COUNT);
        printStat("DELETE", DELETE_COUNT);
        printStat("SELECT", SELECT_COUNT);
        printStat("ALTER", SELECT_COUNT);
    }

    private static void printStat(String name, AtomicLong value) {
        if (value.get() > 0) {
            DecimalFormat df = new DecimalFormat("###,###,###,###");
            System.out.println(name + "=" + df.format(CREATE_COUNT.get()));
        }
    }
}
