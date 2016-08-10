package com.florian.regexfindandreplace.handlers.unittests;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.widgets.Shell;
import org.mockito.Mockito;

import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialog;

public class MockFindReplaceDialog implements IFindReplaceDialog {

	private Shell shell;
	public IFindReplaceDialog mockDialog = Mockito.mock(IFindReplaceDialog.class);

	public MockFindReplaceDialog(Shell shell) {
		this.shell = shell;
		Mockito.when(mockDialog.open()).thenReturn(0);
		Mockito.when(mockDialog.close()).thenReturn(false);
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		mockDialog.create();
	}

	@Override
	public void updateTarget(IFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString) {
		// TODO Auto-generated method stub
		mockDialog.updateTarget(target, isTargetEditable, initializeFindString);
	}

	@Override
	public int open() {

		return mockDialog.open();
	}

	@Override
	public Shell getShell() {
		// TODO Auto-generated method stub
		return shell;
	}

	@Override
	public Shell getParentShell() {
		// TODO Auto-generated method stub
		return shell;
	}

	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		shell.dispose();
		return mockDialog.close();
	}

}
