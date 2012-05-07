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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

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
		postCompile.getExtensions().create("location", LocationExtension.class);
		postCompile.doFirst(new PostCompileClosure(postCompile));
	}
}
