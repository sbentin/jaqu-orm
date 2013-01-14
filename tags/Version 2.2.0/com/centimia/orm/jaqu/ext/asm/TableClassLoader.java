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

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 01/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.ext.asm;

import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * An attempt to produce a Class Loader to load Entity classes. Problem is that there is a problem with the java classloader caching that
 * causes an exception after the 16 load cycle.
 * Don't use this class loader for now. Easier solution exists with the Jaqu Plugin for eclipse
 * @author Shai Bentin
 *
 */
public class TableClassLoader extends ClassLoader {

	public TableClassLoader() {
		super();
	}
	
	public TableClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (name.startsWith("java.") || name.startsWith("javax.")) {
			// if a regular java lib object automatically give to parent to load!!!
            return super.loadClass(name, resolve);
        }		
		
		Class<?> clazz = findClass(name);
		resolveClass(clazz);
		return clazz;
	}

	/**
     * Loads the class with the specified <a href="#name">binary name</a>.  The implementation does the following:
     * 
     * <p><ol>
     * 
     * <li><p> If the class is part of java, let the parent implementation handle this class</p></li>
     * <li><p> If not, check if the class has already been loaded by me or my parent, just to save time </p></li>
     * <li><p> If not, check if the class represents a {@link com.centimia.orm.jaqu.Table <tt>Table</tt>}. If so it is instrumented and loaded. </p></li>
     * <li><p> If not let the parent deal with it</p>
     * 
     * </ol>
     *
     * @param  name The <a href="#name">binary name</a> of the class
     * @return  The resulting <tt>Class</tt> object
     * @throws  ClassNotFoundException If the class could not be found
     */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// gets an input stream to read the bytecode of the class
        String resource = name.replace('.', '/') + ".class";
        InputStream is = getResourceAsStream(resource);
        
        byte[] b = null;
        
        try {
            ClassReader cr = new ClassReader(is);
            String[] iFaces = cr.getInterfaces();
            if (iFaces != null && iFaces.length != 0) {
	            for (String iFaceName: iFaces) {
	            	if (iFaceName.equals("com/centimia/orm/jaqu/Entity") || iFaceName.equals("com/centimia/orm/jaqu/MappedSuperclass")) {
	            		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
	            		
	            		JaquClassAdapter jaquClassAdapter = new JaquClassAdapter(Opcodes.ASM4, cw);
	            		cr.accept(jaquClassAdapter, 0);
	            		
	            		b = cw.toByteArray();
	            		return defineClass(name, b, 0, b.length);
	            	}            		
	            }
            }
            is.close();
//            if (b != null) {
//        		FileOutputStream fos = new FileOutputStream("c:/eclipse/workspaces/MIRACLE/Jaqu/bin/" + resource);
//        		fos.write(b);
//        		fos.flush();
//        		fos.close();
//            }
            return loadClass(name, true);
//            return super.defineClass(name, cr.b, 0, cr.b.length);
        }
        catch (Exception e) {
        	throw new ClassNotFoundException(name, e);
        }
	}	
	
	
}
