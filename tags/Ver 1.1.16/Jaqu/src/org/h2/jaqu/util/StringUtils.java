/*
 * Copyright (c) 2007-2010 Centimia Ltd.
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 */

/*
 * Update Log
 * 
 *  Date			User				Comment
 * ------			-------				--------
 * 26/02/2011		shai				 create
 */
package org.h2.jaqu.util;

import java.util.ArrayList;

import org.h2.jaqu.constant.Constants;


/**
 * 
 * @author shai
 *
 */
public class StringUtils {

	/**
     * Replace all occurrences of the before string with the after string.
     *
     * @param s the string
     * @param before the old text
     * @param after the new text
     * @return the string with the before string replaced
     */
    public static String replaceAll(String s, String before, String after) {
        int next = s.indexOf(before);
        if (next < 0) {
            return s;
        }
        StringBuilder buff = new StringBuilder(s.length() - before.length() + after.length());
        int index = 0;
        while (true) {
            buff.append(s.substring(index, next)).append(after);
            index = next + before.length();
            next = s.indexOf(before, index);
            if (next < 0) {
                buff.append(s.substring(index));
                break;
            }
        }
        return buff.toString();
    }

    /**
     * Check if a String is null or empty (the length is null).
     *
     * @param s the string to check
     * @return true if it is null or empty
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * Enclose a string with double quotes. A double quote inside the string is
     * escaped using a double quote.
     *
     * @param s the text
     * @return the double quoted text
     */
    public static String quoteIdentifier(String s) {
        int length = s.length();
        StringBuilder buff = new StringBuilder(length + 2);
        buff.append('\"');
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '"') {
                buff.append(c);
            }
            buff.append(c);
        }
        return buff.append('\"').toString();
    }

    /**
     * Split a string into an array of strings using the given separator. A null
     * string will result in a null array, and an empty string in a zero element
     * array.
     *
     * @param s the string to split
     * @param separatorChar the separator character
     * @param trim whether each element should be trimmed
     * @return the array list
     */
	public static String[] arraySplit(String s, char separatorChar, boolean trim) {
		if (s == null) {
			return null;
		}
		int length = s.length();
		if (length == 0) {
			return new String[0];
		}
		ArrayList<String> list = Utils.newArrayList();
		StringBuilder buff = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (c == separatorChar) {
				String e = buff.toString();
				list.add(trim ? e.trim() : e);
				buff.setLength(0);
			}
			else if (c == '\\' && i < length - 1) {
				buff.append(s.charAt(++i));
			}
			else {
				buff.append(c);
			}
		}
		String e = buff.toString();
		list.add(trim ? e.trim() : e);
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}

	/**
     * Convert a string to a SQL literal. Null is converted to NULL. The text is
     * enclosed in single quotes. If there are any special characters, the method
     * STRINGDECODE is used.
     *
     * @param s the text to convert.
     * @return the SQL literal
     */
    public static String quoteStringSQL(String s) {
        if (s == null) {
            return "NULL";
        }
        int length = s.length();
        StringBuilder buff = new StringBuilder(length + 2);
        buff.append('\'');
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                buff.append(c);
            } else if (c < ' ' || c > 127) {
                // need to start from the beginning because maybe there was a \
                // that was not quoted
                return "STRINGDECODE(" + quoteStringSQL(javaEncode(s)) + ")";
            }
            buff.append(c);
        }
        buff.append('\'');
        return buff.toString();
    }
    
    /**
     * Convert a string to a Java literal using the correct escape sequences.
     * The literal is not enclosed in double quotes. The result can be used in
     * properties files or in Java source code.
     *
     * @param s the text to convert
     * @return the Java representation
     */
	public static String javaEncode(String s) {
		int length = s.length();
		StringBuilder buff = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\t':
					// HT horizontal tab
					buff.append("\\t");
					break;
				case '\n':
					// LF linefeed
					buff.append("\\n");
					break;
				case '\f':
					// FF form feed
					buff.append("\\f");
					break;
				case '\r':
					// CR carriage return
					buff.append("\\r");
					break;
				case '"':
					// double quote
					buff.append("\\\"");
					break;
				case '\\':
					// backslash
					buff.append("\\\\");
					break;
				default:
					int ch = c & 0xffff;
					if (ch >= ' ' && (ch < 0x80)) {
						buff.append(c);
					}
					else {
						buff.append("\\u");
						// make sure it's four characters
						buff.append(Integer.toHexString(0x10000 | ch).substring(1));
					}
			}
		}
		return buff.toString();
	}

    /**
     * Convert the text to UTF-8 format. For the Unicode characters
     * 0xd800-0xdfff only one byte is returned.
     *
     * @param s the text
     * @return the UTF-8 representation
     */
	public static byte[] utf8Encode(String s) {
		try {
			return s.getBytes(Constants.UTF8);
		}
		catch (Exception e) {
			// UnsupportedEncodingException
			throw new RuntimeException(e);
		}
	}

    /**
     * Convert a UTF-8 representation of a text to the text.
     *
     * @param utf8 the UTF-8 representation
     * @return the text
     */
	public static String utf8Decode(byte[] utf8) {
		try {
			return new String(utf8, Constants.UTF8);
		}
		catch (Exception e) {
			// UnsupportedEncodingException
			throw new RuntimeException(e);
		}
	}
}
