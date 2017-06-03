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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import com.florianingerl.regexfindandreplace.dialogs.swt.FindReplaceDialog;
import com.florianingerl.regexfindandreplace.dialogs.swt.IFindReplaceDialog;
import com.florianingerl.regexfindandreplace.dialogs.swt.IFindReplaceDialogProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FindReplaceHandlerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IFindReplaceDialogProvider.class).toInstance(new IFindReplaceDialogProvider() {

			@Override
			public IFindReplaceDialog getDialog(Shell shell) {
				// TODO Auto-generated method stub
				return new FindReplaceDialog(false, shell);
			}
		});
	}

	@Provides
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	@Provides
	public IDialogSettings getDialogSettings() {
		return TextEditorPlugin.getDefault().getDialogSettings();
	}

	@Provides
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

}
