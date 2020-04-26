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
	private final HashSet<String> lazyLoadFields;
	private final String name;
	
	public JaquFieldVisitor(int api, FieldVisitor fv, String name, HashSet<String> relationFields, HashSet<String> lazyLoadFields) {
		super(api, fv);
		this.name = name;
		this.relationFields = relationFields;
		this.lazyLoadFields = lazyLoadFields;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String, boolean)
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (desc != null) {
			if (desc.indexOf("com/centimia/orm/jaqu/annotation/Converter") != -1)
				this.relationFields.remove(name);
			if (desc.indexOf("com/centimia/orm/jaqu/annotation/JaquIgnore") != -1 || desc.indexOf("com/centimia/orm/jaqu/annotation/Transient") != -1) {
				this.relationFields.remove(name);
			}
			else if (desc.indexOf("com/centimia/orm/jaqu/annotation/Lazy") != -1) {
				this.lazyLoadFields.add(name);
			}
		}
		return super.visitAnnotation(desc, visible);
	}
}
