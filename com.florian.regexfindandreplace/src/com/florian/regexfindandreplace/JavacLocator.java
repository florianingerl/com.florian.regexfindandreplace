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

package com.florian.regexfindandreplace;

import java.io.File;
import java.io.FilenameFilter;

public class JavacLocator implements IJavacLocator {

	public File getJavacLocation() {
		String javaHome = System.getProperty("java.home");
		if (javaHome == null)
			return null;

		File javaHomeDir = new File(javaHome);

		File[] jdkDirs = javaHomeDir.getParentFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return file.isDirectory() && name.contains("jdk");
			}
		});

		if (jdkDirs == null || jdkDirs.length == 0) {
			return null;
		}
		File javacFile = new File(jdkDirs[0], "bin");
		javacFile = new File(javacFile, "javac.exe");

		if (javacFile.exists())
			return javacFile;

		return null;
	}

}
