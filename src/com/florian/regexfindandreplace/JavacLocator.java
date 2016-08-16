package com.florian.regexfindandreplace;

import java.io.File;
import java.io.FilenameFilter;

public class JavacLocator {

	public static File getJavacLocation() {
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
