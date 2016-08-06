package com.florian.regexfindandreplace.activators;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.IEditorStatusLine;

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
						return new FindReplaceDialog(false, shell);
					}
				});
	}
	
	@Provides
	public IWorkbenchWindow getActiveWorkbenchWindow()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	
	@Provides
	public IDialogSettings getDialogSettings()
	{
		return TextEditorPlugin.getDefault().getDialogSettings();
	}
	
	@Provides
	public IEditorStatusLine getEditorStatusLine()
	{
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page= window.getActivePage();
		if (page == null)
			return null;

		IEditorPart editor= page.getActiveEditor();
		if (editor == null)
			return null;

		return (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
	}

}
