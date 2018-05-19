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
package com.centimia.orm.jaqu.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.centimia.orm.jaqu.constant.Constants;

/**
 * This utility class contains input/output functions.
 */
public class IOUtils {

    private IOUtils() {
        // utility class
    }

    /**
     * Copy all data from the reader to the writer and close the reader.
     * Exceptions while closing are ignored.
     *
     * @param in the reader
     * @param out the writer (null if writing is not required)
     * @param length the maximum number of bytes to copy
     * @return the number of characters copied
     */
	public static long copyAndCloseInput(Reader in, Writer out, long length) throws IOException {
		try {
			long copied = 0;
			int len = (int) Math.min(length, Constants.IO_BUFFER_SIZE);
			char[] buffer = new char[len];
			while (length > 0) {
				len = in.read(buffer, 0, len);
				if (len < 0) {
					break;
				}
				if (out != null) {
					out.write(buffer, 0, len);
				}
				length -= len;
				len = (int) Math.min(length, Constants.IO_BUFFER_SIZE);
				copied += len;
			}
			return copied;
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
		finally {
			in.close();
		}
	}

    /**
     * Read a number of characters from a reader and close it.
     *
     * @param in the reader
     * @param length the maximum number of characters to read, or -1 to read until the end of file
     * @return the string read
     */
	public static String readStringAndClose(Reader in, int length) throws IOException {
		try {
			if (length <= 0) {
				length = Integer.MAX_VALUE;
			}
			int block = Math.min(Constants.IO_BUFFER_SIZE, length);
			StringWriter out = new StringWriter(block);
			copyAndCloseInput(in, out, length);
			return out.toString();
		}
		finally {
			in.close();
		}
	}

	/**
     * Convert an exception to an IO exception.
     *
     * @param e the root cause
     * @return the IO exception
     */
    public static IOException convertToIOException(Throwable e) {
        if (e instanceof IOException) {
            return (IOException) e;
        }
        IOException io = new IOException(e.toString());       
        io.initCause(e);
        return io;
    }
}