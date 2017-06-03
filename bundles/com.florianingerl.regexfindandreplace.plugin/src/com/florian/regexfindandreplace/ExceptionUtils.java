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

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	public static String getStackTraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
