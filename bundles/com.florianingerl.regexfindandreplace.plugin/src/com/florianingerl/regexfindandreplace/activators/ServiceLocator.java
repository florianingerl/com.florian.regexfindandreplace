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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import com.florianingerl.regexfindandreplace.dialogs.swt.IFindReplaceDialog;

public class ServiceLocator {

	public interface IActiveWorkbenchWindowProvider {
		public IWorkbenchWindow getActiveWorkbenchWindow();
	}

	private static IActiveWorkbenchWindowProvider activeWorkbenchWindowProvider = null;

	public static void setActiveWorkbenchWindowProvider(IActiveWorkbenchWindowProvider awwp) {
		activeWorkbenchWindowProvider = awwp;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return activeWorkbenchWindowProvider.getActiveWorkbenchWindow();
	}

	public interface IFindReplaceDialogProvider {
		public IFindReplaceDialog getDialog(Shell shell);
	}

	private static IFindReplaceDialogProvider findReplaceDialogProvider = null;

	public static void setFindReplaceDialogProvider(IFindReplaceDialogProvider frdp) {
		findReplaceDialogProvider = frdp;
	}

	public static IFindReplaceDialog getDialog(Shell shell) {
		return findReplaceDialogProvider.getDialog(shell);
	}

	public interface IEditorStatusLineProvider {
		public IEditorStatusLine getEditorStatusLine();
	}

	private static IEditorStatusLineProvider editorStatusLineProvider;

	public static void setEditorStatusLineProvider(IEditorStatusLineProvider eslp) {
		editorStatusLineProvider = eslp;
	}

	public static IEditorStatusLine getEditorStatusLine() {
		return editorStatusLineProvider.getEditorStatusLine();
	}

	public interface IDialogSettingsProvider {
		public IDialogSettings getDialogSettings();
	}

	private static IDialogSettingsProvider dialogSettingsProvider = null;

	public static void setDialogSettingsProvider(IDialogSettingsProvider dsp) {
		dialogSettingsProvider = dsp;
	}

	public static IDialogSettings getDialogSettings() {
		return dialogSettingsProvider.getDialogSettings();
	}
}
