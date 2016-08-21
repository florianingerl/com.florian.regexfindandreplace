package com.florian.regexfindandreplace;

public class MatchEvaluatorException extends Exception {

	public MatchEvaluatorException(String message)
	{
		super(message);
	}

	public MatchEvaluatorException(Exception exp) {
		super(exp);
	}
}
