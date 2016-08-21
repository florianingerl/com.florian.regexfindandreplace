package com.florian.regexfindandreplace.unittests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.florian.regexfindandreplace.JavacLocator;

public class JavacLocatorTest {

	@Test
	public void getJavacLocation_OnThisPC_ReturnsAFileToTheJavacCompiler() {
		File file = JavacLocator.getJavacLocation();

		assertNotNull(file);
		assertTrue(file.exists() && file.getName().equals("javac.exe"));

	}

}
