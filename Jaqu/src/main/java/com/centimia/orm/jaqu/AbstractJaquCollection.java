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
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(E e) {
		if (!dbClosed()) {			
			db.get().checkSession(e); // merge the Object into the DB
		}
		return originalList.add(e);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends E> c) {
		boolean result = false;
		for (E e: c) {
			if (this.add(e))
				result = true;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
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
		}
			
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return originalList.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return originalList.containsAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return originalList.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		boolean removed = originalList.remove(o);
		if (removed) {
			if (!dbClosed()) {
				if (null != db.get().factory.getPrimaryKey(o))
					db.get().deleteChildRelation(definition, o, parentPk);
			}
			else {
				if (null == internalDeleteMapping)
					internalDeleteMapping = Utils.newArrayList();
				internalDeleteMapping.add((E)o);
			}
		}
		return removed;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object e: c) {
			if (this.remove(e))
				result = true;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		boolean result = false;
		for (E e: originalList) {
			if (!c.contains(e))
				if (remove(e))
					result = true;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return originalList.size();
	}

	/**
	 * The array is not backed up by the list
	 */
	public Object[] toArray() {
		return originalList.toArray();
	}

	/**
	 * The array is not backed up by the list
	 */
	public <T> T[] toArray(T[] a) {
		return originalList.toArray(a);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<E> iterator() {
		return new JaquIterator<E>(originalList.iterator());
	}
	
	protected boolean dbClosed() {
		if (null == db)
			// because db is transient, on the client side it may be null.
			return true;
		Db internal = db.get();
		if (null != internal)
			return internal.closed();
		return true;
	}
	
	@SuppressWarnings("hiding")
	protected class JaquIterator<E> implements Iterator<E>{

		protected final Iterator<E> delagete;
		protected E current;
		
		JaquIterator(Iterator<E> iter) {
			this.delagete = iter;
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
			delagete.remove();
			if (!dbClosed()) {
				if (null != db.get().factory.getPrimaryKey(current))
					db.get().deleteChildRelation(definition, current, parentPk);				
			}
			else {
				AbstractJaquCollection<E> col = ((AbstractJaquCollection<E>)AbstractJaquCollection.this);
				if (col.internalDeleteMapping == null)
					col.internalDeleteMapping = new ArrayList<E>();
				col.internalDeleteMapping.add((E) current);
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
		if (internalDeleteMapping != null) {
			for (E child: internalDeleteMapping) {
				if (null != db.get().factory.getPrimaryKey(child))
					db.get().deleteChildRelation(definition, child, parentPk);
			}
		}		
		
		internalDeleteMapping = null;
		for (E e: originalList) {
			db.get().checkSession(e);
		}			
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
        Iterator<E> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }
}
