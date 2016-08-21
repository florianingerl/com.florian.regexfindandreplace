package com.florian.regexfindandreplace;

import java.util.regex.*;

public interface IMatchEvaluator {

	String evaluateMatch(MatchResult match) throws Exception;
}
