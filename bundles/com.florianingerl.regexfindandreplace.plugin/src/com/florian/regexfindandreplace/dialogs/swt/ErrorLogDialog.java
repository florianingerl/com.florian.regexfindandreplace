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

package com.florian.regexfindandreplace.dialogs.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ErrorLogDialog extends Dialog {

	private StringBuilder sb = null;
	private Text errorLogText;

	protected ErrorLogDialog(Shell parentShell, StringBuilder sb) {
		super(parentShell);
		this.sb = sb;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());

		Button clearLogButton = new Button(container, SWT.PUSH);
		clearLogButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		clearLogButton.setText("Clear log");
		clearLogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sb.delete(0, sb.length());
				errorLogText.setText(sb.toString());
			}
		});

		errorLogText = new Text(container, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		errorLogText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		errorLogText.setEditable(false);
		errorLogText.setText(sb.toString());

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Error log");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 800);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

}
