/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu.util;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.h2.jaqu.annotation.Entity;
import org.h2.jaqu.constant.ErrorCode;
import org.h2.jaqu.constant.SysProperties;
import org.h2.jaqu.jdbc.JdbcSQLException;

/**
 * Generic utility methods.
 */
public class Utils {
	private static final AtomicLong COUNTER = new AtomicLong(0);

	private static final boolean MAKE_ACCESSIBLE = true;
	private static final boolean ALLOW_ALL_CLASSES;
    private static final HashSet<String> ALLOWED_CLASS_NAMES = Utils.newHashSet();
    private static final String[] ALLOWED_CLASS_NAME_PREFIXES;

	static {
        String s = SysProperties.ALLOWED_CLASSES;
        ArrayList<String> prefixes = Utils.newArrayList();
        boolean allowAll = false;
        for (String p : StringUtils.arraySplit(s, ',', true)) {
            if (p.equals("*")) {
                allowAll = true;
            } else if (p.endsWith("*")) {
                prefixes.add(p.substring(0, p.length() - 1));
            } else {
                ALLOWED_CLASS_NAMES.add(p);
            }
        }
        ALLOW_ALL_CLASSES = allowAll;
        ALLOWED_CLASS_NAME_PREFIXES = new String[prefixes.size()];
        prefixes.toArray(ALLOWED_CLASS_NAME_PREFIXES);
    }


	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}

	public static <T> HashSet<T> newHashSet() {
		return new HashSet<T>();
	}

	public static <A, B> HashMap<A, B> newHashMap() {
		return new HashMap<A, B>();
	}

	public static <A, B> Map<A, B> newSynchronizedHashMap() {
		HashMap<A, B> map = newHashMap();
		return Collections.synchronizedMap(map);
	}

	public static <A, B> WeakIdentityHashMap<A, B> newWeakIdentityHashMap() {
		return new WeakIdentityHashMap<A, B>();
	}

	public static <A, B> IdentityHashMap<A, B> newIdentityHashMap() {
		return new IdentityHashMap<A, B>();
	}

	@SuppressWarnings("unchecked")
	public static <T> T newObject(Class<T> clazz) {
		// must create new instances
		if (clazz == Integer.class) {
			return (T) new Integer((int) COUNTER.incrementAndGet());
		}
		else if (clazz == String.class) {
			return (T) ("" + COUNTER.incrementAndGet());
		}
		else if (clazz == Long.class) {
			return (T) new Long(COUNTER.incrementAndGet());
		}
		else if (clazz == Short.class) {
			return (T) new Short((short) COUNTER.incrementAndGet());
		}
		else if (clazz == Byte.class) {
			return (T) new Byte((byte) COUNTER.incrementAndGet());
		}
		else if (clazz == Float.class) {
			return (T) new Float(COUNTER.incrementAndGet());
		}
		else if (clazz == Double.class) {
			return (T) new Double(COUNTER.incrementAndGet());
		}
		else if (clazz == Boolean.class) {
			return (T) new Boolean(false);
		}
		else if (clazz == BigDecimal.class) {
			return (T) new BigDecimal(COUNTER.incrementAndGet());
		}
		else if (clazz == BigInteger.class) {
			return (T) new BigInteger("" + COUNTER.incrementAndGet());
		}
		else if (clazz == java.sql.Date.class) {
			return (T) new java.sql.Date(COUNTER.incrementAndGet());
		}
		else if (clazz == java.sql.Time.class) {
			return (T) new java.sql.Time(COUNTER.incrementAndGet());
		}
		else if (clazz == java.sql.Timestamp.class) {
			return (T) new java.sql.Timestamp(COUNTER.incrementAndGet());
		}
		else if (clazz == java.util.Date.class) {
			return (T) new java.util.Date(COUNTER.incrementAndGet());
		}
		else if (clazz == List.class) {
			return (T) new ArrayList();
		}
		else if (clazz == Set.class) {
			return (T) new HashSet();
		}
		try {
			return clazz.newInstance();
		}
		catch (Throwable e) {
			if (MAKE_ACCESSIBLE) {
				Constructor[] constructors = clazz.getDeclaredConstructors();
				// try 0 length constructors
				for (Constructor c : constructors) {
					if (c.getParameterTypes().length == 0) {
						c.setAccessible(true);
						try {
							return clazz.newInstance();
						}
						catch (Exception e2) {
							System.out.println(e2.getMessage());
						}
						finally {
							c.setAccessible(false);
						}
					}
				}
				// try 1 length constructors
				for (Constructor c : constructors) {
					if (c.getParameterTypes().length == 1) {
						c.setAccessible(true);
						try {
							return (T) c.newInstance(new Object[1]);
						}
						catch (Exception e2) {
							// ignore
						}
					}
				}
			}
			throw new RuntimeException("Exception trying to create " + clazz.getName() + ": " + e, e);
		}
	}

	public static <T> boolean isSimpleType(Class<T> clazz) {
		if (Number.class.isAssignableFrom(clazz)) {
			return true;
		}
		else if (clazz == String.class) {
			return true;
		}
		return false;
	}

	public static Object convert(Object o, Class<?> targetType) {
		if (o == null) {
			return null;
		}
		Class<?> currentType = o.getClass();
		if (targetType.isAssignableFrom(currentType)) {
			return o;
		}
		if (targetType == String.class) {
			if (Clob.class.isAssignableFrom(currentType)) {
				Clob c = (Clob) o;
				try {
					Reader r = c.getCharacterStream();
					return IOUtils.readStringAndClose(r, -1);
				}
				catch (Exception e) {
					throw new RuntimeException("Error converting CLOB to String: " + e.toString(), e);
				}
			}
			return o.toString();
		}
		if (targetType.getAnnotation(Entity.class) != null) {
			// the current value is a primary key for a related table.
			try {
				return targetType.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException("Can not convert the value " + o + " from " + currentType + " to " + targetType);
			}
		}
		if (Number.class.isAssignableFrom(currentType)) {
			Number n = (Number) o;
			if (targetType == Integer.class) {
				return n.intValue();
			}
			else if (targetType == Long.class) {
				return n.longValue();
			}
			else if (targetType == Double.class) {
				return n.doubleValue();
			}
			else if (targetType == Float.class) {
				return n.floatValue();
			}
		}
		throw new RuntimeException("Can not convert the value " + o + " from " + currentType + " to " + targetType);
	}

	/**
	 * Load a class, but check if it is allowed to load this class first. To perform access rights checking, the system property
	 * h2.allowedClasses needs to be set to a list of class file name prefixes.
	 * 
	 * @param className the name of the class
	 * @return the class object
	 * @throws JdbcSQLException 
	 */
	public static Class<?> loadUserClass(String className) throws JdbcSQLException {
		if (!ALLOW_ALL_CLASSES && !ALLOWED_CLASS_NAMES.contains(className)) {
			boolean allowed = false;
			for (String s : ALLOWED_CLASS_NAME_PREFIXES) {
				if (className.startsWith(s)) {
					allowed = true;
				}
			}
			if (!allowed) {
				throw JdbcUtils.getJdbcSQLException(ErrorCode.ACCESS_DENIED_TO_CLASS_1, null, className);
			}
		}
		try {
			return Class.forName(className);
		}
		catch (ClassNotFoundException e) {
			try {
				return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
			}
			catch (Exception e2) {
				throw JdbcUtils.getJdbcSQLException(ErrorCode.CLASS_NOT_FOUND_1, e, className);
			}
		}
		catch (NoClassDefFoundError e) {
			throw JdbcUtils.getJdbcSQLException(ErrorCode.CLASS_NOT_FOUND_1, e, className);
		}
		catch (Error e) {
			// UnsupportedClassVersionError
			throw JdbcUtils.getJdbcSQLException(ErrorCode.GENERAL_ERROR_1, e, className);
		}
	}
}