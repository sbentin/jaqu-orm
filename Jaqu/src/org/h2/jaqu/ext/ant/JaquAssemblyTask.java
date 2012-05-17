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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.h2.jaqu.ext.common.BuildStats;
import org.h2.jaqu.ext.common.CommonAssembly;

/**
 * This task is used in an Ant build script to support the post compilation of Jaqu Entities.
 * <p>
 * Example Use:
 * <p>
 * <pre>
 * &lt;taskdef classpathref="projectPath" name="JaquAssembly" classname="org.h2.jaqu.ext.ant.JaquAssemblyTask"/&gt;
 * 
 * &lt;target name="JaquAssembly" depends="main"&gt;
 * 	&lt;!-- the outputDir is the directory where the original .class files before post compile reside --&gt;
 * 	&lt;JaquAssembly classOutputDirectory="${outputDir}"/&gt;
 * &lt;/target&gt;
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
			throw new BuildException(String.format("Output dir %s does not exist!!!", classOutputDirectory));
		
		StringBuilder report = new StringBuilder();
		BuildStats stats = CommonAssembly.assembleFiles(outputDir, report);
		if (stats.getFailure() > 0) {
			report.insert(0, "BUILD FAILED - converted " + stats.getSuccess() + " files, failed to convert " + stats.getFailure() + " files\n");
			throw new BuildException(report.toString());
		}
		System.out.println("BUILD SUCCESS - converted " + stats.getSuccess() + " files");
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
