/*
 * Copyright (c) 2010-2016 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF CENTIMIA. USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF CENTIMIA Ltd.
 */

/*
 ISSUE			DATE			AUTHOR
-------		   ------	       --------
Created		   May 6, 2012			shai

*/
package org.h2.jaqu.ext.gradle;

import java.io.File;

import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginConvention;
import org.h2.jaqu.ext.common.BuildStats;
import org.h2.jaqu.ext.common.CommonAssembly;

import groovy.lang.Closure;

/**
 * 
 * @author shai
 */
public class PostCompileClosure extends Closure<String>{

	private static final long	serialVersionUID	= -467576318569482194L;

	public PostCompileClosure(Object owner) {
		super(owner);
	}
	
	public String call() {
		Task postCompileTask = (Task)getOwner();
		LocationExtension location = (LocationExtension) postCompileTask.getExtensions().getByName("location");
		
		File outputDir = null;
		if (null == location.outputDir) {
			JavaPluginConvention javaConvention = postCompileTask.getProject().getConvention().getPlugin(JavaPluginConvention.class);
			outputDir = javaConvention.getSourceSets().findByName("main").getOutput().getClassesDir();
		}
		else {
			outputDir = new File(location.outputDir);
		}
		if (!outputDir.exists()) {
			System.out.println(String.format("Post Compile Failed - Output dir %s does not exist!!!", outputDir.getAbsolutePath()));
			return "Failed";
		}
		StringBuilder report = new StringBuilder();
		BuildStats stats = CommonAssembly.assenbleFiles(outputDir, report);
		boolean failed = stats.getFailure() > 0;
		if (postCompileTask.getLogger().isEnabled(LogLevel.INFO)) {
			if (failed)
				System.out.println("BUILD FAILED - converted " + stats.getSuccess() + " files, failed to convert " + stats.getFailure() + " files");
			else
				System.out.println("BUILD SUCCESSFUL - converted " + stats.getSuccess() + " files!");
		}
		if (failed) {
			System.out.println("POST COMPILE SUCCESSFUL!!!");
			if (postCompileTask.getLogger().isEnabled(LogLevel.DEBUG)) {
				System.out.println(report.toString());
			}
		}
		return "success";
	}
}