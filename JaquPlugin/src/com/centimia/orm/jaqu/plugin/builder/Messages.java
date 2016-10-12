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
package com.centimia.orm.jaqu.plugin.builder;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author shai
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.centimia.orm.jaqu.plugin.builder.messages"; //$NON-NLS-1$
	
	public static String JaquBuilder_2;
	public static String JaquBuilder_3;
	public static String JaquBuilder_4;
	public static String JaquBuilder_5;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
