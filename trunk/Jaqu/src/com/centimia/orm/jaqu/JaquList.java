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
 * 02/02/2010		Shai Bentin			 create
 */
package com.centimia.orm.jaqu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;

/**
 * A List implementation of the Jaqu Collection. This list should not be invoked by a User it is invoked by the framework only
 * 
 * @see AbstractJaquCollection
 * @author Shai Bentin
 */
class JaquList<E> extends AbstractJaquCollection<E> implements List<E> {
	
	private static final long	serialVersionUID	= -4977190877509050721L;

	public JaquList(List<E> origList, Db db, FieldDefinition definition, Object parentPk) {
		super(origList, db, definition, parentPk);
	}
	
	/**
	 * Add an element to the list in a specific position. Since this list is backed up by the DB. Inserting in a specific position
	 * (although I don't see it's practicality) is only possible when working inside a Jaqu Session.
	 */
	public void add(int index, E element) {
		if (!dbClosed()) {
			db.get().checkSession(element);
		}
		((List<E>)originalList).add(index, element);		
	}

	/**
	 * Add a collection of elements in a specific position.
	 * 
	 * @see {@link JaquList#add(int, Object)}
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		if (!dbClosed()) {
			for (E e: c) {
				db.get().checkSession(e);
			}
		}
		return ((List<E>)originalList).addAll(index, c);
	}

	/**
	 * Getting an object from the list from a specific position may produce an incorrect result since when adding elements to the list outside the jaqu
	 * session will cause inconsistency in the order of elements in the list until it is attached to the session and saved. Therefore, performing get(index) on the list
	 * outside the Jaqu Session is not permitted. To go over the list of Items use the Iterator instead. 
	 */
	public E get(int index) {
		return ((List<E>)originalList).get(index);
	}

	/**
	 * Check the index of an item in a the list may produce an incorrect result since when adding elements to the list outside the jaqu
	 * session will cause inconsistency in the order of elements in the list until it is attached to the session and saved. Therefore, performing indexOf(Object) on the list
	 * outside the Jaqu Session is not permitted.
	 */
	public int indexOf(Object o) {
		return ((List<E>)originalList).indexOf(o);
	}

	/**
	 * Check the index of an item in a the list may produce an incorrect result since when adding elements to the list outside the jaqu
	 * session will cause inconsistency in the order of elements in the list until it is attached to the session and saved. Therefore, performing lastIndexOf(Object) on the list
	 * outside the Jaqu Session is not permitted.
	 */
	public int lastIndexOf(Object o) {
		return ((List<E>)originalList).lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<E> listIterator() {
		throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session");
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<E> listIterator(int index) {
		throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session");
	}

	/**
	 * Add an element to the list in a specific position. Since this list is backed up by the DB. Inserting in a specific position
	 * (although I don't see it's practicality) is only possible when working inside a Jaqu Session.
	 */
	public E remove(int index) {
		E element = ((List<E>)originalList).remove(index);
		if (!dbClosed()) {
			if (null != db.get().factory.getPrimaryKey(element))
				db.get().deleteChildRelation(definition, element, parentPk);
		}
		else {
			if (null == internalDeleteMapping)
				internalDeleteMapping = new ArrayList<E>();
			internalDeleteMapping.add(element);
		}
		return element;
	}

	/**
	 * Add an element to the list in a specific position. Since this list is backed up by the DB. Inserting in a specific position
	 * (although I don't see it's practicality) is only possible when working inside a Jaqu Session.
	 */
	public E set(int index, E element) {
		if (!dbClosed()) {
			db.get().checkSession(element);	
		}
		return ((List<E>)originalList).set(index, element);
	}

	/**
	 * Not Supported at this time
	 * @throws JaquError
	 */
	public List<E> subList(int fromIndex, int toIndex) {
		throw new JaquError("IllegalState - This list is backed up by the DB. This operation is not supported at this time");
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<E> iterator() {
		return new JaquIterator<E>(originalList.iterator());
	}
}
