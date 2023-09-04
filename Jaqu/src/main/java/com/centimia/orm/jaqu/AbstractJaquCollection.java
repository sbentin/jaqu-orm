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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.centimia.orm.jaqu.TableDefinition.FieldDefinition;
import com.centimia.orm.jaqu.util.Utils;

/**
 * A special collection class used to support merging entities into running Db sessions.
 * @author Shai Bentin
 */
abstract class AbstractJaquCollection<E> implements Serializable, IJaquCollection<E> {

	private static final long	serialVersionUID	= 3249922548306321787L;

	protected Collection<E> originalList;
	Map<E, E> internalDeleteMapping;

	protected transient WeakReference<Db> db;
	protected transient FieldDefinition definition;
	protected transient Object parentPk;

	AbstractJaquCollection(Collection<E> origList, Db db, FieldDefinition definition, Object parentPk) {
		this.originalList = origList;
		this.db = new WeakReference<>(db);
		this.definition = definition;
		this.parentPk = parentPk;
	}

	@Override
	public boolean add(E e) {
		if (!dbClosed()) {
			e = db.get().checkSession(e); // merge the Object into the DB
		}
		return originalList.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = false;
		for (E e: c) {
			if (this.add(e))
				result = true;
		}
		return result;
	}

	@Override
	public void clear() {
		if (!dbClosed()) {
			for (E e: originalList) {
				db.get().deleteChildRelation(definition, e, parentPk);
			}
			originalList.clear();
		}
		else {
			if (null == internalDeleteMapping)
				internalDeleteMapping = Utils.newHashMap();
			originalList.forEach(item -> internalDeleteMapping.put(item, null));
			originalList.clear();
		}

	}

	@Override
	public boolean contains(Object o) {
		return originalList.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return originalList.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return originalList.isEmpty();
	}

	@Override
	@SuppressWarnings({ "unchecked", "resource" })
	public boolean remove(Object o) {
		boolean removed = originalList.remove(o);
		if (removed) {
			if (!dbClosed()) {
				if (null != db.get().factory.getPrimaryKey(o))
					db.get().deleteChildRelation(definition, o, parentPk);
			}
			else {
				if (null == internalDeleteMapping)
					internalDeleteMapping = Utils.newHashMap();
				internalDeleteMapping.put((E)o, null);
			}
		}
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object e: c) {
			if (this.remove(e))
				result = true;
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = false;
		for (Iterator<E> iteratorThis = this.iterator(); iteratorThis.hasNext();) {
			E e = iteratorThis.next();
			if (!c.contains(e))
				iteratorThis.remove();
					result = true;
		}
		return result;
	}

	@Override
	public int size() {
		return originalList.size();
	}

	/**
	 * The array is not backed up by the list
	 */
	@Override
	public Object[] toArray() {
		return originalList.toArray();
	}

	/**
	 * The array is not backed up by the list
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return originalList.toArray(a);
	}

	@Override
	public Iterator<E> iterator() {
		return new JaquIterator<>(originalList.iterator());
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

		@Override
		public boolean hasNext() {
			return delagete.hasNext();
		}

		@Override
		public E next() {
			this.current = delagete.next();
			return current;
		}

		@Override
		@SuppressWarnings({ "unchecked", "resource" })
		public void remove() {
			delagete.remove();
			if (!dbClosed()) {
				if (null != db.get().factory.getPrimaryKey(current))
					db.get().deleteChildRelation(definition, current, parentPk);
			}
			else {
				AbstractJaquCollection<E> col = ((AbstractJaquCollection<E>)AbstractJaquCollection.this);
				if (col.internalDeleteMapping == null)
					col.internalDeleteMapping = Utils.newHashMap();
				col.internalDeleteMapping.put(current, null);
			}
		}
	}

	void setDb(Db db) {
		this.db = new WeakReference<>(db);
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
	@SuppressWarnings("resource")
	void merge() {
		if (internalDeleteMapping != null) {
			for (Map.Entry<E, E> entry: internalDeleteMapping.entrySet()) {
				E newValue = entry.getValue();
				E oldValue = entry.getKey();
				if (null == newValue) {
					if (null != db.get().factory.getPrimaryKey(oldValue))
						db.get().deleteChildRelation(definition, oldValue, parentPk);
				}
				else {
					Object oldPk = db.get().factory.getPrimaryKey(oldValue);
					Object newPk = db.get().factory.getPrimaryKey(newValue);
					if (null != oldPk && !oldPk.equals(newPk))
						db.get().deleteChildRelation(definition, oldValue, parentPk);
				}
			}
		}
		internalDeleteMapping = null;
	}

	@Override
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
