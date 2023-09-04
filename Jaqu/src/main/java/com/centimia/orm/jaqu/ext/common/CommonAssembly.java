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
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   May 6, 2012		shai

*/
package com.centimia.orm.jaqu.ext.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import com.centimia.orm.jaqu.ext.asm.JaquClassAdapter;
import com.centimia.orm.jaqu.ext.asm.SafeClassWriter;

/**
 * @author shai
 *
 */
public class CommonAssembly {

	private CommonAssembly() {}
	
	public static BuildStats assembleFiles(File outputDir, StringBuilder successReport, StringBuilder failedReport) {
		ArrayList<File> files = new ArrayList<>();
		getAllFiles(outputDir, files);
		
		int success = 0, failure = 0, ignored = 0;
		for (File classFile: files) {
			try {
				if (assembleFile(classFile)) {
					successReport.append(String.format("SUCCESS -- %s%n", classFile));
					success++;
				}
				else {
					successReport.append(String.format("IGNORED -- %s%n", classFile));
					ignored++;
				}
			}
			catch (Exception e) {
				failedReport.append(String.format("FAILED -- %s --> %s%n", classFile, e.getMessage()));
				failure++;
			}
		}
		return new BuildStats(success, failure, ignored);
	}
	
	public static boolean assembleFile(File classFile) throws IOException {
		FileInputStream fis = new FileInputStream(classFile);
        byte[] b = null;       
       
    	ClassReader cr = new ClassReader(fis);
        ClassWriter cw = new SafeClassWriter(cr, null, ClassWriter.COMPUTE_FRAMES);
          		
        JaquClassAdapter jaquClassAdapter = new JaquClassAdapter(Opcodes.ASM9, cw);
        cr.accept(jaquClassAdapter, 0);
        
        if (jaquClassAdapter.isJaquAnnotated()) {
	        b = cw.toByteArray();
	        fis.close();
	        if (b != null) {
	        	if (classFile.delete()) {
	        		classFile.createNewFile();
		            try (FileOutputStream fos = new FileOutputStream(classFile)) {
		            	fos.write(b);
		            	fos.flush();
		            }
	        	}	        	
	        }
	        return true;
        }
        else {
        	fis.close();
        	return false;
        }
	}

	private static void getAllFiles(File outputDir, List<File> files){
		File[] fileList = outputDir.listFiles(f -> f.getName().endsWith(".class") || f.isDirectory());
		
		for (File file: fileList) {
			if (file.isDirectory()) {
				getAllFiles(file, files);
			}
			else
				files.add(file);
		}
	}
}