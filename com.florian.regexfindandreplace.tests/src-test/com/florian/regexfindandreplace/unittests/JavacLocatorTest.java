/*******************************************************************************
 * Copyright (c) 2016 Florian Ingerl.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florian Ingerl, imelflorianingerl@gmail.com - initial API and implementation
 *******************************************************************************/

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
