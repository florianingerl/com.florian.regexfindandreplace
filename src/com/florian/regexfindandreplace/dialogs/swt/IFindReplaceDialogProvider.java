package com.florian.regexfindandreplace.dialogs.swt;

import org.eclipse.swt.widgets.Shell;

public interface IFindReplaceDialogProvider 
{
	IFindReplaceDialog getDialog(Shell shell);
}
