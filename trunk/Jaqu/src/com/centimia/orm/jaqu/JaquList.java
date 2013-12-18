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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.util.Utils;

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
			((List<E>)originalList).add(index, element);
			db.get().addSession(element);
		}
		else {
			throw new JaquError("IllegalState - This list is backed up by the DB. Can't insert into a specific list position outside the Db Session");
		}		
	}

	/**
	 * Add a collection of elements in a specific position.
	 * 
	 * @see {@link JaquList#add(int, Object)}
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		if (!dbClosed()) {
			for (E e: c) {
				db.get().addSession(e);
			}
			return ((List<E>)originalList).addAll(index, c);
		}
		else
			throw new JaquError("IllegalState - This list is backed up by the DB. Can't insert into a specific list position outside the Db Session");
	}

	/**
	 * Getting an object from the list from a specific position may produce an incorrect result since when adding elements to the list outside the jaqu
	 * session will cause inconsistency in the order of elements in the list until it is attached to the session and saved. Therefore, performing get(index) on the list
	 * outside the Jaqu Session is not permitted. To go over the list of Items use the Iterator instead. 
	 */
	public E get(int index) {
		if (!dbClosed())
			return ((List<E>)originalList).get(index);
		else
			throw new JaquError("IllegalState - This list is backed up by the DB. Can't do get by position outside the Db Session");
	}

	/**
	 * Check the index of an item in a the list may produce an incorrect result since when adding elements to the list outside the jaqu
	 * session will cause inconsistency in the order of elements in the list until it is attached to the session and saved. Therefore, performing indexOf(Object) on the list
	 * outside the Jaqu Session is not permitted.
	 */
	public int indexOf(Object o) {
		if (!dbClosed())
			return ((List<E>)originalList).indexOf(o);
		else
			throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session");
	}

	/**
	 * Check the index of an item in a the list may produce an incorrect result since when adding elements to the list outside the jaqu
	 * session will cause inconsistency in the order of elements in the list until it is attached to the session and saved. Therefore, performing lastIndexOf(Object) on the list
	 * outside the Jaqu Session is not permitted.
	 */
	public int lastIndexOf(Object o) {
		if (!dbClosed())
			return ((List<E>)originalList).lastIndexOf(o);
		else
			throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session");
	}

	public ListIterator<E> listIterator() {
		throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session");
	}

	public ListIterator<E> listIterator(int index) {
		throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session");
	}

	/**
	 * Add an element to the list in a specific position. Since this list is backed up by the DB. Inserting in a specific position
	 * (although I don't see it's practicality) is only possible when working inside a Jaqu Session.
	 */
	public E remove(int index) {
		if (!dbClosed()) {
			E element = ((List<E>)originalList).remove(index);
			db.get().deleteChildRelation(definition, element, parentPk);
			return element;
		}
		else
			throw new JaquError("IllegalState - This list is backed up by the DB. Can't remove by position of object outside the Db Session");
	}

	/**
	 * Add an element to the list in a specific position. Since this list is backed up by the DB. Inserting in a specific position
	 * (although I don't see it's practicality) is only possible when working inside a Jaqu Session.
	 */
	public E set(int index, E element) {
		if (!dbClosed()) {
			db.get().addSession(element);
			((List<E>)originalList).set(index, element);
		}
		throw new JaquError("IllegalState - This list is backed up by the DB. Can't replace by position of object outside the Db Session");
	}

	public List<E> subList(int fromIndex, int toIndex) {
		throw new JaquError("IllegalState - This list is backed up by the DB. This operation is not supported at this time");
	}
	
	public Iterator<E> iterator() {
		if (!dbClosed()) {
			return new JaquIterator<E>(originalList.iterator());
		}
		else {
			List<E> tmpList = Utils.newArrayList();
			tmpList.addAll(originalList);
			if (internalMapping != null)
				tmpList.addAll(internalMapping);
			if (internalDeleteMapping != null)
				tmpList.removeAll(internalDeleteMapping);
			return new JaquIterator<E>(tmpList.iterator(), false);
		}
	}
}
