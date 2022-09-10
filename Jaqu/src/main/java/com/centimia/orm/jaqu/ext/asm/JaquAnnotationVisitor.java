/*
 * Copyright (c) 2020-2024 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.orm.jaqu.ext.asm;

import java.util.HashMap;

import org.objectweb.asm.AnnotationVisitor;

/**
 * @author shai
 */
public class JaquAnnotationVisitor extends AnnotationVisitor {

	private final HashMap<String, String[]> abstractFields;
	private String name;
	
	public JaquAnnotationVisitor(int api, String name, AnnotationVisitor av, HashMap<String, String[]> abstractFields) {
		super(api, av);
		this.name = name;
		this.abstractFields = abstractFields;
	}

	/*
	 * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
	 */
	@Override
	public AnnotationVisitor visitArray(String name) {
		String[] fields = abstractFields.get(this.name);
		if (null == fields)
			abstractFields.put(this.name, new String[0]);
		this.av = av.visitArray(name);
		return this;
	}

	/*
	 * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
	 */
	@Override
	public void visit(String name, Object value) {
		av.visit(name, value);
		String[] fields = abstractFields.get(this.name);
		String s = value.toString();
		String[] tmp = new String[fields.length + 1];
		System.arraycopy(fields, 0, tmp, 0, fields.length);
		tmp[fields.length] = s;
		abstractFields.put(this.name, tmp);
	}

	/*
	 * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
	 */
	@Override
	public void visitEnd() {
		av.visitEnd();
	}
}
