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

package com.florianingerl.regexfindandreplace.dialogs.swt;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.widgets.Shell;

public interface IFindReplaceDialog {

	void create();

	void updateTarget(IFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString);

	int open();

	Shell getShell();

	Shell getParentShell();

	boolean close();
}
