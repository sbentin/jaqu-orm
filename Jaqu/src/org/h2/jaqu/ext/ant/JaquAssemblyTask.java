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
 * 10/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu.ext.ant;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.h2.jaqu.ext.asm.JaquClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * This task is used in an Ant build script to support the post compilation of Jaqu Entities.
 * <p>
 * Example Use:
 * <p>
 * &lttaskdef classpathref="projectPath" name="JaquAssembly" classname="org.h2.jaqu.ext.ant.JaquAssemblyTask" /&gt<br>
 * 
 * &lttarget name="JaquAssembly" depends="main"&gt<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&ltJaquAssembly classOutputDirectory="${outputDir}" /&gt<br>
 * &lt/target&gt<br>
 * <br>
 * <b>Note: </b> Easier solution exists with the Jaqu Plugin for eclipse
 * @author Shai Bentin
 *
 */
public class JaquAssemblyTask extends Task {

	private String classOutputDirectory;
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		File outputDir = new File(classOutputDirectory);
		if (!outputDir.exists())
			throw new BuildException(String.format("Ouput dir %s does not exist!!!", classOutputDirectory));
		
		ArrayList<File> files = new ArrayList<File>();
		getAllFiles(outputDir, files);
		
		StringBuilder errors = new StringBuilder();
		for (File classFile: files) {
			try {
				assembleFile(classFile);
			}
			catch (Exception e) {
				errors.append(String.format("%s --> %s\n", classFile, e.getMessage()));
			}
		}
		if (errors.length() > 0)
			throw new BuildException(errors.toString());
	}

	private void assembleFile(File classFile) throws Exception {
		FileInputStream fis = new FileInputStream(classFile);
        byte[] b = null;       
       
    	ClassReader cr = new ClassReader(fis);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
            		
        JaquClassAdapter jaquClassAdapter = new JaquClassAdapter(cw);
        cr.accept(jaquClassAdapter, 0);
            		
        b = cw.toByteArray();
        fis.close();
        if (b != null) {
        	classFile.delete();
        	classFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(classFile);
    		fos.write(b);
    		fos.flush();
    		fos.close();
        }
 
	}

	private void getAllFiles(File outputDir, List<File> files){
		File[] fileList = outputDir.listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".class") || pathname.isDirectory())
					return true;
				return false;
			}
		});
		
		for (File file: fileList) {
			if (file.isDirectory()) {
				getAllFiles(file, files);
			}
			else
				files.add(file);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#getTaskName()
	 */
	@Override
	public String getTaskName() {
		return "Jaqu Assembly Task";
	}
	
	public void setClassOutputDirectory(String outputDir) {
		this.classOutputDirectory = outputDir;
	}
}
