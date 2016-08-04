package com.florian.regexfindandreplace.handlers.unittests;

import org.eclipse.jface.text.Assert;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.mockito.Mockito;

import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialogProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FindReplaceHandlerTestingModule extends AbstractModule {

	private IWorkbenchWindow workbenchWindow;
	private MockFindReplaceDialog lastFindReplaceDialog;
	
	public FindReplaceHandlerTestingModule(IWorkbenchWindow workbenchWindow)
	{
		this.workbenchWindow = workbenchWindow;
	}
	
	@Override
	protected void configure() 
	{
		bind(IFindReplaceDialogProvider.class).toInstance( new IFindReplaceDialogProvider()
		{

			@Override
			public IFindReplaceDialog getDialog(Shell shell) {
				// TODO Auto-generated method stub
				lastFindReplaceDialog =  new MockFindReplaceDialog( shell );
				return lastFindReplaceDialog;
			}
			
		});
	}
	
	public MockFindReplaceDialog getLastFindReplaceDialog() {
		return lastFindReplaceDialog;
	}

	@Provides
	public IWorkbenchWindow getFakeActiveWorkbenchWindow()
	{
		return workbenchWindow;
	}

	
}
