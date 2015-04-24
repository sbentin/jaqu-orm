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
package com.centimia.orm.jaqu.ext.asm;

import java.util.HashSet;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

/**
 * @author shai
 *
 */
public class JaquFieldVisitor extends FieldVisitor {

	private final HashSet<String> relationFields;
	private final String name;
	
	public JaquFieldVisitor(int api, FieldVisitor fv, String name, HashSet<String> relationFields) {
		super(api, fv);
		this.name = name;
		this.relationFields = relationFields;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String, boolean)
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (desc != null && desc.indexOf("com/centimia/orm/jaqu/annotation/JaquIgnore") != -1){
			this.relationFields.remove(name);
		}
		return super.visitAnnotation(desc, visible);
	}
}
