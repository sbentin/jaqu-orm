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
 * 08/06/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.centimia.orm.jaqu.plugin.nature.ClassPathInitializer;
import com.centimia.orm.jaqu.plugin.nature.JaquNature;

/**
 * 
 * @author shai
 */
public class ToggleNatureAction implements IObjectActionDelegate {

	private ISelection selection;
	private static final String CLASSPATH_CONTAINER_PATH = "com.centimia.orm.jaqu.plugin.nature.JAQU"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} 
				else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				if (project != null) {
					toggleNature(project);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void toggleNature(IProject project) {
		try {			
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			boolean hasJavaNature = false;
			for (int i = 0; i < natures.length; ++i) {
				if (JavaCore.NATURE_ID.equals(natures[i])) {
					hasJavaNature = true;
				}
				if (JaquNature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					deconfigureClassPath(project);
					return;
				}
			}

			// Add the nature
			if (hasJavaNature) {
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = JaquNature.NATURE_ID;
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
				
				configureClassPath(project);
			}
			else {
				IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID,Messages.ToggleNatureAction_1);
				ErrorDialog.openError(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.ToggleNatureAction_2, null, status);
			}	
		} 
		catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,  0, Messages.ToggleNatureAction_3, e);
			ErrorDialog.openError(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.ToggleNatureAction_3, null, status);
		}
	}

	private void configureClassPath(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
		for (IClasspathEntry entry: classpathEntries) {
			if (CLASSPATH_CONTAINER_PATH.equals(entry.getPath().toString())) {
				return;
			}
		}
		// Add the Jaqu library.
		ClassPathInitializer classPathInitializer = new ClassPathInitializer();
		classPathInitializer.initialize(new Path(CLASSPATH_CONTAINER_PATH), javaProject);
		//JavaCore.setClasspathContainer(new Path(CLASSPATH_CONTAINER_PATH), new IJavaProject[] { javaProject },  new IClasspathContainer[] { new JaquClasspathContainer(javaProject, new Path(CLASSPATH_CONTAINER_PATH))}, null);
		
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(javaProject.getRawClasspath()));
        list.add(JavaCore.newContainerEntry(new Path(CLASSPATH_CONTAINER_PATH)));
        javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
	}
	
	private void deconfigureClassPath(IProject project) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
		for (IClasspathEntry entry: classpathEntries) {
			if (CLASSPATH_CONTAINER_PATH.equals(entry.getPath().toString())) {
				List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
				list.addAll(Arrays.asList(javaProject.getRawClasspath()));
				list.remove(entry);
				javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
			}
		}
	}
}
