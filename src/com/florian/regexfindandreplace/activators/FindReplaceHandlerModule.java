package com.florian.regexfindandreplace.activators;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.florian.regexfindandreplace.dialogs.swt.FindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialogProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FindReplaceHandlerModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(IFindReplaceDialogProvider.class).toInstance(
				new IFindReplaceDialogProvider() {
					
					@Override
					public IFindReplaceDialog getDialog(Shell shell) {
						// TODO Auto-generated method stub
						return new FindReplaceDialog(shell);
					}
				});
	}
	
	@Provides
	public IWorkbenchWindow getActiveWorkbenchWindow()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

}
