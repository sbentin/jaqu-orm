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
Created		   Jan 16, 2013			shai

*/
package com.centimia.orm.jaqu;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

/**
 * Used for serialization and deSerialization of JaquList
 * 
 * @author shai *
 */
public class SerializedList implements Serializable {
	private static final long	serialVersionUID	= -8264999540781055201L;
	@SuppressWarnings("rawtypes")
	private final List	originalList;
	@SuppressWarnings("rawtypes")
	private final List	internalMapping;
	@SuppressWarnings("rawtypes")
	private final List	internalDeleteMapping;
	
	@SuppressWarnings("rawtypes")
	public SerializedList(List originalList, List internalMapping, List internalDeleteMapping) {
		this.originalList = originalList;
		this.internalMapping = internalMapping;
		this.internalDeleteMapping = internalDeleteMapping;		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Object readResolve() throws ObjectStreamException {
		JaquList jl = new JaquList(originalList, null, null, null);
		jl.setDb(null);
		jl.internalMapping = this.internalMapping;
		jl.internalDeleteMapping = this.internalDeleteMapping;
		return jl;
	}
}
