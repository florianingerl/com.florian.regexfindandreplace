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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.florian.regexfindandreplace.CouldNotCompileJavaSourceCodeException;
import com.florian.regexfindandreplace.IMatchEvaluator;
import com.florian.regexfindandreplace.MatchEvaluatorFromItsFunctionBodyGenerator;
import com.florian.regexfindandreplace.RegexUtils;

public class MatchResultEvaluatorFromItsFunctionBodyGeneratorUnitTests {

	private File javaCompiler = new File("C:\\Program Files\\Java\\jdk1.8.0_92\\bin\\javac.exe");

	@Test
	public void getMatchResultEvaluatorFromItsFunctionBody_JustANormalStringForFunctionBody_GetsTheRequiredMatchResultEvaluator() {

		try {
			String functionBody = "return \"Hello\";";
			MatchEvaluatorFromItsFunctionBodyGenerator generator = new MatchEvaluatorFromItsFunctionBodyGenerator(
					javaCompiler);
			IMatchEvaluator evaluator = generator.getMatchEvaluatorFromItsFunctionBody(functionBody);

			assertEquals("Hello", evaluator.evaluateMatch(null));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void getMatchResultEvaluatorFromItsFunctionBody_IncreaseEvery2DigitNumberBy1_GetsTheRequiredMatchResultEvaluator() {

		try {
			String functionBody = "int i = Integer.parseInt(match.group());\nreturn \"\"+(i+1);";
			MatchEvaluatorFromItsFunctionBodyGenerator generator = new MatchEvaluatorFromItsFunctionBodyGenerator(
					javaCompiler);
			IMatchEvaluator evaluator = generator.getMatchEvaluatorFromItsFunctionBody(functionBody);

			String input = "Florian is 23 years old. His sister is 2 years older. She is 25 years old.";
			input = RegexUtils.replaceAll(input, "\\d{2}", evaluator, 0);

			assertEquals("Florian is 24 years old. His sister is 2 years older. She is 26 years old.", input);
		} catch (Exception e) {

			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void getMatchResultEvaluatorFromItsFunctionBody_WrongJavaSyntax_ThrowsACouldNotCompileJavaSourceCodeException() {

		try {
			String functionBody = "int i = Integer.parseInt(match.group());\nreturn \"\"+(i+1)";
			MatchEvaluatorFromItsFunctionBodyGenerator generator = new MatchEvaluatorFromItsFunctionBodyGenerator(
					javaCompiler);
			IMatchEvaluator evaluator = generator.getMatchEvaluatorFromItsFunctionBody(functionBody);
		} catch (CouldNotCompileJavaSourceCodeException e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("';' expected"));
			return;
		} catch (Exception e) {

		}
		fail("The above exception is thrown!");

	}

	@Test
	public void getClassNameFromJavaFileTest() {
		File file = new File("Test.java");
		assertEquals("Test", MatchEvaluatorFromItsFunctionBodyGenerator.getClassNameFromJavaFile(file));
	}

	@Test
	public void getMatchResultEvaluatorFromItsFunctionBody_WithAMatchEvaluatorWhereTheCalenderClassNeedsToBeImported_CalenderClassCanBeResolved() {
		try {
			String functionBody = "java.util.Calendar calender = java.util.Calendar.getInstance();\r\n"
					+ "				calender.set(Integer.parseInt(match.group(\"year\")), Integer.parseInt(match.group(\"month\")) - 1,\r\n"
					+ "						Integer.parseInt(match.group(\"day\")));\r\n"
					+ "				int dayOfWeek = calender.get(java.util.Calendar.DAY_OF_WEEK);\r\n" + "\r\n"
					+ "				String[] daysOfWeek = { \"Sonntag\", \"Montag\", \"Dienstag\", \"Mittwoch\", \"Donnerstag\", \"Freitag\",\r\n"
					+ "						\"Samstag\" };\r\n"
					+ "				return daysOfWeek[dayOfWeek - 1] + \", der \" + match.group(\"date\");";
			MatchEvaluatorFromItsFunctionBodyGenerator generator = new MatchEvaluatorFromItsFunctionBodyGenerator(
					javaCompiler);
			IMatchEvaluator evaluator = generator.getMatchEvaluatorFromItsFunctionBody(functionBody);

			String input = "Heute ist der 28.08.2016";
			input = RegexUtils.replaceAll(input,
					"(der\\s+)?(?<date>(?<day>\\d{2})\\.(?<month>\\d{2})\\.(?<year>\\d{4}))", evaluator, 0);

			assertEquals("Heute ist Sonntag, der 28.08.2016", input);
		} catch (Exception e) {

			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void getMatchResultEvaluatorFromItsFunctionBody_WithAMatchEvaluatorWithAnInitializerAndMultipleMethodsAndFields_ItCanBeCompiledAllRight() {
		try {
			String functionBody = "day = Integer.parseInt( match.group(\"day\") );"
					+ "return getOrdinalNumberOfDay() +\" \"+ monthNames[Integer.parseInt(match.group(\"month\")) - 1] + \" \" + match.group(\"year\");\r\n"
					+ "			}\r\n" + " { System.out.println(\"This is a constructor for an anonymous class.\"); }"
					+ "         private int day;\r\n"
					+ "			private final String [] monthNames = {\"January\", \"February\", \"March\", \"April\", \"May\", \"June\", \"July\", \"August\", \"September\", \"Oktober\", \"November\", \"December\"};\r\n"
					+ "			\r\n" + "			public String getOrdinalNumberOfDay()\r\n" + "			{\r\n"
					+ "				String extension = \"th\";\r\n"
					+ "				if( day== 1) extension = \"st\";\r\n"
					+ "				else if( day== 2) extension = \"nd\";\r\n"
					+ "				else if ( day==3) extension = \"rd\";\r\n"
					+ "				return day + extension;";
			MatchEvaluatorFromItsFunctionBodyGenerator generator = new MatchEvaluatorFromItsFunctionBodyGenerator(
					javaCompiler);
			IMatchEvaluator evaluator = generator.getMatchEvaluatorFromItsFunctionBody(functionBody);

			String input = "27.08.2016 02.05.2017 03.09.2016 01.02.2016";
			input = RegexUtils.replaceAll(input, "(?<day>\\d{2})\\.(?<month>\\d{2})\\.(?<year>\\d{4})", evaluator, 0);

			assertEquals("27th August 2016 2nd May 2017 3rd September 2016 1st February 2016", input);
		} catch (Exception e) {

			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
