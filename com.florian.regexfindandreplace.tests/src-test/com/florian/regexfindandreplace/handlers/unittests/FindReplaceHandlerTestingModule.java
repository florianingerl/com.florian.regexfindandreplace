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

package com.florian.regexfindandreplace.handlers.unittests;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

import com.florian.regexfindandreplace.IClassPathProvider;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialogProvider;
import com.google.inject.AbstractModule;

public class FindReplaceHandlerTestingModule extends AbstractModule {

	private MockFindReplaceDialog lastFindReplaceDialog;

	public FindReplaceHandlerTestingModule() {

	}

	@Override
	protected void configure() {
		bind(IFindReplaceDialogProvider.class).toInstance(new IFindReplaceDialogProvider() {

			@Override
			public IFindReplaceDialog getDialog(Shell shell) {
				// TODO Auto-generated method stub
				lastFindReplaceDialog = new MockFindReplaceDialog(shell);
				return lastFindReplaceDialog;
			}

		});

	}

	public MockFindReplaceDialog getLastFindReplaceDialog() {
		return lastFindReplaceDialog;
	}

}
