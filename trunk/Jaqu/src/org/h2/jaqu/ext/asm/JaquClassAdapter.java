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
 * Initial Developer: Centimia Inc.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 01/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu.ext.asm;

import java.util.HashSet;

import org.h2.jaqu.annotation.Entity;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * asm adapter which adds post compile data to the classes annotated with {@link Entity}
 * 
 * @author Shai Bentin
 *
 */
public class JaquClassAdapter extends ClassVisitor implements Opcodes {

	private String className;
	private HashSet<String> relationFields = new HashSet<String>();
	private boolean isEntityAnnotationPresent = false;
	private boolean isMappedSupperClass = false;
	
	public JaquClassAdapter(int api, ClassVisitor classVisitor) {
		super(api, classVisitor);
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.className = name;
		cv.visit(version, access, name, signature, superName, interfaces);
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (desc != null && desc.indexOf("org/h2/jaqu/annotation/Entity") != -1) {
			this.isEntityAnnotationPresent = true;
		}
		if (desc != null && desc.indexOf("org/h2/jaqu/annotation/MappedSuperclass") != -1){
			this.isMappedSupperClass = true;
		}
		return super.visitAnnotation(desc, visible);
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 * 
	 * this method visitor changes the name of the relation getter to $orig_[originalName]
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// 1. if name is in the list of o2m and return type is collection instrument add call to db.getRelationFromDb or db.getRelationArrayFromDb, only if value of field is null;
		if ((isEntityAnnotationPresent || isMappedSupperClass) && name.startsWith("get")) {
			// this is a getter check if it is a relation getter
			if (relationFields.contains(name.substring(3).toLowerCase())) {
				// this is a relationship.
				String newName = "$orig_" + name;
				
				char[] realName = name.substring(3).toCharArray();
				realName[0] = Character.toLowerCase(realName[0]);
				String fieldName = new String(realName);
				
				generateNewMethodBody(access, desc, signature, exceptions, name, newName, fieldName);
				
				return super.visitMethod(access, newName, desc, signature, exceptions);
			}
			else
				return super.visitMethod(access, name, desc, signature, exceptions);
		}
		else if (isEntityAnnotationPresent || isMappedSupperClass)
			return super.visitMethod(access, name, desc, signature, exceptions);
		
		else
			return null;
	}
	
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (isEntityAnnotationPresent || isMappedSupperClass) {
			// collect the fields that are relation by rule. (Collection type fields....)
			if (desc.indexOf("java/util/List") != -1 || desc.indexOf("java/util/Set") != -1 || desc.indexOf("java/util/Collection") != -1)
				relationFields.add(name.toLowerCase());
		}
		return super.visitField(access, name, desc, signature, value);
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitEnd()
	 */
	@Override
	public void visitEnd() {
		if (isEntityAnnotationPresent) {
			FieldVisitor fv = cv.visitField(ACC_PUBLIC+ACC_TRANSIENT, "db", "Lorg/h2/jaqu/Db;", null, null);
			fv.visitEnd();
		}

		super.visitEnd();
	}
	
	public final boolean isEntityAnnotationPresent() {
		return this.isEntityAnnotationPresent;
	}
	
	public final boolean isMappedSuperClass() {
		return this.isMappedSupperClass;
	}

	/**
	 * Generates the new method body, copy the old to a new method and connect them.
	 * 
	 * the structure of the new method is:<br>
	 * <br><b><div style="background:lightgray">
	 * public [CollectionType] [getterName]() {<br>
	 * <span style="margin-left: 2em;">if ([fieldName] == null){</span></br>
	 * <span style="margin-left: 2em;">try {</span></br>
	 * <span style="margin-left: 3em;">if (null == db)</span></br>
	 * <span style="margin-left: 4em;">throw new RuntimeException("Cannot initialize 'Relation' outside an open session!!!. Try initializing field directly within the class.");</span></br>
	 * <span style="margin-left: 3em;">Method method = db.getClass().getDeclaredMethod("getRelationFromDb", String.class, Object.class, Class.class);</span></br>
	 * <span style="margin-left: 3em;">method.setAccessible(true);</span></br>
	 * <span style="margin-left: 3em;">children = (List<TestTable>)method.invoke(db, [fieldName], this, TestTable.class);</span></br>
	 * <span style="margin-left: 3em;">method.setAccessible(false);</span></br>
	 * <span style="margin-left: 2em;">}</span></br>
	 * <span style="margin-left: 2em;">catch (Exception e) {</span></br>
	 * <span style="margin-left: 3em;">if (e instanceof RuntimeException)</span></br>
	 * <span style="margin-left: 4em;">throw (RuntimeException)e;</span></br>
	 * <span style="margin-left: 3em;">throw new RuntimeException(e.getMessage(), e);</span></br>
	 * <span style="margin-left: 2em;">}</span></br>
	 * <span style="margin-left: 1em;">}</span></br>
	 * <span style="margin-left: 1em;">return $orig_[getterName]();</span></br>
	 * }</div></b><br/>
	 * 
	 * @param access
	 * @param desc
	 * @param signature
	 * @param exceptions
	 * @param name
	 * @param newName
	 * @param fieldName
	 */
	private void generateNewMethodBody(int access, String desc, String signature, String[] exceptions, String name, String newName, String fieldName) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		mv.visitCode();
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(61, l3);
		mv.visitVarInsn(ALOAD, 0);
		String fieldSignature = signature.substring(signature.indexOf(')') + 1, signature.lastIndexOf('<')) + ";";
		mv.visitFieldInsn(GETFIELD, className, fieldName, fieldSignature);
		Label l4 = new Label();
		mv.visitJumpInsn(IFNONNULL, l4);
		mv.visitLabel(l0);
		mv.visitLineNumber(63, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lorg/h2/jaqu/Db;");
		Label l5 = new Label();
		mv.visitJumpInsn(IFNONNULL, l5);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(64, l6);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Cannot initialize a 'Relation' outside an open session!!!. Try initializing field directly within the class.");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l5);
		mv.visitLineNumber(65, l5);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lorg/h2/jaqu/Db;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
		mv.visitLdcInsn("getRelationFromDb");
		mv.visitInsn(ICONST_3);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_1);
		mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_2);
		mv.visitLdcInsn(Type.getType("Ljava/lang/Class;"));
		mv.visitInsn(AASTORE);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
		mv.visitVarInsn(ASTORE, 1);
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitLineNumber(66, l7);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V");
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitLineNumber(67, l8);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lorg/h2/jaqu/Db;");
		mv.visitInsn(ICONST_3);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitLdcInsn(fieldName);
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_2);
		String type = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>'));
		mv.visitLdcInsn(Type.getType(type));
		mv.visitInsn(AASTORE);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
		String cast = desc.substring(desc.indexOf("java/"), desc.indexOf(';'));
		mv.visitTypeInsn(CHECKCAST, cast);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldSignature);
		Label l9 = new Label();
		mv.visitLabel(l9);
		mv.visitLineNumber(68, l9);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V");
		mv.visitLabel(l1);
		mv.visitJumpInsn(GOTO, l4);
		mv.visitLabel(l2);
		mv.visitLineNumber(70, l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Exception"});
		mv.visitVarInsn(ASTORE, 1);
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitLineNumber(71, l10);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(INSTANCEOF, "java/lang/RuntimeException");
		Label l11 = new Label();
		mv.visitJumpInsn(IFEQ, l11);
		Label l12 = new Label();
		mv.visitLabel(l12);
		mv.visitLineNumber(72, l12);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, "java/lang/RuntimeException");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l11);
		mv.visitLineNumber(73, l11);
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Exception"}, 0, null);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "getMessage", "()Ljava/lang/String;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l4);
		mv.visitLineNumber(76, l4);
		mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, newName, desc);
		mv.visitInsn(ARETURN);
		Label l13 = new Label();
		mv.visitLabel(l13);
		mv.visitLocalVariable("this", "L"+className+";", null, l3, l13, 0);
		mv.visitLocalVariable("method", "Ljava/lang/reflect/Method;", null, l7, l2, 1);
		mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l10, l4, 1);
		mv.visitMaxs(7, 2);
		mv.visitEnd();
	}

	/**
	 * Returns true when the adapter has dealt with a JaQu annotated class and altered it.
	 * @return boolean
	 */
	public boolean isJaquAnnotated() {
		return isEntityAnnotationPresent || isMappedSupperClass;
	}
}
