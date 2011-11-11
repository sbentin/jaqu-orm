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
 * 02/02/2010		Shai Bentin			 create
 */
package org.h2.jaqu;

import java.util.Iterator;
import java.util.Set;

import org.h2.jaqu.TableDefinition.FieldDefinition;
import org.h2.jaqu.util.Utils;

/**
 * A set implementation of the Jaqu Collection
 * 
 * @see AbstractJaquCollection
 * @author Shai Bentin
 */
class JaquSet<E> extends AbstractJaquCollection<E> implements Set<E> {

	public JaquSet(Set<E> origSet, Db db, FieldDefinition definition, Object parentPk) {
		super(origSet, db, definition, parentPk);		
	}

	public Iterator<E> iterator() {
		if (!dbClosed()) {
			return new JaquIterator<E>(originalList.iterator());
		}
		else {
			Set<E> tmpSet = Utils.newHashSet();
			tmpSet.addAll(originalList);
			if (internalMapping != null)
				tmpSet.addAll(internalMapping);
			if (internalDeleteMapping != null)
				tmpSet.removeAll(internalDeleteMapping);
			return new JaquIterator<E>(tmpSet.iterator(), false);
		}
	}

}