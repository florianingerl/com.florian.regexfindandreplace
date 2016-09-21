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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.florian.regexfindandreplace.MatchEvaluatorFromItsFunctionBodyGenerator;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.florian.examples.helloworld"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		Bundle bundle = Platform.getBundle("com.florian.regexfindandreplace");
		String bundleLocation = bundle.getLocation();
		System.out.println("bundleLocation=" + bundleLocation);

		logToFile("bundleLocation=" + bundleLocation);
		compileSomething();

		Injector injector = Guice.createInjector(new FindReplaceHandlerModule());
		ServiceLocator.setInjector(injector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void logToFile(String message) {
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream("C:/Users/Hermann/Desktop/Log.txt", true));
			pw.println(message);
			pw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void compileSomething() {
		try {
			File sourceFile = new File("MyMatchEvaluator.java");
			PrintWriter writer = new PrintWriter(sourceFile);

			writer.println("import com.ingerlflori.util.regex.*;");
			writer.println("import com.florian.IMatchEvaluator;");
			writer.println(
					"public class " + MatchEvaluatorFromItsFunctionBodyGenerator.getClassNameFromJavaFile(sourceFile)
							+ " implements IMatchEvaluator{");
			writer.println("@Override");
			writer.println("public String evaluateMatch(Matcher match) throws Exception{");
			writer.println("return \"Hello\";");
			writer.println("}");
			writer.println("}");
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
