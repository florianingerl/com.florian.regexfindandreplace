
package com.florian.regexfindandreplace.handlers;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditorExtension2;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.IFindReplaceDialogProvider;

public class OpenFindReplaceDialogE4Handler {

	/**
	 * Represents the "global" find/replace dialog. It tracks the active part
	 * and retargets the find/replace dialog accordingly. The find/replace
	 * target is retrieved from the active part using
	 * <code>getAdapter(IFindReplaceTarget.class)</code>.
	 * <p>
	 * The stub has the same life cycle as the find/replace dialog.
	 * </p>
	 * <p>
	 * If no IWorkbenchPart is available a Shell must be provided In this case
	 * the IFindReplaceTarget will never change.
	 * </p>
	 */
	static class FindReplaceDialogStub implements IPartListener2, IPageChangedListener, DisposeListener {

		/** The workbench part */
		private IWorkbenchPart fPart;
		/** The previous workbench part */
		private IWorkbenchPart fPreviousPart;
		/** The previous find/replace target */
		private IFindReplaceTarget fPreviousTarget;

		/** The workbench window */
		private IWorkbenchWindow fWindow;
		/** The find/replace dialog */
		private IFindReplaceDialog fDialog;

		/**
		 * Creates a new find/replace dialog accessor anchored at the given part
		 * site.
		 *
		 * @param site
		 *            the part site
		 */
		public FindReplaceDialogStub(IWorkbenchPartSite site) {
			this(site.getShell());
			fWindow = site.getWorkbenchWindow();
			IPartService service = fWindow.getPartService();
			service.addPartListener(this);
			partActivated(service.getActivePart());
		}

		/**
		 * Creates a new find/replace dialog accessor anchored at the given
		 * shell.
		 *
		 * @param shell
		 *            the shell if no site is used
		 * @since 3.3
		 */
		public FindReplaceDialogStub(Shell shell) {
			logger.debug("Trying to construct the FindReplaceDialog!");
			fDialog = ((IFindReplaceDialogProvider) ServiceLocator.getInjector()
					.getInstance(IFindReplaceDialogProvider.class)).getDialog(shell);
			fDialog.create();
			fDialog.getShell().addDisposeListener(this);
			logger.debug("Successfully constructed the FindReplaceDialog!");
		}

		/**
		 * Returns the find/replace dialog.
		 * 
		 * @return the find/replace dialog
		 */
		public IFindReplaceDialog getDialog() {
			return fDialog;
		}

		private void partActivated(IWorkbenchPart part) {
			IFindReplaceTarget target = part == null ? null
					: (IFindReplaceTarget) part.getAdapter(IFindReplaceTarget.class);
			fPreviousPart = fPart;
			fPart = target == null ? null : part;

			if (fPreviousTarget != target) {
				fPreviousTarget = target;
				if (fDialog != null) {
					boolean isEditable = false;
					if (fPart instanceof ITextEditorExtension2) {
						ITextEditorExtension2 extension = (ITextEditorExtension2) fPart;
						isEditable = extension.isEditorInputModifiable();
					} else if (target != null)
						isEditable = target.isEditable();
					fDialog.updateTarget(target, isEditable, false);
				}
			}
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.
		 * IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference partRef) {
			partActivated(partRef.getPart(true));
		}

		/*
		 * @see org.eclipse.jface.dialogs.IPageChangedListener#pageChanged(org.
		 * eclipse.jface.dialogs.PageChangedEvent)
		 * 
		 * @since 3.5
		 */
		public void pageChanged(PageChangedEvent event) {
			if (event.getSource() instanceof IWorkbenchPart)
				partActivated((IWorkbenchPart) event.getSource());
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.
		 * IWorkbenchPartReference)
		 */
		public void partClosed(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(true);
			if (part == fPreviousPart) {
				fPreviousPart = null;
				fPreviousTarget = null;
			}

			if (part == fPart)
				partActivated((IWorkbenchPart) null);
		}

		/*
		 * @see DisposeListener#widgetDisposed(DisposeEvent)
		 */
		@Override
		public void widgetDisposed(DisposeEvent event) {

			if (fgFindReplaceDialogStub == this)
				fgFindReplaceDialogStub = null;

			if (fgFindReplaceDialogStubShell == this)
				fgFindReplaceDialogStubShell = null;

			if (fWindow != null) {
				fWindow.getPartService().removePartListener(this);
				fWindow = null;
			}
			fDialog = null;
			fPart = null;
			fPreviousPart = null;
			fPreviousTarget = null;
		}

		/*
		 * @see
		 * org.eclipse.ui.IPartListener2#partOpened(IWorkbenchPartReference)
		 */
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(
		 * IWorkbenchPartReference)
		 */
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(
		 * IWorkbenchPartReference)
		 */
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.
		 * IWorkbenchPartReference)
		 * 
		 * @since 3.5
		 */
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.
		 * IWorkbenchPartReference)
		 * 
		 * @since 3.5
		 */
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		/*
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.
		 * IWorkbenchPartReference)
		 * 
		 * @since 3.5
		 */
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		/**
		 * Checks if the dialogs shell is the same as the given
		 * <code>shell</code> and if not clears the stub and closes the dialog.
		 * 
		 * @param shell
		 *            the shell check
		 * @since 3.3
		 */
		public void checkShell(Shell shell) {
			if (fDialog != null && shell != fDialog.getParentShell()) {
				if (fgFindReplaceDialogStub == this)
					fgFindReplaceDialogStub = null;

				if (fgFindReplaceDialogStubShell == this)
					fgFindReplaceDialogStubShell = null;

				fDialog.close();
			}
		}

	}

	private static Logger logger = Logger.getLogger(FindAndReplaceHandler.class);
	/**
	 * Listener for disabling the dialog on shell close.
	 * <p>
	 * This stub is shared amongst <code>IWorkbenchPart</code>s.
	 * </p>
	 */
	private static FindReplaceDialogStub fgFindReplaceDialogStub;

	/**
	 * Listener for disabling the dialog on shell close.
	 * <p>
	 * This stub is shared amongst <code>Shell</code>s.
	 * </p>
	 * 
	 * @since 3.3
	 */
	private static FindReplaceDialogStub fgFindReplaceDialogStubShell;

	private IFindReplaceTarget fTarget;

	public OpenFindReplaceDialogE4Handler() {

	}

	@Execute
	public void execute(Shell shell, IWorkbenchPart workbenchPart) {
		logger.debug("The handler was called!");

		if (!canExecute(workbenchPart))
			return;

		final IFindReplaceDialog dialog;
		final boolean isEditable;

		if (fgFindReplaceDialogStub != null) {
			fgFindReplaceDialogStub.checkShell(shell);
		}
		if (fgFindReplaceDialogStub == null) {
			IWorkbenchPartSite site = workbenchPart.getSite();
			fgFindReplaceDialogStub = new FindReplaceDialogStub(site);
		}

		if (workbenchPart instanceof ITextEditorExtension2)
			isEditable = ((ITextEditorExtension2) workbenchPart).isEditorInputModifiable();
		else
			isEditable = fTarget.isEditable();

		dialog = fgFindReplaceDialogStub.getDialog();

		logger.debug("Try to open the dialog!");
		dialog.updateTarget(fTarget, isEditable, true);
		dialog.open();
		logger.debug("The dialog got opened!");
	}

	@CanExecute
	public boolean canExecute(IWorkbenchPart workbenchPart) {
		logger.debug("Can execute got called!");
		fTarget = (IFindReplaceTarget) workbenchPart.getAdapter(IFindReplaceTarget.class);
		return fTarget != null;
	}

}