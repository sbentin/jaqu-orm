/*
 * Copyright (c) 2007-2010 Centimia Ltd.
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
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 02/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import java.util.Iterator;
import java.util.Set;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;

/**
 * A set implementation of the Jaqu Collection
 * 
 * @see AbstractJaquCollection
 * @author Shai Bentin
 */
class JaquSet<E> extends AbstractJaquCollection<E> implements Set<E> {

	private static final long	serialVersionUID	= -1456310904754891892L;

	public JaquSet(Set<E> origSet, Db db, FieldDefinition definition, Object parentPk) {
		super(origSet, db, definition, parentPk);		
	}

	public Iterator<E> iterator() {
		return new JaquIterator<E>(originalList.iterator());
	}
}
