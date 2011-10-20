/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.jaqu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.h2.jaqu.constant.Constants;
import org.h2.jaqu.jdbc.JdbcSQLException;

/**
 * This utility class contains input/output functions.
 */
public class IOUtils {

    private IOUtils() {
        // utility class
    }

    /**
     * Close an output stream without throwing an exception.
     *
     * @param out the output stream or null
     */
	public static void closeSilently(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			}
			catch (Exception e) {
				// ignore
			}
		}
	}

    /**
     * Skip a number of bytes in an input stream.
     *
     * @param in the input stream
     * @param skip the number of bytes to skip
     * @throws EOFException if the end of file has been reached before all bytes
     *             could be skipped
     * @throws IOException if an IO exception occurred while skipping
     */
	public static void skipFully(InputStream in, long skip) throws IOException {
		try {
			while (skip > 0) {
				long skipped = in.skip(skip);
				if (skipped <= 0) {
					throw new EOFException();
				}
				skip -= skipped;
			}
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
	}

    /**
     * Skip a number of characters in a reader.
     *
     * @param reader the reader
     * @param skip the number of characters to skip
     * @throws EOFException if the end of file has been reached before all
     *             characters could be skipped
     * @throws IOException if an IO exception occurred while skipping
     */
	public static void skipFully(Reader reader, long skip) throws IOException {
		try {
			while (skip > 0) {
				long skipped = reader.skip(skip);
				if (skipped <= 0) {
					throw new EOFException();
				}
				skip -= skipped;
			}
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
	}

    /**
     * Copy all data from the input stream to the output stream and close both
     * streams. Exceptions while closing are ignored.
     *
     * @param in the input stream
     * @param out the output stream
     * @return the number of bytes copied
     */
	public static long copyAndClose(InputStream in, OutputStream out) throws IOException {
		try {
			long len = copyAndCloseInput(in, out);
			out.close();
			return len;
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
		finally {
			closeSilently(out);
		}
	}

    /**
     * Copy all data from the input stream to the output stream and close the
     * input stream. Exceptions while closing are ignored.
     *
     * @param in the input stream
     * @param out the output stream (null if writing is not required)
     * @return the number of bytes copied
     */
	public static long copyAndCloseInput(InputStream in, OutputStream out) throws IOException {
		try {
			return copy(in, out);
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
		finally {
			closeSilently(in);
		}
	}

    /**
     * Copy all data from the input stream to the output stream. Both streams
     * are kept open.
     *
     * @param in the input stream
     * @param out the output stream (null if writing is not required)
     * @return the number of bytes copied
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, Long.MAX_VALUE);
    }

    /**
     * Copy all data from the input stream to the output stream. Both streams
     * are kept open.
     *
     * @param in the input stream
     * @param out the output stream (null if writing is not required)
     * @param length the maximum number of bytes to copy
     * @return the number of bytes copied
     */
	public static long copy(InputStream in, OutputStream out, long length) throws IOException {
		try {
			long copied = 0;
			int len = (int) Math.min(length, Constants.IO_BUFFER_SIZE);
			byte[] buffer = new byte[len];
			while (length > 0) {
				len = in.read(buffer, 0, len);
				if (len < 0) {
					break;
				}
				if (out != null) {
					out.write(buffer, 0, len);
				}
				copied += len;
				length -= len;
				len = (int) Math.min(length, Constants.IO_BUFFER_SIZE);
			}
			return copied;
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
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
     * Close an input stream without throwing an exception.
     *
     * @param in the input stream or null
     */
	public static void closeSilently(InputStream in) {
		if (in != null) {
			try {
				in.close();
			}
			catch (Exception e) {
				// ignore
			}
		}
	}

    /**
     * Close a reader without throwing an exception.
     *
     * @param reader the reader or null
     */
	public static void closeSilently(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			}
			catch (Exception e) {
				// ignore
			}
		}
	}

    /**
     * Close a writer without throwing an exception.
     *
     * @param writer the writer or null
     */
	public static void closeSilently(Writer writer) {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			}
			catch (Exception e) {
				// ignore
			}
		}
	}

    /**
     * Read a number of bytes from an input stream and close the stream.
     *
     * @param in the input stream
     * @param length the maximum number of bytes to read, or -1 to read until
     *            the end of file
     * @return the bytes read
     */
	public static byte[] readBytesAndClose(InputStream in, int length) throws IOException {
		try {
			if (length <= 0) {
				length = Integer.MAX_VALUE;
			}
			int block = Math.min(Constants.IO_BUFFER_SIZE, length);
			ByteArrayOutputStream out = new ByteArrayOutputStream(block);
			copy(in, out, length);
			return out.toByteArray();
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
     * @param length the maximum number of characters to read, or -1 to read
     *            until the end of file
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
     * Try to read the given number of bytes to the buffer. This method reads
     * until the maximum number of bytes have been read or until the end of
     * file.
     *
     * @param in the input stream
     * @param buffer the output buffer
     * @param off the offset in the buffer
     * @param max the number of bytes to read at most
     * @return the number of bytes read, 0 meaning EOF
     */
	public static int readFully(InputStream in, byte[] buffer, int off, int max) throws IOException {
		try {
			int len = Math.min(max, buffer.length);
			int result = 0;
			while (len > 0) {
				int l = in.read(buffer, off, len);
				if (l < 0) {
					break;
				}
				result += l;
				off += l;
				len -= l;
			}
			return result;
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
	}

    /**
     * Try to read the given number of characters to the buffer. This method
     * reads until the maximum number of characters have been read or until the
     * end of file.
     *
     * @param in the reader
     * @param buffer the output buffer
     * @param max the number of characters to read at most
     * @return the number of characters read
     */
	public static int readFully(Reader in, char[] buffer, int max) throws IOException {
		try {
			int off = 0, len = Math.min(max, buffer.length);
			if (len == 0) {
				return 0;
			}
			while (true) {
				int l = len - off;
				if (l <= 0) {
					break;
				}
				l = in.read(buffer, off, l);
				if (l < 0) {
					break;
				}
				off += l;
			}
			return off <= 0 ? -1 : off;
		}
		catch (Exception e) {
			throw convertToIOException(e);
		}
	}

    /**
     * Create a buffered reader to read from an input stream using the UTF-8
     * format. If the input stream is null, this method returns null. The
     * InputStreamReader that is used here is not exact, that means it may read
     * some additional bytes when buffering.
     *
     * @param in the input stream or null
     * @return the reader
     */
	public static Reader getBufferedReader(InputStream in) {
		try {
			//
			return in == null ? null : new BufferedReader(new InputStreamReader(in, Constants.UTF8));
		}
		catch (Exception e) {
			// UnsupportedEncodingException
			throw new RuntimeException(e);
		}
	}

    /**
     * Create a reader to read from an input stream using the UTF-8 format. If
     * the input stream is null, this method returns null. The InputStreamReader
     * that is used here is not exact, that means it may read some additional
     * bytes when buffering.
     *
     * @param in the input stream or null
     * @return the reader
     */
	public static Reader getReader(InputStream in) {
		try {
			// InputStreamReader may read some more bytes
			return in == null ? null : new BufferedReader(new InputStreamReader(in, Constants.UTF8));
		}
		catch (Exception e) {
			// UnsupportedEncodingException
			throw new RuntimeException(e);
		}
	}

    /**
     * Create a buffered writer to write to an output stream using the UTF-8
     * format. If the output stream is null, this method returns null.
     *
     * @param out the output stream or null
     * @return the writer
     */
	public static Writer getBufferedWriter(OutputStream out) {
		try {
			return out == null ? null : new BufferedWriter(new OutputStreamWriter(out, Constants.UTF8));
		}
		catch (Exception e) {
			// UnsupportedEncodingException
			throw new RuntimeException(e);
		}
	}

    /**
     * Create an input stream to read from a string. The string is converted to
     * a byte array using UTF-8 encoding.
     * If the string is null, this method returns null.
     *
     * @param s the string
     * @return the input stream
     */
	public static InputStream getInputStream(String s) {
		if (s == null) {
			return null;
		}
		return new ByteArrayInputStream(StringUtils.utf8Encode(s));
	}

	/**
	 * Create a reader to read from a string. If the string is null, this method returns null.
	 * 
	 * @param s
	 *            the string or null
	 * @return the reader
	 */
	public static Reader getReader(String s) {
		return s == null ? null : new StringReader(s);
	}

    /**
     * Wrap an input stream in a reader. The bytes are converted to characters
     * using the US-ASCII character set.
     *
     * @param in the input stream
     * @return the reader
     */
	public static Reader getAsciiReader(InputStream in) {
		try {
			return in == null ? null : new InputStreamReader(in, "US-ASCII");
		}
		catch (Exception e) {
			// UnsupportedEncodingException
			throw new RuntimeException(e);
		}
	}

    /**
     * Create the directory and all parent directories if required.
     *
     * @param directory the directory
     * @throws IOException
     */
	public static void mkdirs(File directory) throws IOException {
		// loop, to deal with race conditions (if another thread creates or
		// deletes the same directory at the same time).
		for (int i = 0; i < 5; i++) {
			if (directory.exists()) {
				if (directory.isDirectory()) {
					return;
				}
				throw new IOException("Could not create directory, because a file with the same name already exists: "
						+ directory.getAbsolutePath());
			}
			if (directory.mkdirs()) {
				return;
			}
		}
		throw new IOException("Could not create directory: " + directory.getAbsolutePath());
	}

    /**
     * Change the length of the file.
     *
     * @param file the random access file
     * @param newLength the new length
     */
	public static void setLength(RandomAccessFile file, long newLength) throws IOException {
		try {
			file.setLength(newLength);
		}
		catch (IOException e) {
			long length = file.length();
			if (newLength < length) {
				throw e;
			}
			long pos = file.getFilePointer();
			file.seek(length);
			long remaining = newLength - length;
			int maxSize = 1024 * 1024;
			int block = (int) Math.min(remaining, maxSize);
			byte[] buffer = new byte[block];
			while (remaining > 0) {
				int write = (int) Math.min(remaining, maxSize);
				file.write(buffer, 0, write);
				remaining -= write;
			}
			file.seek(pos);
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
        if (e instanceof JdbcSQLException) {
            JdbcSQLException e2 = (JdbcSQLException) e;
            if (e2.getOriginalCause() != null) {
                e = e2.getOriginalCause();
            }
        }
        IOException io = new IOException(e.toString());
       
        io.initCause(e);
        return io;
    }
}