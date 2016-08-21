package com.florian.regexfindandreplace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

	public static String replaceAll(String input, String regex, IMatchEvaluator evaluator, int flags)
			throws MatchEvaluatorException {
		Pattern pattern = Pattern.compile(regex, flags);
		Matcher matcher = pattern.matcher(input);

		int index = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			sb.append(input.substring(index, matcher.start()));
			try {
				sb.append(evaluator.evaluateMatch(matcher.toMatchResult()));
			} catch (Exception exp) {
				throw new MatchEvaluatorException(exp);
			}
			index = matcher.end();
		}
		sb.append(input.substring(index));

		return sb.toString();
	}

	public static String getReplaceStringOfFirstMatch(String input, int offset, Pattern pattern,
			IMatchEvaluator evaluator) throws MatchEvaluatorException {
		Matcher matcher = pattern.matcher(input);

		if (!matcher.find(offset))
			return null;
		try {
			return evaluator.evaluateMatch(matcher.toMatchResult());
		} catch (Exception e) {
			throw new MatchEvaluatorException(e);
		}
	}

}
