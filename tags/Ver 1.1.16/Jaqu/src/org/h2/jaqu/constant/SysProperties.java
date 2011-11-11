/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 26/02/2011		shai				 create
 */
package org.h2.jaqu.constant;

/**
 * 
 * @author shai
 *
 */
public class SysProperties {

	/**
     * System property <code>h2.allowedClasses</code> (default: *).<br />
     * Comma separated list of class names or prefixes.
     */
    public static final String ALLOWED_CLASSES = getStringSetting("h2.allowedClasses", "*");

    /**
     * INTERNAL
     */
	public static String getStringSetting(String name, String defaultValue) {
		String s = getProperty(name);
		return s == null ? defaultValue : s;
	}

	private static String getProperty(String name) {
		try {
			return System.getProperty(name);
		}
		catch (Exception e) {
			// SecurityException
			// applets may not do that - ignore
			return null;
		}
	}
}
