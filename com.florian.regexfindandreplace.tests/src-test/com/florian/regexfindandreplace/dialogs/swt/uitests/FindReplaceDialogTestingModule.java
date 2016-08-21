package com.florian.regexfindandreplace.dialogs.swt.uitests;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IEditorStatusLine;

public class FindReplaceDialogTestingModule extends AbstractModule {

	private IDialogSettings dialogSettings;
	private IEditorStatusLine editorStatusLine;

	public FindReplaceDialogTestingModule(IDialogSettings dialogSettings, IEditorStatusLine editorStatusLine )
	{
		this.dialogSettings = dialogSettings;
		this.editorStatusLine = editorStatusLine;
	}
	
	@Override
	protected void configure() {
		// TODO Auto-generated method stub
		
	}
	
	@Provides
	public IDialogSettings getDialogSettings()
	{
		return dialogSettings;
	}
	
	@Provides
	public IEditorStatusLine getEditorStatusLine()
	{
		return editorStatusLine;
	}

}
