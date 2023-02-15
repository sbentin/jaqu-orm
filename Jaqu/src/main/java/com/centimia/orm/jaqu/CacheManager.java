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
package com.centimia.orm.jaqu;

import java.util.Map;

import com.centimia.orm.jaqu.annotation.Entity;
import com.centimia.orm.jaqu.annotation.MappedSuperclass;
import com.centimia.orm.jaqu.util.Utils;

/**
 * Manages the cache for objects already visited by the connection.
 * @author shai
 */
final class CacheManager {

	private final Map<Class<?>, Map<String, Object>> cache = Utils.newHashMap();
	private final JaquSessionFactory factory;

	public CacheManager(JaquSessionFactory factory) {
		this.factory = factory;
	}

	/**
     * Prepares the reentrant list with the object that should not be reentered into.
     *
     * @param obj
     */
    void prepareReEntrent(Object obj) {
		Map<String, Object> innerMap = cache.get(obj.getClass());
		if (null == innerMap) {
			innerMap = Utils.newHashMap();
			cache.put(obj.getClass(), innerMap);
		}
		String primaryKey = factory.getPrimaryKey(obj).toString();
		if (null == innerMap.get(primaryKey))
			innerMap.put(primaryKey, obj);
	}

    /**
     * remove the object from the cache if the object exists in cache.
     * No need for the object to be returned as the user already holds the instance.
     *
     * @param obj
     */
    void removeReEntrent(Object obj) {
    	Map<String, Object> innerMap = cache.get(obj.getClass());
    	if (null == innerMap)
    		// all is removed
    		return;
    	innerMap.remove(factory.getPrimaryKey(obj).toString());
    	if (innerMap.isEmpty()) {
    		cache.remove(obj.getClass());
    	}
    }

    /**
     * Reports whether the object actually exists in cache.
     * @param obj
     * @return boolean
     */
    boolean checkReEntrent(Object obj) {
		if (null != obj && (null != obj.getClass().getAnnotation(Entity.class) || null != obj.getClass().getAnnotation(MappedSuperclass.class)))
			return checkReEntrent(obj.getClass(), factory.getPrimaryKey(obj)) != null;
		return false;
	}

    /**
     * Tries to match the key with an object in cache. If it exists the object is returned.
     *
     * @param clazz
     * @param key
     * @return Object
     */
	Object checkReEntrent(Class<?> clazz, Object key) {
		if (null != key){
			key = key.toString();
			Map<String, ?> innerMap = cache.get(clazz);
			if (null != innerMap) {
				return innerMap.get(key);
			}
		}
		return null;
	}

	/**
	 * Clears the reEntrent cache
	 */
	void clearReEntrent() {
		cache.clear();
	}
}
