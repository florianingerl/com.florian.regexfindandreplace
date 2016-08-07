package com.florian.regexfindandreplace;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	public static String getStackTraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}
}
