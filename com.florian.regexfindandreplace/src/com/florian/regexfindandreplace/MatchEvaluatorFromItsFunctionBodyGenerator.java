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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;

public class MatchEvaluatorFromItsFunctionBodyGenerator {
	private static int i = 0;

	private File javaCompiler;

	private File sourceFile;
	private File classFile;

	public MatchEvaluatorFromItsFunctionBodyGenerator(File javaCompiler) {
		this.javaCompiler = javaCompiler;
	}

	public IMatchEvaluator getMatchEvaluatorFromItsFunctionBody(String functionBody)
			throws CouldNotCompileJavaSourceCodeException, IOException, InterruptedException, ClassNotFoundException,
			NoSuchMethodException, SecurityException {

		try {

			createSourceFile();
			classFile = new File(sourceFile.getParent(), sourceFile.getName().replace(".java", ".class"));
			writeSourceFile(functionBody);
			compileClassFile();

			return loadMatchEvaluatorFromClassFile();

		} catch (Exception exception) {
			throw exception;
		}
	}

	private void createSourceFile() throws IOException {
		sourceFile = null;
		++i;
		try {
			sourceFile = File.createTempFile("MatchEvaluator" + Integer.toString(i), ".java");
		} catch (Exception e) {
			new File("MatchEvaluators").mkdir();
			sourceFile = new File(new File("MatchEvaluators"), "MatchEvaluator" + Integer.toString(i) + ".java");
			if (!sourceFile.createNewFile())
				throw new IOException(sourceFile.getAbsolutePath() + " already existed!");
		}
	}

	private void writeSourceFile(String functionBody) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(sourceFile);
		writer.println("import java.util.regex.*;");
		writer.println("public class " + getClassNameFromJavaFile(sourceFile) + "{");
		writer.println("public static String evaluateMatch(Matcher match){");
		writer.println(functionBody);
		writer.println("}");
		writer.println("}");
		writer.close();
	}

	private void compileClassFile() throws IOException, InterruptedException, CouldNotCompileJavaSourceCodeException {
		ProcessBuilder processBuilder = new ProcessBuilder(javaCompiler.getAbsolutePath(),
				sourceFile.getAbsolutePath());
		Process p = processBuilder.start();
		String processOutput = getProcessOutput(p);

		int result = p.waitFor();
		if (result != 0) {
			throw new CouldNotCompileJavaSourceCodeException(processOutput);

		}
		assert classFile != null && classFile.exists();
	}

	private String getProcessOutput(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		return sb.toString();
	}

	private IMatchEvaluator loadMatchEvaluatorFromClassFile()
			throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, SecurityException {

		URL url = new File(classFile.getAbsolutePath()).getParentFile().toURI().toURL();
		URL[] urls = new URL[] { url };
		ClassLoader classLoader = new URLClassLoader(urls);
		Class c = classLoader.loadClass(getClassNameFromJavaFile(sourceFile));
		final Method method = c.getMethod("evaluateMatch", Matcher.class);
		return new IMatchEvaluator() {

			@Override
			public String evaluateMatch(Matcher match) throws Exception {
				return (String) method.invoke(null, match);
			}

		};
	}

	public static String getClassNameFromJavaFile(File javaFile) {
		String filename = javaFile.getName();
		return filename.substring(0, filename.length() - ".java".length());
	}

}
