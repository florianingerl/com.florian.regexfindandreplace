package com.florian.regexfindandreplace.dialogs.swt;

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
