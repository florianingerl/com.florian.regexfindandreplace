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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

	public static String replaceAll(String input, String regex, Function<Matcher, String> evaluator, int flags)
			throws MatchEvaluatorException {
		Pattern pattern = Pattern.compile(regex, flags);
		Matcher matcher = pattern.matcher(input);

		int index = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			sb.append(input.substring(index, matcher.start()));
			try {
				sb.append(evaluator.apply(matcher));
			} catch (Exception exp) {
				throw new MatchEvaluatorException(exp);
			}
			index = matcher.end();
		}
		sb.append(input.substring(index));

		return sb.toString();
	}

	public static String getReplaceStringOfFirstMatch(String input, int offset, Pattern pattern,
			Function<Matcher, String> evaluator) throws MatchEvaluatorException {
		Matcher matcher = pattern.matcher(input);

		if (!matcher.find(offset))
			return null;
		try {
			return evaluator.apply(matcher);
		} catch (Exception e) {
			throw new MatchEvaluatorException(e);
		}
	}

}
