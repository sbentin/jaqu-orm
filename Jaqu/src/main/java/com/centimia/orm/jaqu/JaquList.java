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

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
	protected transient int modCount = 0;
	
	public JaquList(List<E> origList, Db db, FieldDefinition definition, Object parentPk) {
		super(origList, db, definition, parentPk);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.AbstractJaquCollection#add(java.lang.Object)
	 */
	public boolean add(E e) {
		boolean result = super.add(e);
		if (result)
			modCount++;
		return result;
	}
	
	/**
	 * Add an element to the list in a specific position. Since this list is backed up by the DB. Inserting in a specific position
	 * (although I don't see it's practicality) is only possible when working inside a Jaqu Session.
	 */
	public void add(int index, E element) {
		if (!dbClosed()) {
			element = db.get().checkSession(element);
		}
		((List<E>)originalList).add(index, element);
		modCount++;
	}

	/**
	 * Add a collection of elements in a specific position.
	 * 
	 * @see {@link JaquList#add(int, Object)}
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result;
		if (!dbClosed()) {
			ArrayList<E> attachedList = new ArrayList<>();
			for (E e: c) {
				attachedList.add(db.get().checkSession(e));
			}
			result = ((List<E>)originalList).addAll(index, c);
		}
		else
			result = ((List<E>)originalList).addAll(index, c);
		if (result)
			modCount ++;
		return result;
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
		return new JaquListIterator(((List<E>)originalList).listIterator());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<E> listIterator(int index) {
		return new JaquListIterator(((List<E>)originalList).listIterator(index));
	}

	public boolean remove(Object o) {
		boolean result = super.remove(o);
		if (result)
			modCount++;
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	@SuppressWarnings("resource")
	public E remove(int index) {
		E element = ((List<E>)originalList).remove(index);
		modCount++;
		if (!dbClosed()) {
			if (null != db.get().factory.getPrimaryKey(element))
				db.get().deleteChildRelation(definition, element, parentPk);
		}
		else {
			if (null == internalDeleteMapping)
				internalDeleteMapping = Utils.newHashMap();
			internalDeleteMapping.put(element, null);
		}
		return element;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@SuppressWarnings("resource")
	public E set(int index, E element) {
		// get the current element
		E e = ((List<E>)originalList).set(index, element);
		if (!dbClosed()) {
			// only when the instances are not of the same database row we need to actually delete an instance from the db.
			Object oldPk = db.get().factory.getPrimaryKey(e);
			Object newPk = db.get().factory.getPrimaryKey(element);
			if (null != oldPk && !oldPk.equals(newPk)) {
				e = db.get().checkSession(element);				
				db.get().deleteChildRelation(definition, e, parentPk);
			}
		}
		else {
			// we're not in session we need to take care of this replaced element when entering a session
			if (null == internalDeleteMapping)
				internalDeleteMapping = Utils.newHashMap();
			internalDeleteMapping.put(e, element);
		}
		return e;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.AbstractJaquCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		boolean result = super.retainAll(c);
		if (result)
			modCount++;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.centimia.orm.jaqu.AbstractJaquCollection#clear()
	 */
	public void clear() {
		super.clear();
		modCount++;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public List<E> subList(int fromIndex, int toIndex) {
		return new SubList(this, fromIndex, toIndex);
	}
	
	class JaquListIterator extends JaquIterator<E> implements ListIterator<E> {
		// first index is 0
		int lastIdx = -1;
		
		JaquListIterator(ListIterator<E> iter) {
			super(iter);
		}

		public E next() {
			super.next();
			lastIdx++;
			return current;
		}
		
		public boolean hasPrevious() {
			return ((ListIterator<E>)this.delagete).hasPrevious();
		}

		public E previous() {
			this.current = ((ListIterator<E>)this.delagete).previous();
			lastIdx--;
			return current;
		}

		public int nextIndex() {
			return lastIdx + 1;
		}

		public int previousIndex() {
			return lastIdx - 1;
		}

		public void set(E e) {
			if (null != current) {	
				JaquList.this.set(lastIdx, e);
				current = e;
			}			
		}

		public void add(E e) {
			((ListIterator<E>)this.delagete).add(e);		
		}		
	}
	
	@SuppressWarnings("resource")
	void merge() {
		super.merge();
		originalList = originalList.stream().map(e -> db.get().checkSession(e)).collect(Collectors.toList());
	}
	
	class SubList implements List<E> {
	    private JaquList<E> l;
	    private int offset;
	    private int size;
	    private int expectedModCount;

	    SubList(JaquList<E> list, int fromIndex, int toIndex) {
	        if (fromIndex < 0)
	            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
	        if (toIndex > list.size())
	            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
	        if (fromIndex > toIndex)
	            throw new IllegalArgumentException("fromIndex[" + fromIndex + "] > toIndex[" + toIndex + "]");
	        
	        l = list;
	        offset = fromIndex;
	        size = toIndex - fromIndex;
	        expectedModCount = l.modCount;
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#set(int, java.lang.Object)
	     */
	    public E set(int index, E element) {
	        rangeCheck(index);
	        checkForComodification();
	        return l.set(index + offset, element);
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#get(int)
	     */
	    public E get(int index) {
	        rangeCheck(index);
	        checkForComodification();
	        return l.get(index + offset);
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#size()
	     */
	    public int size() {
	        checkForComodification();
	        return size;
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#add(java.lang.Object)
	     */
	    public boolean add(E element) {
	    	add(size, element);
	    	return true;
	    }
	    
	    /*
	     * (non-Javadoc)
	     * @see java.util.List#add(int, java.lang.Object)
	     */
	    public void add(int index, E element) {
	        if (index < 0 || index > size)
	            throw new IndexOutOfBoundsException("My size is: " + size + " your index: " + index);
	        checkForComodification();
	        l.add(index + offset, element);
	        expectedModCount = l.modCount;
	        size++;
	        // adjust this subList's modCount
	        modCount++;
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#remove(java.lang.Object)
	     */
	    public boolean remove(Object o) {
	    	int index = indexOf(o); // o may be in the parent list but not in the sublist
	    	if (-1 != index) {
	    		remove(index);
	    		return true;
	    	}
	    	return false;
	    }
	    
	    /*
	     * (non-Javadoc)
	     * @see java.util.List#remove(int)
	     */
	    public E remove(int index) {
	        rangeCheck(index);
	        checkForComodification();
	        E result = l.remove(index + offset);
	        expectedModCount = l.modCount;
        	size--;
       
	        // adjust this subList's modCount
	        modCount++;
	        return result;
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#removeAll(java.util.Collection)
	     */
	    public boolean removeAll(Collection<?> c) {
	    	boolean result = false;
			for (Object e: c) {
				try {
					rangeCheck(l.indexOf(e));
				}
				catch (IndexOutOfBoundsException iob) {
					// this element is not in this sublist
					continue;
				}
				if (l.remove(e)) {
					result = true;
					expectedModCount = l.modCount;					
					size--;
					
					// adjust this subList's modCount
					modCount++;
				}
			}
			
			return result;
		}
	    
	    /*
	     * (non-Javadoc)
	     * @see java.util.List#addAll(java.util.Collection)
	     */
	    public boolean addAll(Collection<? extends E> c) {
	        return addAll(size, c);
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#addAll(int, java.util.Collection)
	     */
	    public boolean addAll(int index, Collection<? extends E> c) {
	        if (index<0 || index>size)
	            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
	        int cSize = c.size();
	        if (cSize == 0)
	            return false;

	        checkForComodification();
	        boolean result = l.addAll(offset + index, c);
	        expectedModCount = l.modCount;
	        if (result) {
	        	size += cSize;
	        	modCount++;
	        }	        
	        return result;
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#isEmpty()
	     */
		public boolean isEmpty() {
			if (size == 0 || l.isEmpty())
				return true;
			return false;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#contains(java.lang.Object)
		 */
		public boolean contains(Object o) {
			int index = l.indexOf(o);
			try {
				rangeCheck(index);
				return true;
			}
			catch (IndexOutOfBoundsException iob) {
				return false;
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#toArray()
		 */
		public Object[] toArray() {
			Object[] array = new Object[size];
			for (int i = 0; i < size; i++) {
				array[i] = l.get(offset + i);
			}
			return array;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#toArray(T[])
		 */
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			T[] array = a.length >= size ? a : (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
			for (int i = 0; i < array.length; i++) {
				if (i >= size)
					array[i] = null;
				else
					array[i] = (T) l.get(offset + i);
			}
			return array;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#containsAll(java.util.Collection)
		 */
		public boolean containsAll(Collection<?> c) {
			Iterator<?> e = c.iterator();
			while (e.hasNext())
				if (!contains(e.next()))
					return false;
			return true;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#indexOf(java.lang.Object)
		 */
		public int indexOf(Object o) {
			int result = l.indexOf(o) - offset;
			try {
				rangeCheck(result);
				return result;
			}
			catch (IndexOutOfBoundsException iob) {
				return -1;
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#lastIndexOf(java.lang.Object)
		 */
		public int lastIndexOf(Object o) {
			if (null == o)
				return -1;
			for (int i = (size - 1); i <= 0; i--) {
				if (o.equals(l.get(offset + i)))
					return i;
			}
			return -1;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.List#clear()
		 */
		public void clear() {
			for (int i = 0; i < size; i++) {
				l.remove(offset + i);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#retainAll(java.util.Collection)
		 */
		public boolean retainAll(Collection<?> c) {
			if (null == c) {
				return false;
			}
			
			boolean changed = false;
			for (int i = 0; i < size; i++) {
				E element = l.get(offset + i);
				if (!c.contains(element)) {
					remove(element);
					changed = true;
				}
			}
			return changed;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#listIterator()
		 */
		public ListIterator<E> listIterator() {
			return listIterator(0);
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.List#iterator()
		 */
	    public Iterator<E> iterator() {
	        return listIterator();
	    }

	    /*
	     * (non-Javadoc)
	     * @see java.util.List#listIterator(int)
	     */
	    public ListIterator<E> listIterator(final int index) {
	        checkForComodification();
	        if (index<0 || index>size)
	            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

	        return new ListIterator<E>() {
	            private ListIterator<E> i = l.listIterator(offset + index);

	            public boolean hasNext() {
	                return nextIndex() < size;
	            }

	            public E next() {
	                if (hasNext())
	                    return i.next();
	                else
	                    throw new NoSuchElementException();
	            }

	            public boolean hasPrevious() {
	                return previousIndex() >= 0;
	            }

	            public E previous() {
	                if (hasPrevious())
	                    return i.previous();
	                else
	                    throw new NoSuchElementException();
	            }

	            public int nextIndex() {
	                return i.nextIndex() - offset;
	            }

	            public int previousIndex() {
	                return i.previousIndex() - offset;
	            }

	            public void remove() {
	                i.remove();
	                expectedModCount = l.modCount;
	                size--;
	                modCount++;
	            }

	            public void set(E e) {
	                i.set(e);
	            }

	            public void add(E e) {
	                i.add(e);
	                expectedModCount = l.modCount;
	                size++;
	                modCount++;
	            }
	        };
	    }

	    /**
	     * Unsupported in JaquList.subList because jaquList is actually a special wrapper
	     * @throws JaquError
	     */
	    public List<E> subList(int fromIndex, int toIndex) {
	    	throw new JaquError("IllegalState - This list is backed up by the DB. Can't get position of object outside the Db Session"); 
	    }

	    /**
	     * checks whether the given index is within the sublist's range
	     * @param index
	     */
	    private void rangeCheck(int index) {
	        if (index < 0 || index >= size)
	        	throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
	    }

	    /**
	     * check that the 'real' list was not modified outside the sublist process by a different thread.
	     */
	    private void checkForComodification() {
	        if (l.modCount != expectedModCount)
	            throw new ConcurrentModificationException();
	    }
	}
}