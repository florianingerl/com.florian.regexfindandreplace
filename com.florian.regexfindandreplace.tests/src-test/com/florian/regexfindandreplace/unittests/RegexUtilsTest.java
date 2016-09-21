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

import java.util.Calendar;

import com.ingerlflori.util.regex.MatchResult;
import com.ingerlflori.util.regex.Matcher;
import com.ingerlflori.util.regex.Pattern;

import org.junit.Test;

import com.florian.regexfindandreplace.IMatchEvaluator;
import com.florian.regexfindandreplace.MatchEvaluatorException;
import com.florian.regexfindandreplace.RegexUtils;

public class RegexUtilsTest {

	@Test
	public void replaceAll_WhenNoMatchCanBeFound_ReturnsTheUnchangedInputString() {
		String input = "Heilig ist der Herr";

		try {
			input = RegexUtils.replaceAll(input, "\\d", null, 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("This shouldn't throw an exception!");
		}

		assertEquals("Heilig ist der Herr", input);
	}

	@Test
	public void replaceAll_WithAMatchEvaluator_MatchEvaluatorIsCorrectlyCalled() {
		String input = "Florian is 23 years old. His sister is 2 years older. She is 25 years old!";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {

			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				int i = Integer.parseInt(match.group());
				return "" + (i + 1);
			}

		};
		try {
			input = RegexUtils.replaceAll(input, "\\d{2}", matchEvaluator, 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("This shouldn't throw an exception!");
		}

		assertEquals("Florian is 24 years old. His sister is 2 years older. She is 26 years old!", input);
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorAndACaseInsensitiveSearch_MatchEvaluatorIsCorrectlyCalled() {
		String input = "All Nouns in the English Language don't start with capital Letters";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {

			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				String temp = match.group();
				return Character.toLowerCase(temp.charAt(0)) + temp.substring(1);
			}

		};
		try {
			input = RegexUtils.replaceAll(input, "(nouns|language|letters)", matchEvaluator, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			e.printStackTrace();
			fail("This shouldn't throw an exception!");
		}

		assertEquals("All nouns in the English language don't start with capital letters", input);
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorAndACaseSensitiveSearch_NoMatchIsFound() {
		String input = "C";
		try {
			input = RegexUtils.replaceAll(input, "c", new IMatchEvaluator() {

				@Override
				public String evaluateMatch(MatchResult match) throws Exception {
					return "A";
				}
			}, 0);
		} catch (MatchEvaluatorException e) {
			fail("This shouldn't happen!");
		}
		assertEquals("C", input);

	}

	@Test
	public void replaceAll_WhenTheMatchEvaluatorThrowsAnException_AMatchEvaluatorExceptionIsThrown() {
		String input = "No matter";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {
			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				return match.group(100); // The group is out of range
			}

		};

		try {
			input = RegexUtils.replaceAll(input, "M", matchEvaluator, Pattern.CASE_INSENSITIVE);
		} catch (MatchEvaluatorException e) {
			assertTrue(e.getCause() instanceof IndexOutOfBoundsException);
			return;
		}
		fail("The previous call should have thrown the exception!");
	}

	@Test
	public void replaceAll_WhenTheMatchesAreEmptyStrings_TheseCanAlsoBeReplaced() {
		String input = "In Deutschland leben 80000000 Menschen!Auf der Welt leben 7000000000 Menschen";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {

			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				return ".";
			}

		};
		try {
			input = RegexUtils.replaceAll(input, "(?<=\\d)(?=(\\d{3})+(?!\\d))", matchEvaluator, 0);
		} catch (MatchEvaluatorException e) {
			fail("This exception should not happen!");
		}

		assertEquals("In Deutschland leben 80.000.000 Menschen!Auf der Welt leben 7.000.000.000 Menschen", input);

	}

	@Test
	public void replaceAll_WhereALookAheadCapturesAGroup_PerformsTheCorrectReplacement() {
		String input = "17_{hex}+10_{bin}=27";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {

			@Override
			public String evaluateMatch(MatchResult match) throws Exception {

				if (match.group(1).equals("hex")) {
					return Integer.toHexString(Integer.parseInt(match.group()));
				} else if (match.group(1).equals("bin")) {
					return Integer.toBinaryString(Integer.parseInt(match.group()));
				}
				return null;
			}

		};
		try {
			input = RegexUtils.replaceAll(input, "\\d{2}(?=_\\{(hex|bin)\\})", matchEvaluator, 0);
		} catch (MatchEvaluatorException e) {
			fail();
		}
		assertEquals("11_{hex}+1010_{bin}=27", input);

	}

	@Test
	public void replaceAll_WhereALookbehindCapturesAGroup_PerformsTheCorrectReplacement() {
		String input = "x17=17 and 010=10";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {
			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				if (match.group(1).equals("x")) {
					return Integer.toHexString(Integer.parseInt(match.group()));
				} else if (match.group(1).equals("0")) {
					return Integer.toOctalString(Integer.parseInt(match.group()));
				}
				return null;
			}

		};
		try {
			input = RegexUtils.replaceAll(input, "(?<=(x|0))[1-9]\\d*", matchEvaluator, 0);
		} catch (MatchEvaluatorException e) {
			fail();
		}
		assertEquals("x11=17 and 012=10", input);

	}

	@Test
	public void replaceAll_ToMakeStaticFinalConstantsFollowTheNamingConvention_ItJustWorks() {
		String input = "public static final int caseInsensitive = 0;" + "public static final int canonEq = 1;"
				+ "public static final int unicodeCase =2;" + "public static final int unixLines = 3;"
				+ "public static final int dotall = 4;";
		String regex = "(?![A-Z_]+\\s*=)(?<identifier>[\\w_]+)(\\s*=)";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {
			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				String identifier = match.group("identifier");
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < identifier.length(); i++) {
					char c = identifier.charAt(i);
					if (Character.isUpperCase(c))
						sb.append("_");
					sb.append(Character.toUpperCase(c));
				}
				return sb.toString() + match.group(2);
			}

		};

		try {
			input = RegexUtils.replaceAll(input, regex, matchEvaluator, Pattern.MULTILINE);
		} catch (MatchEvaluatorException e) {
			fail();
		}
		assertEquals("public static final int CASE_INSENSITIVE = 0;" + "public static final int CANON_EQ = 1;"
				+ "public static final int UNICODE_CASE =2;" + "public static final int UNIX_LINES = 3;"
				+ "public static final int DOTALL = 4;", input);

	}

	@Test
	public void replaceAll_ToBringDatesInANicerFormat_ItJustWorks() {
		String input = "Heute ist der 27.08.2016";
		String regex = "(der\\s+)?(?<date>(?<day>\\d{2})\\.(?<month>\\d{2})\\.(?<year>\\d{4}))";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {
			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				Calendar calender = Calendar.getInstance();
				calender.set(Integer.parseInt(match.group("year")), Integer.parseInt(match.group("month")) - 1,
						Integer.parseInt(match.group("day")));
				int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK);

				String[] daysOfWeek = { "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag",
						"Samstag" };
				return daysOfWeek[dayOfWeek - 1] + ", der " + match.group("date");
			}

		};

		try {
			input = RegexUtils.replaceAll(input, regex, matchEvaluator, Pattern.MULTILINE);
		} catch (MatchEvaluatorException e) {
			fail();
		}
		assertEquals("Heute ist Samstag, der 27.08.2016", input);

	}

	@Test
	public void getReplaceStringOfFirstMatch_WhereALookbehindCapturesAGroup_GetsTheCorrectReplacementString() {
		String input = "x17=17 and 010=10";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {
			@Override
			public String evaluateMatch(MatchResult match) throws Exception {
				if (match.group(1).equals("x")) {
					return Integer.toHexString(Integer.parseInt(match.group()));
				} else if (match.group(1).equals("0")) {
					return Integer.toOctalString(Integer.parseInt(match.group()));
				}
				return null;
			}

		};
		try {
			Pattern pattern = Pattern.compile("(?<=(x|0))[1-9]\\d*", Pattern.MULTILINE);
			String replacement = RegexUtils.getReplaceStringOfFirstMatch(input, 1, pattern, matchEvaluator);
			assertEquals("11", replacement);
			replacement = RegexUtils.getReplaceStringOfFirstMatch(input, 12, pattern, matchEvaluator);
			assertEquals("12", replacement);
		} catch (MatchEvaluatorException e) {
			fail();
		}
	}

	@Test
	public void getReplaceStringOfFirstMatch_WhereAheadCapturesAGroup_GetsTheCorrectReplacementString() {
		String input = "17_{hex}+10_{bin}=27";
		IMatchEvaluator matchEvaluator = new IMatchEvaluator() {

			@Override
			public String evaluateMatch(MatchResult match) throws Exception {

				if (match.group(1).equals("hex")) {
					return Integer.toHexString(Integer.parseInt(match.group()));
				} else if (match.group(1).equals("bin")) {
					return Integer.toBinaryString(Integer.parseInt(match.group()));
				}
				return null;
			}

		};
		try {
			Pattern pattern = Pattern.compile("\\d{2}(?=_\\{(hex|bin)\\})", Pattern.MULTILINE);
			String replacement = RegexUtils.getReplaceStringOfFirstMatch(input, 0, pattern, matchEvaluator);
			assertEquals("11", replacement);
			replacement = RegexUtils.getReplaceStringOfFirstMatch(input, 9, pattern, matchEvaluator);
			assertEquals("1010", replacement);
		} catch (MatchEvaluatorException e) {
			fail();
		}
	}

}
