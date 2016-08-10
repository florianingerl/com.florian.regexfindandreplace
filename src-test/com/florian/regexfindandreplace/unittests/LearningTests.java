package com.florian.regexfindandreplace.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

}
