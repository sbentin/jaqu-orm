/*
 * Copyright (c) 2007-2016 Centimia Ltd.
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
Created		   May 6, 2012			shai

*/
package com.centimia.orm.jaqu.ext.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;

/**
 * @author shai
 *
 */
public class JaquPlugin implements Plugin<Project> {

	public void apply(Project prj) {
		try {
			prj.getPlugins().getPlugin("java");
		}
		catch (Exception e) {
			System.out.println("Adding dependency java plugin....");
			prj.getPlugins().apply("java");
		}

		Task postCompile = prj.task("jaquPostCompile");
		
		postCompile.dependsOn(JavaPlugin.CLASSES_TASK_NAME);
		postCompile.getExtensions().create("location", LocationExtension.class);
		postCompile.doFirst(new PostCompileClosure(postCompile));
	}
}
