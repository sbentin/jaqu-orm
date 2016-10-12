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
 * 08/06/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu.plugin.builder;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;

import com.centimia.orm.jaqu.ext.common.CommonAssembly;
import com.centimia.orm.jaqu.plugin.Activator;
import com.centimia.orm.jaqu.plugin.decorator.ResourceDecorator;

/**
 * 
 * @author shai
 */
public class JaquBuilder extends IncrementalProjectBuilder {

	class JaquDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				assembleResource(resource);
				refreshLocal(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				assembleResource(resource);
				refreshLocal(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}

	}

	class JaquResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) throws CoreException {
			assembleResource(resource);
			refreshLocal(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "com.centimia.orm.jaqu.plugin.builder.jaquBuilder"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} 
		else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} 
			else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	synchronized void assembleResource(IResource resource) {
		if (resource instanceof IFile) {
			IFile fileResource = (IFile)resource;
			if (resource.getName().endsWith(".class")) { //$NON-NLS-1$
				try {
					File classFile = fileResource.getLocation().toFile();
					boolean isJaquAnnotated = CommonAssembly.assembleFile(classFile);
					
					ResourceDecorator decorator = (ResourceDecorator) PlatformUI.getWorkbench().getDecoratorManager().getBaseLabelProvider(ResourceDecorator.ID);
					if (decorator != null) {
						String resourceName = resource.getName().substring(0, resource.getName().lastIndexOf('.')) + ".java";
						String isJaqu = Activator.getDefault().getPreferenceStore().getString(resource.getProject().getName() + "_" + resourceName);
						if (isJaquAnnotated) {							
							if (isJaqu == null || "".equals(isJaqu)) {
								Activator.getDefault().getPreferenceStore().putValue(resource.getProject().getName() + "_" + resourceName, resourceName);
							}
						}
						else {
							if (isJaqu != null && !"".equals(isJaqu)) {
								Activator.getDefault().getPreferenceStore().putValue(resource.getProject().getName() + "_" + resourceName, "");
							}
						}						
					}				
				}
				catch (Exception e) {
					// unexpected error. Write to the eclipse error log???
					IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.JaquBuilder_2 + fileResource.getName(), e);
					Activator.getDefault().getLog().log(status);
				}
			}
		}
	}
	
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new JaquResourceVisitor());
		} 
		catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.JaquBuilder_4, e);
			Activator.getDefault().getLog().log(status);
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new JaquDeltaVisitor());
	}
	
	/**
	 * Refresh the current project to sync it with the change on disk.
	 * @param resource
	 * @throws CoreException 
	 */
	protected void refreshLocal(IResource resource) throws CoreException {
		resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
}
