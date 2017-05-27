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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class MatchEvaluatorFromItsFunctionBodyGenerator {
	private static int i = 0;

	private File sourceFile;
	private File classFile;

	public MatchEvaluatorFromItsFunctionBodyGenerator() {

	}

	public Function<Matcher, String> getMatchEvaluatorFromItsFunctionBody(String functionBody)
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
		writer.println("import java.util.regex.Matcher;");
		writer.println("import java.util.function.Function;");
		writer.println("public class " + getClassNameFromJavaFile(sourceFile) + "{");
		writer.println("public static Function<Matcher,String> getMatchEvaluator(){");
		writer.println("return new Function<Matcher,String>(){");
		writer.println("@Override");
		writer.println("public String apply(Matcher match){");
		writer.println(functionBody);
		writer.println("}");
		writer.println("};");
		writer.println("}");
		writer.println("}");
		writer.close();
	}

	private void compileClassFile() throws IOException, InterruptedException, CouldNotCompileJavaSourceCodeException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter errWriter = new PrintWriter(baos);

		boolean success = BatchCompiler.compile(
				sourceFile.getAbsolutePath() + " -d " + sourceFile.getParentFile().getAbsolutePath() + " -source 1.8",
				new PrintWriter(new ByteArrayOutputStream()), errWriter, null);

		if (!success)
			throw new CouldNotCompileJavaSourceCodeException(baos.toString());

		assert classFile != null && classFile.exists();
	}

	private Function<Matcher, String> loadMatchEvaluatorFromClassFile()
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException {

		URL url = new File(classFile.getAbsolutePath()).getParentFile().toURI().toURL();
		URL[] urls = new URL[] { url };
		URLClassLoader classLoader = new URLClassLoader(urls);
		Class<?> c = classLoader.loadClass(getClassNameFromJavaFile(sourceFile));
		Method method = c.getMethod("getMatchEvaluator");
		// classLoader.close();
		return (Function<Matcher, String>) method.invoke(null);

	}

	public static String getClassNameFromJavaFile(File javaFile) {
		String filename = javaFile.getName();
		return filename.substring(0, filename.length() - ".java".length());
	}

}
