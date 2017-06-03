package com.florianingerl.regexfindandreplace.manualtests;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.mockito.Mockito;

import com.florianingerl.regexfindandreplace.activators.ServiceLocator;
import com.florianingerl.regexfindandreplace.dialogs.swt.FindReplaceDialog;
import com.florianingerl.regexfindandreplace.dialogs.swt.uitests.FindReplaceDialogTestingModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MainWindow {

	protected Shell shell;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		IDialogSettings dialogSettings = new DialogSettings("root");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				MainWindow window = new MainWindow();
				window.open();
			}
		});

	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(800, 400);
		shell.setText("Find/Replace with regular expressions and match evaluators");
		shell.setLayout(new GridLayout());

		TextViewer textViewer = new TextViewer(shell, SWT.BORDER);
		textViewer.setDocument(new Document());
		textViewer.getDocument().set("Florian is 23 years old.\nHis sister is 2 years older.\nShe is 25 years old.");
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		textViewer.getTextWidget().setLayoutData(gd);

		Button openDialogButton = new Button(shell, SWT.PUSH);
		openDialogButton.setText("Find/Replace");
		openDialogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FindReplaceDialog dialog = new FindReplaceDialog(true, shell);
				dialog.create();
				dialog.updateTarget(textViewer.getFindReplaceTarget(), true, true);
				dialog.open();
			}
		});

	}

}
