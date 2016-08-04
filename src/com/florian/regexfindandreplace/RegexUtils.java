package com.florian.regexfindandreplace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

	public static String replaceAll(String input, String regex, IMatchEvaluator evaluator) throws Exception
	{
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		
		int index = 0;
		StringBuilder sb = new StringBuilder();
		while(matcher.find())
		{
			sb.append(input.substring(index, matcher.start()));
			index = matcher.end();
			sb.append(evaluator.evaluateMatch(matcher.toMatchResult()));
		}
		sb.append(input.substring(index));
		
		return sb.toString();
	}
	
}
