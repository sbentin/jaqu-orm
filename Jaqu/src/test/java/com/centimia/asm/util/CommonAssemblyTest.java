package com.centimia.asm.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.centimia.orm.jaqu.ext.common.CommonAssembly;

class CommonAssemblyTest {

	@Test
	void testAssembleFile() {
		File classFile = new File("C:\\Users\\shai\\git\\jaqu-orm\\Jaqu\\bin\\default\\com\\centimia\\asm\\util\\CommonAssemblyTestModel.class");
		try {
			if (!CommonAssembly.assembleFile(classFile))
				fail("Assembly failed");
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Assembly failed with exception");
		}
	}

}
