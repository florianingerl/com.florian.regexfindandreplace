package com.florian.regexfindandreplace.handlers.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.dialogs.swt.uitests.FindReplaceTarget;
import com.florian.regexfindandreplace.handlers.OpenFindReplaceDialogE4Handler;
import com.google.inject.Guice;

public class OpenFindReplaceDialogE4HandlerTests {

	private Display display;
	private Shell shell;

	private boolean implementsIEditorExtension2 = false;
	private boolean isEditorInputModifiable = false;
	private IFindReplaceTarget initialTarget = null;

	private FakePartService partService = null;
	private FindReplaceHandlerTestingModule testingModule;
	private IWorkbenchPart workbenchPart;

	@Before
	public void before() {
		initialTarget = null;
		workbenchPart = null;
		implementsIEditorExtension2 = false;
		isEditorInputModifiable = false;
		partService = new FakePartService();
		display = new Display();
		shell = new Shell(display);
	}

	@After
	public void after() {
		shell.dispose();
		display.dispose();
	}

	public void initialize() {
		testingModule = new FindReplaceHandlerTestingModule();
		ServiceLocator.setInjector(Guice.createInjector(testingModule));

		IWorkbenchWindow workbenchWindow = mock(IWorkbenchWindow.class);
		if (!implementsIEditorExtension2) {
			workbenchPart = mock(IWorkbenchPart.class);
		} else {
			workbenchPart = mock(IWorkbenchPart.class,
					Mockito.withSettings().extraInterfaces(ITextEditorExtension2.class));
			when(((ITextEditorExtension2) workbenchPart).isEditorInputModifiable()).thenReturn(isEditorInputModifiable);
		}

		when(workbenchPart.getAdapter(IFindReplaceTarget.class)).thenReturn(initialTarget);
		IWorkbenchPartSite partSite = mock(IWorkbenchPartSite.class);

		when(partSite.getShell()).thenReturn(shell);
		when(partSite.getWorkbenchWindow()).thenReturn(workbenchWindow);
		when(workbenchPart.getSite()).thenReturn(partSite);

		IWorkbenchPartReference workbenchPartReference = mock(IWorkbenchPartReference.class);
		when(workbenchPartReference.getPart(true)).thenReturn(workbenchPart);
		partService.setActivePart(workbenchPartReference);
		when(workbenchWindow.getPartService()).thenReturn(partService);

	}

	@Test
	public void canExecute_whenTheFindReplaceTargetIsNull_ReturnsFalse() {
		initialTarget = null;
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertFalse(handler.canExecute(workbenchPart));
	}

	@Test
	public void canExecute_whenTheFindReplaceTargetIsntNull_ReturnsTrue() {
		initialTarget = mock(IFindReplaceTarget.class);
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertTrue(handler.canExecute(workbenchPart));
	}

	@Test
	public void canExecute_whenTheActiveWorkbenchPartImplementsITextEditorExtension2AndIsntModifiable_CallsUpdateTargetCorrectly() {
		initialTarget = mock(IFindReplaceTarget.class);
		implementsIEditorExtension2 = true;
		isEditorInputModifiable = false;
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertTrue(handler.canExecute(workbenchPart));

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog = testingModule.getLastFindReplaceDialog();
		verify(dialog.mockDialog).create();
		assertTrue(dialog.getShell() == shell);
		verify(dialog.mockDialog).updateTarget(initialTarget, false, true);
	}

	@Test
	public void execute_whenTheActiveWorkbenchPartImplementsITextEditorExtension2AndIsModifiable_CallsUpdateTargetCorrectly() {
		initialTarget = mock(IFindReplaceTarget.class);
		implementsIEditorExtension2 = true;
		isEditorInputModifiable = true;
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertTrue(handler.canExecute(workbenchPart));

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog = testingModule.getLastFindReplaceDialog();
		verify(dialog.mockDialog).create();
		assertTrue(dialog.getShell() == shell);
		verify(dialog.mockDialog).updateTarget(initialTarget, true, true);
	}

	@Test
	public void activePartChanges_whenTheNewActivePartIsEverythingPossible_theDialogsTargetGetsUpdated() {
		initialTarget = mock(IFindReplaceTarget.class);
		when(initialTarget.isEditable()).thenReturn(true);
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertTrue(handler.canExecute(workbenchPart));

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog = testingModule.getLastFindReplaceDialog();
		verify(dialog.mockDialog).create();
		assertTrue(dialog.getShell() == shell);
		verify(dialog.mockDialog).updateTarget(initialTarget, true, true);
		verify(dialog.mockDialog).open();

		IFindReplaceTarget target = mock(IFindReplaceTarget.class);
		when(target.isEditable()).thenReturn(false);
		openNewWorkbenchPart(target, false, false);

		verify(dialog.mockDialog).updateTarget(target, false, false);

		target = mock(FindReplaceTarget.class);
		when(target.isEditable()).thenReturn(true);
		openNewWorkbenchPart(target, false, false);
		verify(dialog.mockDialog).updateTarget(target, true, false);

		target = null;
		openNewWorkbenchPart(target, false, false);
		verify(dialog.mockDialog).updateTarget(null, false, false);

		target = mock(IFindReplaceTarget.class);
		openNewWorkbenchPart(target, true, true);
		verify(dialog.mockDialog).updateTarget(target, true, false);

		target = mock(IFindReplaceTarget.class);
		openNewWorkbenchPart(target, true, false);
		verify(dialog.mockDialog).updateTarget(target, false, false);

	}

	@Test
	public void execute_WhenADialogGotClosedBefore_OpensANewDialog() {
		initialTarget = mock(IFindReplaceTarget.class);
		when(initialTarget.isEditable()).thenReturn(true);
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertTrue(handler.canExecute(workbenchPart));

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog = testingModule.getLastFindReplaceDialog();

		verify(dialog.mockDialog).open();
		dialog.close();
		assertTrue(shell.isDisposed());

		shell = new Shell(display);
		when(workbenchPart.getSite().getShell()).thenReturn(shell);

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog2 = testingModule.getLastFindReplaceDialog();
		assertFalse(dialog == dialog2);

		verify(dialog2.mockDialog).open();

	}

	@Test
	public void execute_WhenADialogIsStillOpened_DoesntOpenANewDialog() {
		initialTarget = mock(IFindReplaceTarget.class);
		when(initialTarget.isEditable()).thenReturn(true);
		initialize();

		OpenFindReplaceDialogE4Handler handler = new OpenFindReplaceDialogE4Handler();
		assertTrue(handler.canExecute(workbenchPart));

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog = testingModule.getLastFindReplaceDialog();

		verify(dialog.mockDialog).open();

		handler.execute(shell, workbenchPart);
		MockFindReplaceDialog dialog2 = testingModule.getLastFindReplaceDialog();
		assertTrue(dialog == dialog2);

		verify(dialog2.mockDialog, Mockito.times(2)).open();

	}

	private void openNewWorkbenchPart(IFindReplaceTarget target, boolean implementsITextEditorExtension2,
			boolean isEditorInputModifiable) {
		IWorkbenchPartReference workbenchPartReference = mock(IWorkbenchPartReference.class);
		IWorkbenchPart workbenchPart = null;
		if (!implementsITextEditorExtension2)
			workbenchPart = mock(IWorkbenchPart.class);
		else {
			workbenchPart = mock(IWorkbenchPart.class,
					Mockito.withSettings().extraInterfaces(ITextEditorExtension2.class));
			when(((ITextEditorExtension2) workbenchPart).isEditorInputModifiable()).thenReturn(isEditorInputModifiable);
		}
		when(workbenchPartReference.getPart(true)).thenReturn(workbenchPart);

		when(workbenchPart.getAdapter(IFindReplaceTarget.class)).thenReturn(target);
		partService.setActivePart(workbenchPartReference);

	}

}
