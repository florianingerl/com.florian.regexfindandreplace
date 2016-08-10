package com.florian.regexfindandreplace.handlers.unittests;

import org.eclipse.swt.widgets.Shell;

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
