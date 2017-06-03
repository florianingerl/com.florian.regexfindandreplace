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

package com.florian.regexfindandreplace.activators;

import com.google.inject.Injector;

public class ServiceLocator {

	private static Injector injector;

	public static Injector getInjector() {
		return injector;
	}

	public static void setInjector(Injector injector) {
		ServiceLocator.injector = injector;
	}
}
