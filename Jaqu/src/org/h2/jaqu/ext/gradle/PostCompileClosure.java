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

import groovy.lang.Closure;

import java.io.File;

import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginConvention;
import org.h2.jaqu.ext.common.BuildStats;
import org.h2.jaqu.ext.common.CommonAssembly;

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
			postCompileTask.getLogger().error(String.format("Post Compile Failed - Output dir %s does not exist!!!", outputDir.getAbsolutePath()));
			return "Failed";
		}
		StringBuilder report = new StringBuilder();
		BuildStats stats = CommonAssembly.assembleFiles(outputDir, report);
		boolean failed = stats.getFailure() > 0;
		if (failed) {
			postCompileTask.getLogger().lifecycle("POST COMPILE FAILED - converted " + stats.getSuccess() + " files, , ignored " + stats.getIgnored() + "files, failed to convert " + stats.getFailure() + " files");
		}
		else {
			postCompileTask.getLogger().lifecycle("POST COMPILE SUCCESSFUL - converted " + stats.getSuccess() + " files, ignored " + stats.getIgnored());
		}
		if (postCompileTask.getLogger().isEnabled(LogLevel.DEBUG)) {
			postCompileTask.getLogger().debug(report.toString());
		}
		return "success";
	}
}