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

package com.florianingerl.regexfindandreplace.activators;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import com.florianingerl.regexfindandreplace.dialogs.swt.FindReplaceDialog;
import com.florianingerl.regexfindandreplace.dialogs.swt.IFindReplaceDialog;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.florianingerl.regexfindandreplace.plugin"; //$NON-NLS-1$

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
		System.out.println("Activator was called!");

		configureDependencies();
	}

	public void configureDependencies() {
		ServiceLocator.setActiveWorkbenchWindowProvider(new ServiceLocator.IActiveWorkbenchWindowProvider() {
			@Override
			public IWorkbenchWindow getActiveWorkbenchWindow() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			}
		});

		ServiceLocator.setFindReplaceDialogProvider(new ServiceLocator.IFindReplaceDialogProvider() {
			@Override
			public IFindReplaceDialog getDialog(Shell shell) {
				// TODO Auto-generated method stub
				return new FindReplaceDialog(false, shell);
			}
		});

		ServiceLocator.setDialogSettingsProvider(new ServiceLocator.IDialogSettingsProvider() {
			@Override
			public IDialogSettings getDialogSettings() {
				return TextEditorPlugin.getDefault().getDialogSettings();
			}
		});

		ServiceLocator.setEditorStatusLineProvider(new ServiceLocator.IEditorStatusLineProvider() {
			@Override
			public IEditorStatusLine getEditorStatusLine() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return null;

				IWorkbenchPage page = window.getActivePage();
				if (page == null)
					return null;

				IEditorPart editor = page.getActiveEditor();
				if (editor == null)
					return null;

				return (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
			}
		});
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

}
