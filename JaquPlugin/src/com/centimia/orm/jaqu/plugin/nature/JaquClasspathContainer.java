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
package com.centimia.orm.jaqu.plugin.nature;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;

import com.centimia.orm.jaqu.plugin.Activator;

/**
 * @author shai
 * 
 */
public class JaquClasspathContainer implements IClasspathContainer {

	private IPath path;
	private IJavaProject javaProject;
	private IClasspathEntry[] jaquLibraryEntries;

	public JaquClasspathContainer(IJavaProject project, IPath path) {
		this.javaProject = project;
		this.path = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		if (jaquLibraryEntries == null)
			jaquLibraryEntries = createJaquLibraryEntries(javaProject);
		return jaquLibraryEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		return "Jaqu Library";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return path;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isValid() {
		return true;
	}
	
	private IClasspathEntry[] createJaquLibraryEntries(IJavaProject project) {
    	// only one jar is included here
		URL pathToJaquJar = Activator.getDefault().getBundle().getEntry("/lib/jaqu.jar"); // $NON-NLS-1$
		URL jarURL = null;
		try {
			jarURL = FileLocator.resolve(pathToJaquJar);		
			Path jarPath = new Path(jarURL.getPath());
			URL javaDocPath = FileLocator.resolve(Activator.getDefault().getBundle().getEntry("/doc")); // $NON_NLS-2$
			IClasspathAttribute javaDocAttrib = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javaDocPath.toExternalForm());			
			return new IClasspathEntry[] {JavaCore.newLibraryEntry(jarPath, null, null, null, new IClasspathAttribute[] {javaDocAttrib}, false)};
		}
		catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,  0, Messages.JaquClasspathContainer_2, e);
			ErrorDialog.openError(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.JaquClasspathContainer_3, null, status);
		}
		return null;
    }
}
