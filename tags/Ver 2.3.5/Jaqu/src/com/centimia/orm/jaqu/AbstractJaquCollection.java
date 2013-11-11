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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.util.Utils;

/**
 * A special collection class used to support merging entities into running Db sessions.
 * @author Shai Bentin
 */
abstract class AbstractJaquCollection<E> implements Collection<E>, Serializable {

	private static final long	serialVersionUID	= 3249922548306321787L;
	
	protected final Collection<E> originalList;
	List<E> internalMapping;
	List<E> internalDeleteMapping;
	
	protected transient WeakReference<Db> db;
	protected transient FieldDefinition definition;
	protected transient Object parentPk;
	
	AbstractJaquCollection(Collection<E> origList, Db db, FieldDefinition definition, Object parentPk) {
		this.originalList = origList;
		this.db = new WeakReference<Db>(db);
		this.definition = definition;
		this.parentPk = parentPk;
	}	
	
	public boolean add(E e) {
		if (!dbClosed()) {			
			db.get().addSession(e); // merge the Object into the DB
			return originalList.add(e);
		}
		else {
			if (internalMapping == null)
				internalMapping = Utils.newArrayList();
			return internalMapping.add(e);
		}
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean result = false;
		for (E e: c) {
			if (this.add(e))
				result = true;
		}
		return result;
	}

	public void clear() {
		if (!dbClosed()) {
			for (E e: originalList) {
				db.get().deleteChildRelation(definition, e, parentPk);
			}
			originalList.clear();
		}
		else {
			if (internalDeleteMapping == null)
				internalDeleteMapping = Utils.newArrayList();
			internalDeleteMapping.addAll(originalList);
			originalList.clear();
			if (internalMapping != null)
				internalMapping.clear();
		}
			
	}

	public boolean contains(Object o) {
		boolean result = originalList.contains(o);
		if (result)
			return result;
		if (internalMapping != null)
			return internalMapping.contains(o);
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		if (!dbClosed())
			return originalList.containsAll(c);
		else {
			// this has to look at the joined list...
			List<E> tmpList = Utils.newArrayList();
			tmpList.addAll(originalList);
			if (internalMapping != null)
				tmpList.addAll(originalList);
			if (internalDeleteMapping != null)
				tmpList.removeAll(internalDeleteMapping);
			return tmpList.containsAll(c);
		}
	}

	public boolean isEmpty() {
		return originalList.isEmpty() && (internalMapping != null ? internalMapping.isEmpty() : true);
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		if (!dbClosed()) {
			db.get().deleteChildRelation(definition, o, parentPk);
			return originalList.remove(o);
		}
		else {
			if (internalMapping != null) {
				boolean result = internalMapping.remove(o);
				if (result)
					return true;
			}
		}
		if (originalList.contains(o)) {
			if (internalDeleteMapping == null)
				internalDeleteMapping = Utils.newArrayList();
			internalDeleteMapping.add((E)o);
			originalList.remove(o);
			return true;
		}
		return false;
	}

	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object e: c) {
			if (this.remove(e))
				result = true;
		}
		return result;
	}

	public boolean retainAll(Collection<?> c) {
		// retaining is tricky because we don't know what to delete...
		if (!dbClosed()) {
			boolean result = false;
			for (E e: originalList) {
				if (!c.contains(e))
					if (remove(e))
						result = true;
			}
			return result;
		}
		throw new JaquError("IllegalState - This list is backed up by the DB. Can't retain objects outside the Db Session");
	}

	public int size() {
		int delete = 0, add = 0;
		if (internalMapping != null)
			add = internalMapping.size();
		return originalList.size() + add - delete;
	}

	/**
	 * The array is not backed up by the list
	 */
	public Object[] toArray() {
		List<E> elements = Utils.newArrayList();
		elements.addAll(originalList);
		if (internalMapping != null)
			elements.addAll(internalMapping);
		return elements.toArray();
	}

	/**
	 * The array is not backed up by the list
	 */
	public <T> T[] toArray(T[] a) {
		List<E> elements = Utils.newArrayList();
		elements.addAll(originalList);
		if (internalMapping != null)
			elements.addAll(internalMapping);
		return elements.toArray(a);
	}

	protected boolean dbClosed() {
		Db internal = db.get();
		if (internal != null)
			return internal.closed();
		return true;
	}
	
	@SuppressWarnings("hiding")
	protected class JaquIterator<E> implements Iterator<E>{

		private final Iterator<E> delagete;
		private final boolean removable;
		private E current;
		
		JaquIterator(Iterator<E> iter) {
			this.delagete = iter;
			removable = true;
		}
		
		public JaquIterator(Iterator<E> iter, boolean b) {
			this.delagete = iter;
			removable = b;
		}

		public boolean hasNext() {
			return delagete.hasNext();
		}

		public E next() {
			this.current = delagete.next();
			return current;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			if (!dbClosed() && removable) {
				delagete.remove();
				db.get().deleteChildRelation(definition, current, parentPk);				
			}
			else {
				AbstractJaquCollection<E> col = ((AbstractJaquCollection<E>)AbstractJaquCollection.this);
				if (col.internalDeleteMapping == null)
					col.internalDeleteMapping = new ArrayList<E>();
				col.internalDeleteMapping.add((E) current);
				delagete.remove();
			}
		}
	}
	
	void setDb(Db db) {
		this.db = new WeakReference<Db>(db);
	}
	
	void setFieldDefinition(FieldDefinition fDef) {
		this.definition = fDef;
	}
	
	void setParentPk(Object parentPk) {
		this.parentPk = parentPk;
	}

	/*
	 * Merge the list to the open db session
	 */
	void merge() {
		if (internalMapping != null)
			originalList.addAll(internalMapping);
		if (internalDeleteMapping != null) {
			for (E child: internalDeleteMapping) {
				db.get().deleteChildRelation(definition, child, parentPk);
			}
			originalList.removeAll(internalDeleteMapping); // make sure we don't have left overs
		}
		
		
		internalDeleteMapping = null;
		internalMapping = null;
		for (E e: originalList) {
			db.get().addSession(e);
		}			
	}
}
