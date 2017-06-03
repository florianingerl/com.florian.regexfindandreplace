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

package com.florianingerl.regexfindandreplace.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

public class LearningTests {

	@Test
	public void testWheterEmptyMatchIsFound() {
		String input = "42398";

		Pattern pattern = Pattern.compile("(?<=(\\d))(?=\\d)");
		Matcher matcher = pattern.matcher(input);

		assertTrue(matcher.find());
		assertEquals("4", matcher.group(1));
	}

	@Test
	public void testToMatchWholeString() {
		String input = "Whole string is \n \r\n this \t \" Amen \n ";
		Pattern pattern = Pattern.compile("[\\s\\S]*", Pattern.MULTILINE);

		Matcher matcher = pattern.matcher(input);
		assertTrue(matcher.find());
		assertEquals(input, matcher.group());

	}

	public static String replaceAll(String input, Pattern regex, String replaceString) {
		Matcher m = regex.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, replaceString);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	@Test
	public void replaceAll_WhereAllMatchesHaveZeroLength_PerformsTheReplacementAllTheSame() {
		String input = "Germany has 80000000 inhabitants. The world has 7000000000 inhabitants.";
		Pattern regex = Pattern.compile("(?<=\\d)(?=(\\d{3})+(?!\\d))");
		input = replaceAll(input, regex, ".");
		assertEquals("Germany has 80.000.000 inhabitants. The world has 7.000.000.000 inhabitants.", input);
	}

	@Test
	public void replaceAll_WithANormalRegexReplace_ReplacesEvertyhing() {
		String input = "24.08.2016 01.04.2017";
		Pattern regex = Pattern.compile("\\b(\\d{2})\\.(\\d{2})\\.(?<year>\\d{4})\\b");
		input = replaceAll(input, regex, "$2/$1/${year}");
		assertEquals("08/24/2016 04/01/2017", input);
	}

	@Ignore
	@Test
	public void replaceAll_WorkaroundForMatchesWithZeroLength_PerformsTheReplacementAllTheSame() {
		String input = "Germany has 80000000 inhabitants. The world has 7000000000 inhabitants.";
		Pattern regex = Pattern.compile("(\\d)((\\d{3})+)(?!\\d)");
		input = replaceAll(input, regex, "$1.$2");
		assertEquals("Germany has 80.000.000 inhabitants. The world has 7.000.000.000 inhabitants.", input);
	}

	@Test
	public void replaceAllTest() {
		String input = "17_{hex}+10_{bin}=27";
		Pattern regex = Pattern.compile("\\d{2}(?=_\\{(?<format>hex|bin)\\})");
		Matcher matcher = regex.matcher(input);

		matcher.find();
		assertEquals("hex", matcher.group(1));
	}

	@Test
	public void parseIntTest() {
		int i = Integer.parseInt("08");
		assertEquals(8, i);
	}

	@Test
	public void reflectionTest1() {
		try {
			Object o = new Object() {
				public void say(String words) {
					System.out.println(words);
				}
			};
			Class<?> c = o.getClass();
			Method m = c.getMethod("say", String.class);
			m.invoke(o, "Hello");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

	}

}
