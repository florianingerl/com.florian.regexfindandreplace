package com.florian.regexfindandreplace.handlers.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.handlers.FindAndReplaceHandler;
import com.google.inject.Guice;

public class FindAndReplaceHandlerTests {

	private Display display;
	private Shell shell;
	
	private IFindReplaceTarget initialTarget = null;

	private FakePartService partService = null;
	private FindReplaceHandlerTestingModule testingModule;

	@Before
	public void before() {
		initialTarget = null;
		partService = new FakePartService();
		display = new Display();
		shell = new Shell(display);
	}
	
	@After
	public void after()
	{
		display.dispose();
	}

	public void initialize() {
		IWorkbenchWindow workbenchWindow = mock(IWorkbenchWindow.class);

		testingModule = new FindReplaceHandlerTestingModule(workbenchWindow);
		ServiceLocator.setInjector(Guice.createInjector(testingModule) );

		IWorkbenchPart workbenchPart = mock(IWorkbenchPart.class);

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
	public void isEnabled_whenTheFindReplaceTargetCanPerformFind_ReturnsTrue() {
		initialTarget = mock(IFindReplaceTarget.class);
		when(initialTarget.canPerformFind()).thenReturn(true);
		initialize();

		FindAndReplaceHandler handler = new FindAndReplaceHandler();
		assertTrue(handler.isEnabled());
	}


	@Test
	public void activePartChanges_whenTheNewIFindReplaceTargetCantPerformAFind_theDialogsTargetGetsUpdated()
	{
		initialTarget = mock(IFindReplaceTarget.class);
		when(initialTarget.canPerformFind()).thenReturn(true);
		initialize();

		FindAndReplaceHandler handler = new FindAndReplaceHandler();
		assertTrue(handler.isEnabled());
		
		try {
			handler.execute(null);
		} catch (ExecutionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		IFindReplaceTarget target = mock(IFindReplaceTarget.class);
		when(target.canPerformFind()).thenReturn(false);
		openNewWorkbenchPart(target);
		
		
	}
	
	private void openNewWorkbenchPart(IFindReplaceTarget target) {
		IWorkbenchPartReference workbenchPartReference = mock(IWorkbenchPartReference.class);
		IWorkbenchPart workbenchPart = mock(IWorkbenchPart.class);
		when(workbenchPartReference.getPart(true)).thenReturn(workbenchPart);
		
		when(workbenchPart.getAdapter(IFindReplaceTarget.class)).thenReturn(target);
		partService.setActivePart(workbenchPartReference);

		
		verify(testingModule.getLastFindReplaceDialog().mockDialog ).updateTarget( target, false, false );
		
	}

	@Test
	public void activePartChanges_whenTheNewIFindReplaceTargetIsNull_theDialogsTargetGetsUpdated()
	{
		initialTarget = mock(IFindReplaceTarget.class);
		when(initialTarget.canPerformFind()).thenReturn(true);
		initialize();

		FindAndReplaceHandler handler = new FindAndReplaceHandler();
		assertTrue(handler.isEnabled());
		
		try {
			handler.execute(null);
		} catch (ExecutionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		openNewWorkbenchPart((IFindReplaceTarget) null );
	}
	

}
