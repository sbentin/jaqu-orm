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

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

import com.centimia.orm.jaqu.util.Logger;
import com.centimia.orm.jaqu.util.SimpleLogger;
import com.centimia.orm.jaqu.util.slf4jLogger;

/**
 * Utility class to optionally log generated statements to an output stream.<br>
 * Default output stream is System.out.<br>
 * Statement logging is disabled by default.
 * <p>
 * This class also tracks the counts for generated statements by major type.
 *
 */
public class StatementLogger {

	private static Logger logger;
	static {
		try {
			StatementLogger.class.getClassLoader().loadClass("org.slf4j.Logger");
			logger = new slf4jLogger();
		}
		catch (Throwable t){
			logger = new SimpleLogger();
		}
	}
	
	
    private static final AtomicLong SELECT_COUNT = new AtomicLong();
    private static final AtomicLong CREATE_COUNT = new AtomicLong();
    private static final AtomicLong INSERT_COUNT = new AtomicLong();
    private static final AtomicLong UPDATE_COUNT = new AtomicLong();
    private static final AtomicLong MERGE_COUNT = new AtomicLong();
    private static final AtomicLong DELETE_COUNT = new AtomicLong();
    private static final AtomicLong ALTER_COUNT = new AtomicLong();
    private static boolean isGathering = false;
    
    static void create(String statement) {
        CREATE_COUNT.incrementAndGet();
        log(statement);
    }

    static void insert(String statement) {
        INSERT_COUNT.incrementAndGet();
        log(statement);
    }

    static void update(String statement) {
        UPDATE_COUNT.incrementAndGet();
        log(statement);
    }

    static void merge(String statement) {
        MERGE_COUNT.incrementAndGet();
        log(statement);
    }

    static void delete(String statement) {
        DELETE_COUNT.incrementAndGet();
        log(statement);
    }

    static void select(String statement) {
        SELECT_COUNT.incrementAndGet();
        log(statement);
    }
    
    static void alter(String statement) {
        ALTER_COUNT.incrementAndGet();
        log(statement);
    }

    static void log(String statement) {
    	if (logger.isDebugEnabled()) {
    		isGathering = true;
    		logger.debug(statement);
    	}
    }

    static void error(String statement) {
    	logger.error(statement);
    }
    
    static void debug(String statement) {
    	logger.debug(statement);
    }
    
    static void info(String statement) {
    	logger.info(statement);		
	}
    
    static boolean isDebugEnabled() {
    	return logger.isDebugEnabled();
    }
    
    /**
     * print CRUD usage statistics: how many selects, inserts, deletes and such were performed.
     */
    public static void printStats() {
    	if (isGathering){
	    	StringBuilder stats = new StringBuilder("\n JaQu Runtime Statistics \n");    	
	    	stats.append("=========================\n");
	    	stats.append(printStat("CREATE", CREATE_COUNT));
	    	stats.append(printStat("INSERT", INSERT_COUNT));
	    	stats.append(printStat("UPDATE", UPDATE_COUNT));
	    	stats.append(printStat("MERGE", MERGE_COUNT));
	    	stats.append(printStat("DELETE", DELETE_COUNT));
	    	stats.append(printStat("SELECT", SELECT_COUNT));
	    	stats.append(printStat("ALTER", ALTER_COUNT));
	    	
	    	logger.info(stats.toString());
    	}
    	else
    		logger.info("Statistics were not gathered. To gather statistics please turn on showSQL on JaquFactory!!");
    }

    private static String printStat(String name, AtomicLong value) {
        if (value.get() > 0) {
            DecimalFormat df = new DecimalFormat("###,###,###,###");
            return("    " + name + "=" + df.format(value.get())+ "    \n");
        }
        return "";
    }
}
