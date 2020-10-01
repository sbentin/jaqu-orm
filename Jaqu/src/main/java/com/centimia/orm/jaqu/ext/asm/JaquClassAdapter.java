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

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 01/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.ext.asm;

import java.util.HashSet;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.centimia.orm.jaqu.annotation.Entity;

/**
 * asm adapter which adds post compile data to the classes annotated with {@link Entity}
 * 
 * @author Shai Bentin
 *
 */
public class JaquClassAdapter extends ClassVisitor implements Opcodes {

	private static final String $ORIG = "$orig_";
	private String className;
	private HashSet<String> relationFields = new HashSet<>();
	private HashSet<String> lazyLoadFields = new HashSet<>();
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
		if (desc != null && desc.indexOf("com/centimia/orm/jaqu/annotation/Entity") != -1) {
			this.isEntityAnnotationPresent = true;
		}
		if (desc != null && desc.indexOf("com/centimia/orm/jaqu/annotation/MappedSuperclass") != -1){
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
				String newName = $ORIG + name;				
				String fieldName = camelCase(name);
				
				generateNewMethodBody(access, desc, signature, exceptions, name, newName, fieldName);
				
				return super.visitMethod(access, newName, desc, signature, exceptions);
			}
			else if (lazyLoadFields.contains(name.substring(3).toLowerCase())) {
				// this is a O2O relationship which should be lazy loaded
				String newName = $ORIG + name;
				String fieldName = camelCase(name);
				
				generateLazyRelation(access, desc, exceptions, name, newName, fieldName);
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

	/**
	 * @param name
	 * @return
	 */
	private String camelCase(String name) {
		char[] realName = name.substring(3).toCharArray();
		realName[0] = Character.toLowerCase(realName[0]);
		return new String(realName);
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
		return new JaquFieldVisitor(Opcodes.ASM8, super.visitField(access, name, desc, signature, value), name.toLowerCase(), relationFields, lazyLoadFields);
	}
	
	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitEnd()
	 */
	@Override
	public void visitEnd() {
		if (isEntityAnnotationPresent) {
			FieldVisitor fv = cv.visitField(ACC_PUBLIC+ACC_TRANSIENT, "db", "Lcom/centimia/orm/jaqu/Db;", null, null);
			fv.visitEnd();
			
			fv = cv.visitField(ACC_PUBLIC, "isLazy", "Z", null, null);
			// add the jaquIgnore annotaion to the lazy field because we need to carry this field around the network but not persist it
			AnnotationVisitor av = fv.visitAnnotation("Lcom/centimia/orm/jaqu/annotation/Transient;", true);
			av.visitEnd();
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
	 * <pre>
	 * public [CollectionType] [getterName]() {
	 * 	if ([fieldName] == null){
	 * 		try {
	 * 			if (null == db || db.isClosed())
	 * 				throw new RuntimeException("Cannot initialize a 'Relation' outside an open session!!!. Try initializing the field directly within the class.");
	 * 			Method method = db.getClass().getDeclaredMethod("getRelationFromDb", String.class, Object.class, Class.class);
	 * 			method.setAccessible(true);
	 * 			children = (Collection<TestTable>)method.invoke(db, [fieldName], this, TestTable.class);
	 * 			method.setAccessible(false);
	 * 		}
	 * 		catch (Exception e) {
	 * 			if (e instanceof RuntimeException)
	 * 				throw (RuntimeException)e;
	 * 			throw new RuntimeException(e.getMessage(), e);
	 * 		}
	 * 	}
	 * return $orig_[getterName]();
	 * }
	 * </pre>
	 * </div>
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
		String fieldSignature = signature.substring(signature.indexOf(')') + 1, signature.lastIndexOf('<')) + ";";
		String type = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>'));
		String cast = desc.substring(desc.indexOf("java/"), desc.indexOf(';'));
		
		mv.visitCode();
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, fieldName, fieldSignature);
		Label l3 = new Label();
		mv.visitJumpInsn(IFNONNULL, l3);
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		Label l4 = new Label();
		mv.visitJumpInsn(IFNULL, l4);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/Db", "isClosed", "()Z", false);
		Label l5 = new Label();
		mv.visitJumpInsn(IFEQ, l5);
		mv.visitLabel(l4);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Cannot initialize a 'Relation' outside an open session!!!. Try initializing the field directly within the class.");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l5);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
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
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
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
		mv.visitLdcInsn(Type.getType(type));
		mv.visitInsn(AASTORE);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, cast);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldSignature);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false);
		mv.visitLabel(l1);
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Exception"});
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(INSTANCEOF, "java/lang/RuntimeException");
		Label l6 = new Label();
		mv.visitJumpInsn(IFEQ, l6);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, "java/lang/RuntimeException");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l6);
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Exception"}, 0, null);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "getMessage", "()Ljava/lang/String;", false);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, newName, desc, false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(7, 2);
		mv.visitEnd();
	}
		
	/**
	 * Generates the new method body, copy the old to a new method and connect them.
	 * the structure of the new method is:<br>
	 * <br><b><div style="background:lightgray">
	 * <pre>
	 * public [entityType] [getterName]() {
	 *	if ([field] == null) {
	 *		try {
	 *			if (null == db)
	 *				throw new RuntimeException("Cannot initialize 'Relation' outside an open session!!!. Try initializing field directly within the class.");
	 *			
	 *			[parentType] parent = this.getClass().newInstance();
	 *			[entityType] desc = TestB.class.newInstance();
	 *			
	 *			// get the primary key
	 *			Object pk = db.getPrimaryKey(this);
	 *			
	 *			// get the object
	 *			[entityValue] = db.from(desc).innerJoin(parent).on(parent.[entityValue]).is(desc).where(db.getPrimaryKey(parent)).is(pk).selectFirst();
	 *		}
	 *		catch (Exception e) {
	 *			if (e instanceof RuntimeException)
	 *				throw (RuntimeException)e;
	 *			throw new RuntimeException(e.getMessage(), e);
	 *		}
	 *	}
	 *	return $orig_[getterName]();
	 *  }
	 * </pre>
	 * </div>
	 * 
	 * @param access
	 * @param desc
	 * @param exceptions
	 * @param name - current method name
	 * @param newName - new method name (the orig...)
	 * @param fieldName
	 */
	public void generateLazyRelation(int access, String desc, String[] exceptions, String name, String newName, String fieldName) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, null, exceptions);
		String fieldSignature = desc.substring(desc.indexOf(')') + 1);
		String fieldClassName = desc.substring(desc.indexOf(')') + 2, desc.length() - 1);
		
		mv.visitCode();
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, fieldName, fieldSignature);
		Label l3 = new Label();
		mv.visitJumpInsn(IFNULL, l3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, fieldName, fieldSignature);
		mv.visitFieldInsn(GETFIELD, fieldClassName, "isLazy", "Z");
		mv.visitJumpInsn(IFEQ, l3);
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		Label l4 = new Label();
		mv.visitJumpInsn(IFNULL, l4);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/Db", "isClosed", "()Z", false);
		Label l5 = new Label();
		mv.visitJumpInsn(IFEQ, l5);
		mv.visitLabel(l4);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Cannot initialize 'Relation' outside an open session!!!. Try initializing field directly within the class.");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l5);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, className);
		mv.visitVarInsn(ASTORE, 1);
		mv.visitLdcInsn(Type.getType(fieldSignature));
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, fieldClassName);
		mv.visitVarInsn(ASTORE, 2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/Db", "getPrimaryKey", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
		mv.visitVarInsn(ASTORE, 3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/Db", "from", "(Ljava/lang/Object;)Lcom/centimia/orm/jaqu/QueryInterface;", false);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEINTERFACE, "com/centimia/orm/jaqu/QueryInterface", "innerJoin", "(Ljava/lang/Object;)Lcom/centimia/orm/jaqu/QueryJoin;", true);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(GETFIELD, className, fieldName, fieldSignature);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/QueryJoin", "on", "(Ljava/lang/Object;)Lcom/centimia/orm/jaqu/QueryJoinCondition;", false);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/QueryJoinCondition", "is", "(Ljava/lang/Object;)Lcom/centimia/orm/jaqu/QueryJoinWhere;", false);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "db", "Lcom/centimia/orm/jaqu/Db;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/Db", "getPrimaryKey", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/QueryJoinWhere", "where", "(Ljava/lang/Object;)Lcom/centimia/orm/jaqu/QueryCondition;", false);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/QueryCondition", "is", "(Ljava/lang/Object;)Lcom/centimia/orm/jaqu/QueryWhere;", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, "com/centimia/orm/jaqu/QueryWhere", "selectFirst", "()Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, fieldClassName);
		mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldSignature);
		mv.visitLabel(l1);
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Exception"});
		mv.visitVarInsn(ASTORE, 1);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(INSTANCEOF, "java/lang/RuntimeException");
		Label l6 = new Label();
		mv.visitJumpInsn(IFEQ, l6);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, "java/lang/RuntimeException");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l6);
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Exception"}, 0, null);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "getMessage", "()Ljava/lang/String;", false);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, newName, desc, false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(4, 4);
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
