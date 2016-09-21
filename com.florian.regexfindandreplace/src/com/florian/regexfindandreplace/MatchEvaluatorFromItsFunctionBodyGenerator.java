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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Function;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.ingerlflori.util.regex.MatchResult;

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
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		createSourceFile();
		classFile = new File(sourceFile.getParent(), sourceFile.getName().replace(".java", ".class"));
		writeSourceFile(functionBody);
		compileClassFile();

		return loadMatchEvaluatorFromClassFile();
	}

	private void createSourceFile() throws IOException {
		sourceFile = null;
		++i;
		try {
			sourceFile = File.createTempFile("MatchEvaluatorProvider" + Integer.toString(i), ".java");
		} catch (Exception e) {
			new File("MatchEvaluators").mkdir();
			sourceFile = new File(new File("MatchEvaluators"),
					"MatchEvaluatorProvider" + Integer.toString(i) + ".java");
			if (!sourceFile.createNewFile())
				throw new IOException(sourceFile.getAbsolutePath() + " already existed!");
		}
	}

	private void writeSourceFile(String functionBody) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(sourceFile);
		// writer.println("import
		// com.florian.regexfindandreplace.IMatchEvaluator;");
		writer.println("import com.ingerlflori.util.regex.*;");
		writer.println("import java.util.function.Function;");
		writer.println("import java.util.Stack;");
		writer.println("public class " + getClassNameFromJavaFile(sourceFile) + "{");
		writer.println("public static Function<MatchResult,String> getMatchEvaluator(){");
		writer.println("return new Function<MatchResult,String>(){");
		writer.println("@Override");
		writer.println("public String apply(MatchResult match) {");
		writer.println(functionBody);
		writer.println("}");
		writer.println("};");
		writer.println("}");
		writer.println("}");
		writer.close();
	}

	private void compileClassFile() throws IOException, InterruptedException, CouldNotCompileJavaSourceCodeException {

		ProcessBuilder processBuilder = new ProcessBuilder(javaCompiler.getAbsolutePath());
		processBuilder.command().add("-cp");
		IClassPathProvider cpp = ServiceLocator.getInjector().getInstance(IClassPathProvider.class);
		processBuilder.command().add(cpp.getClassPath());
		processBuilder.command().add(sourceFile.getAbsolutePath());

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
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException {

		URL url = new File(classFile.getAbsolutePath()).getParentFile().toURI().toURL();
		URL[] urls = new URL[] { url };
		URLClassLoader classLoader = new URLClassLoader(urls);
		Class<?> c = classLoader.loadClass(getClassNameFromJavaFile(sourceFile));
		Method method = c.getMethod("getMatchEvaluator", null);
		final Function<MatchResult, String> func = (Function<MatchResult, String>) method.invoke(null);
		return new IMatchEvaluator() {

			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				return func.apply(match);
			}

		};

	}

	public static String getClassNameFromJavaFile(File javaFile) {
		String filename = javaFile.getName();
		return filename.substring(0, filename.length() - ".java".length());
	}

}
