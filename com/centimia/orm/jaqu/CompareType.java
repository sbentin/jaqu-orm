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

/**
 * An enumeration of compare operations.
 */
public enum CompareType {
    EQUAL("=", true),
    BIGGER(">", true),
    BIGGER_EQUAL(">=", true),
    SMALLER("<", true),
    SMALLER_EQUAL("<=", true),
    NOT_EQUAL("<>", true),
    BETWEEN("BETWEEN", true),
    IS_NOT_NULL("IS NOT NULL", false),
    IS_NULL("IS NULL", false),
    LIKE("LIKE", true),
    IN("IN", false),
    NOT_IN("NOT IN", false);

    private String text;
    private boolean hasRightExpression;

    CompareType(String text, boolean hasRightExpression) {
        this.text = text;
        this.hasRightExpression = hasRightExpression;
    }

    String getString() {
        return text;
    }

    boolean hasRightExpression() {
        return hasRightExpression;
    }

}