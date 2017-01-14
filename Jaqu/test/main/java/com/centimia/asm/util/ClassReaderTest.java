/*
 * Copyright (c) 2008-2012 Shai Bentin & Centimia Inc..
 * All rights reserved.  Unpublished -- rights reserved
 *
 * Use of a copyright notice is precautionary only, and does
 * not imply publication or disclosure.
 *
 * THIS SOFTWARE CONTAINS CONFIDENTIAL INFORMATION AND TRADE
 * SECRETS OF Shai Bentin USE, DISCLOSURE, OR
 * REPRODUCTION IS PROHIBITED WITHOUT THE PRIOR EXPRESS
 * WRITTEN PERMISSION OF Shai Bentin & CENTIMIA, INC.
 */
package com.centimia.asm.util;

import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * A helper to generate ASM style byte code building
 * @author shai
 */
public class ClassReaderTest {

    public static void main(String[] args) throws Exception {
    	InputStream in = ClassReaderTest.class.getResourceAsStream("TestASM.class");
        ClassReader reader = new ClassReader(in);
        int flags = ClassReader.SKIP_DEBUG;
        reader.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)), flags);
    }
}