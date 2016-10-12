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
package com.centimia.orm.jaqu.plugin.decorator;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.centimia.orm.jaqu.plugin.Activator;

/**
 * @author shai
 * 
 */
public class ResourceDecorator implements ILightweightLabelDecorator {

	public static final String ID = "com.centimia.orm.jaqu.plugin.ResourceDecorator"; // NON-NLS-$1
	private ImageDescriptor descriptor = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile) {
			String isJaqu = Activator.getDefault().getPreferenceStore().getString(((IFile)element).getProject().getName() + "_" + ((IFile)element).getName());
			if (!"".equals(isJaqu)) {
				((DecorationContext)decoration.getDecorationContext()).putProperty(IDecoration.ENABLE_REPLACE, Boolean.TRUE);
				decoration.addOverlay(getFileImageDescriptor(), IDecoration.TOP_LEFT);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {

	}
	
	/**
	 * Returns the image that is to be rendered in the overlay
	 * @return ImageDescriptor
	 */
	private ImageDescriptor getFileImageDescriptor() {
		if (descriptor == null) {
			URL iconURL = Activator.getDefault().getBundle().getEntry("icons/jaqu-entity3.png");
			if (iconURL != null) {
				descriptor = ImageDescriptor.createFromURL(iconURL);
			}
		}
		return descriptor;
	}
}
