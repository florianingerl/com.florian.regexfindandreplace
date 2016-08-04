

/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.florian.regexfindandreplace.dialogs.swt;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.swing.JOptionPane;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.SWTUtil;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.osgi.framework.Bundle;

import com.florian.regexfindandreplace.IMatchEvaluator;
import com.florian.regexfindandreplace.MatchEvaluatorFromItsFunctionBodyGenerator;
import com.florian.regexfindandreplace.RegexUtils;
import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.dialogs.EditorMessages;
import com.sun.tools.javac.code.Attribute.Array;


/**
 * Find/Replace dialog. The dialog is opened on a particular
 * target but can be re-targeted. Internally used by the <code>FindReplaceAction</code>
 */
public class FindReplaceDialog extends Dialog implements IFindReplaceDialog {

	/**
	 * Updates the find replace dialog on activation changes.
	 */
	class ActivationListener extends ShellAdapter {
		/*
		 * @see ShellListener#shellActivated(ShellEvent)
		 */
		public void shellActivated(ShellEvent e) {
			fActiveShell= (Shell)e.widget;
			updateButtonState();

			if (fGiveFocusToFindField && getShell() == fActiveShell && okToUse(fFindField))
				fFindField.setFocus();

		}

		/*
		 * @see ShellListener#shellDeactivated(ShellEvent)
		 */
		public void shellDeactivated(ShellEvent e) {
			fGiveFocusToFindField= false;

			storeSettings();

			fGlobalRadioButton.setSelection(true);
			fSelectedRangeRadioButton.setSelection(false);
			fUseSelectedLines= false;

			if (fTarget != null && (fTarget instanceof IFindReplaceTargetExtension))
				((IFindReplaceTargetExtension) fTarget).setScope(null);

			fOldScope= null;

			fActiveShell= null;
			updateButtonState();
		}
	}

	/**
	 * Modify listener to update the search result in case of incremental search.
	 * @since 2.0
	 */
	private class FindModifyListener implements ModifyListener {

		// XXX: Workaround for Combo bug on Linux (see bug 404202 and bug 410603)
		private boolean fIgnoreNextEvent;
		private void ignoreNextEvent() {
			fIgnoreNextEvent= true;
		}

		/*
		 * @see ModifyListener#modifyText(ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			// XXX: Workaround for Combo bug on Linux (see bug 404202 and bug 410603)
			if (fIgnoreNextEvent) {
				fIgnoreNextEvent= false;
				return;
			}

			if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked()) {
				if (fFindField.getText().equals("") && fTarget != null) { //$NON-NLS-1$
					// empty selection at base location
					int offset= fIncrementalBaseLocation.x;

					if (isForwardSearch() && !fNeedsInitialFindBeforeReplace || !isForwardSearch() && fNeedsInitialFindBeforeReplace)
						offset= offset + fIncrementalBaseLocation.y;

					fNeedsInitialFindBeforeReplace= false;
					findAndSelect(offset, "", isForwardSearch(), isCaseSensitiveSearch(), isWholeWordSearch(), isRegExSearchAvailableAndChecked()); //$NON-NLS-1$
				} else {
					performSearch(false, false, isForwardSearch());
				}
			}

			updateButtonState(!isIncrementalSearch());
		}

		
	}

	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE= 5;
	
	private Point fIncrementalBaseLocation;
	private boolean fWrapInit, fCaseInit, fWholeWordInit, fForwardInit, fGlobalInit, fIncrementalInit;
	/**
	 * Tells whether an initial find operation is needed
	 * before the replace operation.
	 * @since 3.0
	 */
	private boolean fNeedsInitialFindBeforeReplace;
	/**
	 * Initial value for telling whether the search string is a regular expression.
	 * @since 3.0
	 */
	boolean fIsRegExInit;

	private List fFindHistory;
	private List fReplaceHistory;
	private IRegion fOldScope;

	private boolean fIsTargetEditable;
	private IFindReplaceTarget fTarget;
	private Shell fParentShell;
	private Shell fActiveShell;

	private final ActivationListener fActivationListener= new ActivationListener();
	private final FindModifyListener fFindModifyListener= new FindModifyListener();

	private Label fReplaceLabel, fStatusLabel;
	private Button fForwardRadioButton, fGlobalRadioButton, fSelectedRangeRadioButton;
	private Button fCaseCheckBox, fWrapCheckBox, fWholeWordCheckBox, fIncrementalCheckBox;

	/**
	 * Checkbox for selecting whether the search string is a regular expression.
	 * @since 3.0
	 */
	private Button fIsRegExCheckBox;

	private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton;
	private Combo fFindField, fReplaceField;
	private Composite fMatchEvaluatorPanel;
	private Button fUseMatchEvaluatorCheckBox;
	/**
	 * Find and replace command adapters.
	 * @since 3.3
	 */
	private ContentAssistCommandAdapter fContentAssistFindField, fContentAssistReplaceField;

	private Rectangle fDialogPositionInit;

	private IDialogSettings fDialogSettings;
	/**
	 * Tells whether the target supports regular expressions.
	 * <code>true</code> if the target supports regular expressions
	 * @since 3.0
	 */
	private boolean fIsTargetSupportingRegEx;
	/**
	 * Tells whether fUseSelectedLines radio is checked.
	 * @since 3.0
	 */
	private boolean fUseSelectedLines;
	/**
	 * <code>true</code> if the find field should receive focus the next time
	 * the dialog is activated, <code>false</code> otherwise.
	 * @since 3.0
	 */
	private boolean fGiveFocusToFindField= true;

	/**
	 * Holds the mnemonic/button pairs for all buttons.
	 * @since 3.7
	 */
	private HashMap fMnemonicButtonMap= new HashMap();

	private boolean fUseMatchEvaluator;
	private String fLastMatchEvaluatorCode;
	private IMatchEvaluator fMatchEvaluator;
	private Text fMatchEvaluatorField;

	private File fJavacCompiler;
	private Text fJavacCompilerField;

	private Label fMatchEvaluatorLabel;
	/**
	 * Creates a new dialog with the given shell as parent.
	 * @param parentShell the parent shell
	 */
	public FindReplaceDialog(Shell parentShell) {
		super(parentShell);

		fParentShell= null;
		fTarget= null;

		fDialogPositionInit= null;
		fFindHistory= new ArrayList(HISTORY_SIZE - 1);
		fReplaceHistory= new ArrayList(HISTORY_SIZE - 1);

		fWrapInit= false;
		fCaseInit= false;
		fIsRegExInit= false;
		fUseMatchEvaluator = false;
		fWholeWordInit= false;
		fIncrementalInit= false;
		fGlobalInit= true;
		fForwardInit= true;

		readConfiguration();

		setShellStyle(getShellStyle() ^ SWT.APPLICATION_MODAL | SWT.MODELESS);
		setBlockOnOpen(false);
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 * @since 3.4
	 */
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Returns this dialog's parent shell.
	 * @return the dialog's parent shell
	 */
	public Shell getParentShell() {
		return super.getParentShell();
	}


	/**
	 * Returns <code>true</code> if control can be used.
	 *
	 * @param control the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}

	/*
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {

		super.create();

		Shell shell= getShell();
		shell.addShellListener(fActivationListener);

		// set help context
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG);

		// fill in combo contents
		fFindField.removeModifyListener(fFindModifyListener);
		updateCombo(fFindField, fFindHistory);
		fFindField.addModifyListener(fFindModifyListener);
		updateCombo(fReplaceField, fReplaceHistory);

		// get find string
		initFindStringFromSelection();

		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);

		shell.setText(EditorMessages.FindReplace_title);
		// shell.setImage(null);
	}

	/**
	 * Create the button section of the find/replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the button section
	 */
	private Composite createButtonSection(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= -2; // this is intended
		panel.setLayout(layout);

		fFindNextButton= makeButton(panel, EditorMessages.FindReplace_FindNextButton_label, 102, true, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
					initIncrementalBaseLocation();

				fNeedsInitialFindBeforeReplace= false;
				performSearch((e.stateMask == SWT.SHIFT) ^ isForwardSearch());
				updateFindHistory();
			}
		});
		setGridData(fFindNextButton, SWT.FILL, true, SWT.FILL, false);

		fReplaceFindButton= makeButton(panel, EditorMessages.FindReplace_ReplaceFindButton_label, 103, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fNeedsInitialFindBeforeReplace)
					performSearch((e.stateMask == SWT.SHIFT) ^ isForwardSearch());
				if (performReplaceSelection())
					performSearch((e.stateMask == SWT.SHIFT) ^ isForwardSearch());
				updateFindAndReplaceHistory();
			}
		});
		setGridData(fReplaceFindButton, SWT.FILL, false, SWT.FILL, false);

		fReplaceSelectionButton= makeButton(panel, EditorMessages.FindReplace_ReplaceSelectionButton_label, 104, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fNeedsInitialFindBeforeReplace)
					performSearch();
				performReplaceSelection();
				updateButtonState();
				updateFindAndReplaceHistory();
			}
		});
		setGridData(fReplaceSelectionButton, SWT.FILL, false, SWT.FILL, false);

		fReplaceAllButton= makeButton(panel, EditorMessages.FindReplace_ReplaceAllButton_label, 105, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceAll();
				updateFindAndReplaceHistory();
			}
		});
		fReplaceAllButton.setData(ISWTBotFindConstant.FIND_KEY, "replaceAllButton");
		setGridData(fReplaceAllButton, SWT.FILL, true, SWT.FILL, false);

		// Make the all the buttons the same size as the Remove Selection button.
		fReplaceAllButton.setEnabled(isEditable());

		return panel;
	}

	/**
	 * Creates the options configuration section of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options configuration section
	 */
	private Composite createConfigPanel(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);

		Composite directionGroup= createDirectionGroup(panel);
		setGridData(directionGroup, SWT.FILL, true, SWT.FILL, false);

		Composite scopeGroup= createScopeGroup(panel);
		setGridData(scopeGroup, SWT.FILL, true, SWT.FILL, false);

		Composite optionsGroup= createOptionsGroup(panel);
		setGridData(optionsGroup, SWT.FILL, true, SWT.FILL, true);
		((GridData)optionsGroup.getLayoutData()).horizontalSpan= 2;

		return panel;
	}

	/*
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite inputPanel= createInputPanel(panel);
		setGridData(inputPanel, SWT.FILL, true, SWT.FILL, true);

		Composite configPanel= createConfigPanel(panel);
		setGridData(configPanel, SWT.FILL, true, SWT.TOP, true);

		Composite buttonPanelB= createButtonSection(panel);
		setGridData(buttonPanelB, SWT.RIGHT, true, SWT.BOTTOM, false);

		Composite statusBar= createStatusAndCloseButton(panel);
		setGridData(statusBar, SWT.FILL, true, SWT.BOTTOM, false);

		panel.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					if (!Util.isMac()) {
						Control controlWithFocus= getShell().getDisplay().getFocusControl();
						if (controlWithFocus != null && (controlWithFocus.getStyle() & SWT.PUSH) == SWT.PUSH)
							return;
					}
					Event event= new Event();
					event.type= SWT.Selection;
					event.stateMask= e.stateMask;
					fFindNextButton.notifyListeners(SWT.Selection, event);
					e.doit= false;
				}
				else if (e.detail == SWT.TRAVERSE_MNEMONIC) {
					Character mnemonic= new Character(Character.toLowerCase(e.character));
					if (fMnemonicButtonMap.containsKey(mnemonic)) {
						Button button= (Button)fMnemonicButtonMap.get(mnemonic);
						if ((fFindField.isFocusControl() || fReplaceField.isFocusControl() || (button.getStyle() & SWT.PUSH) != 0)
								&& button.isEnabled()) {
							Event event= new Event();
							event.type= SWT.Selection;
							event.stateMask= e.stateMask;
							if ((button.getStyle() & SWT.RADIO) != 0) {
								Composite buttonParent= button.getParent();
								if (buttonParent != null) {
									Control[] children= buttonParent.getChildren();
									for (int i= 0; i < children.length; i++)
										((Button)children[i]).setSelection(false);
								}
								button.setSelection(true);
							} else {
								button.setSelection(!button.getSelection());
							}
							button.notifyListeners(SWT.Selection, event);
							e.detail= SWT.TRAVERSE_NONE;
							e.doit= true;
						}
					}
				}
			}
		});

		updateButtonState();

		applyDialogFont(panel);

		return panel;
	}

	private void setContentAssistsEnablement(boolean enable) {
		//Not for now
		/*fContentAssistFindField.setEnabled(enable);
		fContentAssistReplaceField.setEnabled(enable);*/
	}

	/**
	 * Creates the direction defining part of the options defining section
	 * of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the direction defining part
	 */
	private Composite createDirectionGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(EditorMessages.FindReplace_Direction);
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SelectionListener selectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
					initIncrementalBaseLocation();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fForwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fForwardRadioButton.setText(EditorMessages.FindReplace_ForwardRadioButton_label);
		setGridData(fForwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fForwardRadioButton.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fForwardRadioButton);

		Button backwardRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		backwardRadioButton.setText(EditorMessages.FindReplace_BackwardRadioButton_label);
		setGridData(backwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		backwardRadioButton.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(backwardRadioButton);

		backwardRadioButton.setSelection(!fForwardInit);
		fForwardRadioButton.setSelection(fForwardInit);

		return panel;
	}

	/**
	 * Creates the scope defining part of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the scope defining part
	 * @since 2.0
	 */
	private Composite createScopeGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText(EditorMessages.FindReplace_Scope);
		GridLayout groupLayout= new GridLayout();
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fGlobalRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fGlobalRadioButton.setText(EditorMessages.FindReplace_GlobalRadioButton_label);
		setGridData(fGlobalRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fGlobalRadioButton.setSelection(fGlobalInit);
		fGlobalRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!fGlobalRadioButton.getSelection() || !fUseSelectedLines)
					return;
				fUseSelectedLines= false;
				useSelectedLines(false);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		storeButtonWithMnemonicInMap(fGlobalRadioButton);

		fSelectedRangeRadioButton= new Button(group, SWT.RADIO | SWT.LEFT);
		fSelectedRangeRadioButton.setText(EditorMessages.FindReplace_SelectedRangeRadioButton_label);
		setGridData(fSelectedRangeRadioButton, SWT.LEFT, false, SWT.CENTER, false);
		fSelectedRangeRadioButton.setSelection(!fGlobalInit);
		fUseSelectedLines= !fGlobalInit;
		fSelectedRangeRadioButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!fSelectedRangeRadioButton.getSelection() || fUseSelectedLines)
					return;
				fUseSelectedLines= true;
				useSelectedLines(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		storeButtonWithMnemonicInMap(fSelectedRangeRadioButton);

		return panel;
	}

	/**
	 * Tells the dialog to perform searches only in the scope given by the actually selected lines.
	 * @param selectedLines <code>true</code> if selected lines should be used
	 * @since 2.0
	 */
	private void useSelectedLines(boolean selectedLines) {
		if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
			initIncrementalBaseLocation();

		if (fTarget == null || !(fTarget instanceof IFindReplaceTargetExtension))
			return;

		IFindReplaceTargetExtension extensionTarget= (IFindReplaceTargetExtension) fTarget;

		if (selectedLines) {

			IRegion scope;
			if (fOldScope == null) {
				Point lineSelection= extensionTarget.getLineSelection();
				scope= new Region(lineSelection.x, lineSelection.y);
			} else {
				scope= fOldScope;
				fOldScope= null;
			}

			int offset= isForwardSearch()
				? scope.getOffset()
				: scope.getOffset() + scope.getLength();

			extensionTarget.setSelection(offset, 0);
			extensionTarget.setScope(scope);
		} else {
			fOldScope= extensionTarget.getScope();
			extensionTarget.setScope(null);
		}
	}

	/**
	 * Creates the panel where the user specifies the text to search
	 * for and the optional replacement text.
	 *
	 * @param parent the parent composite
	 * @return the input panel
	 */
	private Composite createInputPanel(Composite parent) {

		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		};

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		panel.setLayout(layout);

		Label findLabel= new Label(panel, SWT.LEFT);
		findLabel.setText(EditorMessages.FindReplace_Find_label);
		setGridData(findLabel, SWT.LEFT, false, SWT.CENTER, false);

		// Create the find content assist field
		ComboContentAdapter contentAdapter= new ComboContentAdapter();
		FindReplaceDocumentAdapterContentProposalProvider findProposer= new FindReplaceDocumentAdapterContentProposalProvider(true);
		fFindField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		fFindField.setData(ISWTBotFindConstant.FIND_KEY, "findField");
		//Not for now
		/* fContentAssistFindField= new ContentAssistCommandAdapter(
				fFindField,
				contentAdapter,
				findProposer,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS,
				new char[0],
				true); */
		setGridData(fFindField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(fFindField);
		fFindField.addModifyListener(fFindModifyListener);
		
		
		fReplaceLabel= new Label(panel, SWT.LEFT);
		fReplaceLabel.setText(EditorMessages.FindReplace_Replace_label);
		setGridData(fReplaceLabel, SWT.LEFT, false, SWT.CENTER, false);

		// Create the replace content assist field
		FindReplaceDocumentAdapterContentProposalProvider replaceProposer= new FindReplaceDocumentAdapterContentProposalProvider(false);
		fReplaceField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		//Not for now
		/*fContentAssistReplaceField= new ContentAssistCommandAdapter(
				fReplaceField,
				contentAdapter, replaceProposer,
				ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS,
				new char[0],
				true);*/
		setGridData(fReplaceField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(fReplaceField);
		fReplaceField.addModifyListener(listener);

		fMatchEvaluatorPanel = createMatchEvaluatorPanel(panel);
		setGridData(fMatchEvaluatorPanel, SWT.FILL, true, SWT.FILL, true);
		GridData gd = (GridData) fMatchEvaluatorPanel.getLayoutData();
		gd.horizontalSpan = 2;
		gd.exclude = true;
		fMatchEvaluatorPanel.setVisible(false);

		fUseMatchEvaluatorCheckBox = new Button(panel, SWT.CHECK);
		fUseMatchEvaluatorCheckBox.setData(ISWTBotFindConstant.FIND_KEY,"Use a match evaluator" );
		gd = new GridData();
		gd.horizontalSpan = 2;
		if(!fIsRegExInit)
		{
			gd.exclude = true;
			fUseMatchEvaluatorCheckBox.setVisible(false);
		}
	
		fUseMatchEvaluatorCheckBox.setLayoutData(gd);
		fUseMatchEvaluatorCheckBox.setSelection(fUseMatchEvaluator);
		fUseMatchEvaluatorCheckBox.setText("Use a match evaluator");
		fUseMatchEvaluatorCheckBox.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean b = fUseMatchEvaluatorCheckBox.getSelection();
				setMatchEvaluatorFieldVisible(b);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
		if(fIsRegExInit && fUseMatchEvaluator)
		{
			setMatchEvaluatorFieldVisible(true);
		}
		
		
		return panel;
	}
	
	private Composite createMatchEvaluatorPanel(Composite parent) {
		 	fMatchEvaluatorPanel = new Composite(parent, SWT.NULL);
			fMatchEvaluatorPanel.setLayout(new GridLayout(2, false));
			
			fMatchEvaluatorLabel = new Label(fMatchEvaluatorPanel, SWT.LEFT);
			fMatchEvaluatorLabel.setData(ISWTBotFindConstant.FIND_KEY, "matchEvaluatorLabel");
			//setTextOfMatchEvaluatorLabel();
			DataBindingContext dbc = new DataBindingContext();
		
			
			IObservableValue dbSource = WidgetProperties.text().observe(fFindField); // WidgetProperties.text(SWT.Modify).observe(fFindField);
			IObservableValue dbTarget = WidgetProperties.text().observe(fMatchEvaluatorLabel);
			 dbc.bindValue(dbTarget, dbSource,
				        null,
				        new UpdateValueStrategy(){{ setConverter( new IConverter(){

							@Override
							public Object getFromType() {
								return  String.class;
							}

							@Override
							public Object getToType() {
								// TODO Auto-generated method stub
								return String.class;
							}

							@Override
							public Object convert(Object fromObject) {
								// TODO Auto-generated method stub
								String findString = ( String ) fromObject ;
								return "editorContent = RegexUtils.replaceNext/All(\ninput: editorContent,\nregex: \"" + (findString != null ? findString : "") + "\",\nnew IMatchEvaluator(){\n@Override\npublic String evaluateMatch(MatchResult match){";
							}});}} );
			
			setGridData(fMatchEvaluatorLabel, SWT.LEFT, true, SWT.CENTER, false);
			Button helpButton = new Button(fMatchEvaluatorPanel, SWT.PUSH);
			GridData gd = new GridData();
			helpButton.setLayoutData(gd);
			gd.horizontalAlignment = SWT.RIGHT;
			gd.verticalAlignment = SWT.BOTTOM;
			helpButton.setText("Help");
			//helpButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_LCL_LINKTO_HELP ));
			helpButton.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					HelpDialog helpDialog = new HelpDialog(getShell() );
					helpDialog.setBlockOnOpen(true);
					helpDialog.open();
				}
			});
			
			fMatchEvaluatorField = new Text(fMatchEvaluatorPanel, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
			fMatchEvaluatorField.setData(ISWTBotFindConstant.FIND_KEY, "matchEvaluatorField");
			setGridData(fMatchEvaluatorField, SWT.FILL, true, SWT.FILL, true);
			((GridData) fMatchEvaluatorField.getLayoutData() ).horizontalSpan = 2;
			if(fLastMatchEvaluatorCode != null )
			{
				fMatchEvaluatorField.setText(fLastMatchEvaluatorCode);
			}
			Label closingBraceLabel = new Label(fMatchEvaluatorPanel, SWT.LEFT);
			closingBraceLabel.setText("}");
			setGridData(closingBraceLabel, SWT.LEFT, true, SWT.CENTER, false);
			((GridData) closingBraceLabel.getLayoutData()).horizontalSpan = 2;
			
			Composite browseJavac = createBrowseJavacPanel(fMatchEvaluatorPanel);
			setGridData(browseJavac, SWT.FILL, true, SWT.FILL, false);
			((GridData) browseJavac.getLayoutData() ).horizontalSpan = 2;
			
			return fMatchEvaluatorPanel;
	}

	private Composite createBrowseJavacPanel(Composite parent) {
		Composite browseJavac = new Composite(parent, SWT.NULL);
		
		browseJavac.setLayout(new GridLayout(3, false));
		Label javacLabel = new Label(browseJavac, SWT.LEFT);
		javacLabel.setText("Java compiler:");
		setGridData(javacLabel, SWT.LEFT, false, SWT.CENTER, false );
		fJavacCompilerField = new Text(browseJavac, SWT.BORDER );
		setGridData(fJavacCompilerField, SWT.FILL, true, SWT.CENTER, false);
		if( fJavacCompiler != null && fJavacCompiler.exists() && fJavacCompiler.getName().equals("javac.exe") )
		{
			fJavacCompilerField.setText(fJavacCompiler.getAbsolutePath());
		}
		fJavacCompilerField.setEditable(false);
		
		Button fBrowseJavacButton = new Button(browseJavac, SWT.PUSH);
		fBrowseJavacButton.setText("Browse...");
		fBrowseJavacButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell() , SWT.OPEN );
				dialog.setFilterExtensions(new String[]{"*.exe"});
				dialog.setText("Please select the appropriate java compiler (javac.exe)!");
				
				String result = dialog.open();
				if(result != null && result.endsWith("javac.exe"))
				{
					fJavacCompiler = new File(result);
					fJavacCompilerField.setText(fJavacCompiler.getAbsolutePath());
					storeSettings();
				}
			}
		});
		return browseJavac;
	}

	private void setMatchEvaluatorFieldVisible(boolean visible)
	{
		fReplaceLabel.setVisible(!visible);
		((GridData)fReplaceLabel.getLayoutData()).exclude = visible;
		fReplaceField.setVisible(!visible);
		((GridData)fReplaceField.getLayoutData()).exclude = visible;
		fMatchEvaluatorPanel.setVisible(visible);
		((GridData)fMatchEvaluatorPanel.getLayoutData()).exclude = !visible;
		forceRecalculationOfLayoutAndARepaint(fUseMatchEvaluatorCheckBox.getParent().getParent());
	}

	private void forceRecalculationOfLayoutAndARepaint(Composite composite) {
		composite.layout();
		composite.redraw();
		composite.update();
	}

	/**
	 * Creates the functional options part of the options defining
	 * section of the find replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the options group
	 */
	private Composite createOptionsGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_NONE);
		group.setText(EditorMessages.FindReplace_Options);
		GridLayout groupLayout= new GridLayout();
		groupLayout.numColumns= 2;
		groupLayout.makeColumnsEqualWidth= true;
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SelectionListener selectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				storeSettings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		fCaseCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fCaseCheckBox.setText(EditorMessages.FindReplace_CaseCheckBox_label);
		setGridData(fCaseCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fCaseCheckBox.setSelection(fCaseInit);
		fCaseCheckBox.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fCaseCheckBox);

		fWrapCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWrapCheckBox.setText(EditorMessages.FindReplace_WrapCheckBox_label);
		setGridData(fWrapCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fWrapCheckBox.setSelection(fWrapInit);
		fWrapCheckBox.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fWrapCheckBox);

		fWholeWordCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fWholeWordCheckBox.setText(EditorMessages.FindReplace_WholeWordCheckBox_label);
		setGridData(fWholeWordCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fWholeWordCheckBox.setSelection(fWholeWordInit);
		fWholeWordCheckBox.addSelectionListener(selectionListener);
		storeButtonWithMnemonicInMap(fWholeWordCheckBox);

		fIncrementalCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fIncrementalCheckBox.setText(EditorMessages.FindReplace_IncrementalCheckBox_label);
		setGridData(fIncrementalCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		fIncrementalCheckBox.setSelection(fIncrementalInit);
		fIncrementalCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (isIncrementalSearch() && !isRegExSearch())
					initIncrementalBaseLocation();

				storeSettings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		storeButtonWithMnemonicInMap(fIncrementalCheckBox);

		fIsRegExCheckBox= new Button(group, SWT.CHECK | SWT.LEFT);
		fIsRegExCheckBox.setData(ISWTBotFindConstant.FIND_KEY, "isRegExSearch" );
		fIsRegExCheckBox.setText(EditorMessages.FindReplace_RegExCheckbox_label);
		setGridData(fIsRegExCheckBox, SWT.LEFT, false, SWT.CENTER, false);
		((GridData)fIsRegExCheckBox.getLayoutData()).horizontalSpan= 2;
		fIsRegExCheckBox.setSelection(fIsRegExInit);
		fIsRegExCheckBox.addSelectionListener(new SelectionAdapter() {
			/*
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				boolean newState= fIsRegExCheckBox.getSelection();
				fIncrementalCheckBox.setEnabled(!newState);
				fUseMatchEvaluatorCheckBox.setVisible(newState);
				((GridData) fUseMatchEvaluatorCheckBox.getLayoutData()).exclude = !newState;
				setMatchEvaluatorFieldVisible(newState && fUseMatchEvaluatorCheckBox.getSelection());
				forceRecalculationOfLayoutAndARepaint(fUseMatchEvaluatorCheckBox.getParent().getParent());
				
				updateButtonState();
				storeSettings();
				setContentAssistsEnablement(newState);
			}
		});
		storeButtonWithMnemonicInMap(fIsRegExCheckBox);
		fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
		fWholeWordCheckBox.addSelectionListener(new SelectionAdapter() {
			/*
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateButtonState();
			}
		});
		fIncrementalCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
		return panel;
	}

	/**
	 * Creates the status and close section of the dialog.
	 *
	 * @param parent the parent composite
	 * @return the status and close button
	 */
	private Composite createStatusAndCloseButton(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		fStatusLabel= new Label(panel, SWT.LEFT);
		fStatusLabel.setData(ISWTBotFindConstant.FIND_KEY, "statusLabel");
		setGridData(fStatusLabel, SWT.FILL, true, SWT.CENTER, false);

		String label= EditorMessages.FindReplace_CloseButton_label;
		Button closeButton= createButton(panel, 101, label, false);
		setGridData(closeButton, SWT.RIGHT, false, SWT.BOTTOM, false);

		return panel;
	}

	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
	}



	// ------- action invocation ---------------------------------------

	/**
	 * Returns the position of the specified search string, or <code>-1</code> if the string can not
	 * be found when searching using the given options.
	 * 
	 * @param findString the string to search for
	 * @param startPosition the position at which to start the search
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive should the search be case sensitive
	 * @param wrapSearch should the search wrap to the start/end if arrived at the end/start
	 * @param wholeWord does the search string represent a complete word
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @param beep if <code>true</code> beeps when search does not find a match or needs to wrap
	 * @return the occurrence of the find string following the options or <code>-1</code> if nothing
	 *         found
	 * @since 3.0
	 */
	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch, boolean beep) {

		if (forwardSearch) {
			int index= findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch);
			if (index == -1) {

				if (beep && okToUse(getShell()))
					getShell().getDisplay().beep();

				if (wrapSearch) {
					statusMessage(EditorMessages.FindReplace_Status_wrapped_label);
					index= findAndSelect(-1, findString, true, caseSensitive, wholeWord, regExSearch);
				}
			}
			return index;
		}

		// backward
		int index= startPosition == 0 ? -1 : findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord, regExSearch);
		if (index == -1) {

			if (beep && okToUse(getShell()))
				getShell().getDisplay().beep();

			if (wrapSearch) {
				statusMessage(EditorMessages.FindReplace_Status_wrapped_label);
				index= findAndSelect(-1, findString, false, caseSensitive, wholeWord, regExSearch);
			}
		}
		return index;
	}

	/**
	 * Searches for a string starting at the given offset and using the specified search
	 * directives. If a string has been found it is selected and its start offset is
	 * returned.
	 *
	 * @param offset the offset at which searching starts
	 * @param findString the string which should be found
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
	 * @param wholeWord if <code>true</code> only occurrences are reported in which the findString stands as a word by itself
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the position of the specified string, or -1 if the string has not been found
	 * @since 3.0
	 */
	private int findAndSelect(int offset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3)fTarget).findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
		return fTarget.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord);
	}

	/**
	 * Replaces the selection with <code>replaceString</code>. If
	 * <code>regExReplace</code> is <code>true</code>,
	 * <code>replaceString</code> is a regex replace pattern which will get
	 * expanded if the underlying target supports it. Returns the region of the
	 * inserted text; note that the returned selection covers the expanded
	 * pattern in case of regex replace.
	 *
	 * @param replaceString the replace string (or a regex pattern)
	 * @param regExReplace <code>true</code> if <code>replaceString</code>
	 *        is a pattern
	 * @return the selection after replacing, i.e. the inserted text
	 * @since 3.0
	 */
	Point replaceSelection(String replaceString, boolean regExReplace) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			((IFindReplaceTargetExtension3)fTarget).replaceSelection(replaceString, regExReplace);
		else
			fTarget.replaceSelection(replaceString);

		return fTarget.getSelection();
	}

	/**
	 * Returns whether the specified search string can be found using the given options.
	 * 
	 * @param findString the string to search for
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive should the search be case sensitive
	 * @param wrapSearch should the search wrap to the start/end if arrived at the end/start
	 * @param wholeWord does the search string represent a complete word
	 * @param incremental is this an incremental search
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @param beep if <code>true</code> beeps when search does not find a match or needs to wrap
	 * @return <code>true</code> if the search string can be found using the given options
	 * 
	 * @since 3.0
	 */
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean incremental, boolean regExSearch, boolean beep) {

		if (fTarget == null)
			return false;

		Point r= null;
		if (incremental)
			r= fIncrementalBaseLocation;
		else
			r= fTarget.getSelection();

		int findReplacePosition= r.x;
		if (forwardSearch && !fNeedsInitialFindBeforeReplace || !forwardSearch && fNeedsInitialFindBeforeReplace)
			findReplacePosition += r.y;

		fNeedsInitialFindBeforeReplace= false;

		int index= findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch, wholeWord, regExSearch, beep);

		if (index == -1) {
			statusMessage(EditorMessages.FindReplace_Status_noMatch_label);
			return false;
		}

		if (forwardSearch && index >= findReplacePosition || !forwardSearch && index <= findReplacePosition)
			statusMessage(""); //$NON-NLS-1$

		return true;
	}

	/**
	 * Returns the dialog's boundaries.
	 * @return the dialog's boundaries
	 */
	private Rectangle getDialogBoundaries() {
		if (okToUse(getShell()))
			return getShell().getBounds();
		return fDialogPositionInit;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 * @since 3.5
	 */
	protected Point getInitialSize() {
		Point initialSize= super.getInitialSize();
		Point minSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (initialSize.x < minSize.x)
			return minSize;
		return initialSize;
	}

	/**
	 * Returns the dialog's history.
	 * @return the dialog's history
	 */
	private List getFindHistory() {
		return fFindHistory;
	}

	// ------- accessors ---------------------------------------

	/**
	 * Retrieves the string to search for from the appropriate text input field and returns it.
	 * @return the search string
	 */
	private String getFindString() {
		if (okToUse(fFindField)) {
			return fFindField.getText();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the dialog's replace history.
	 * @return the dialog's replace history
	 */
	private List getReplaceHistory() {
		return fReplaceHistory;
	}

	/**
	 * Retrieves the replacement string from the appropriate text input field and returns it.
	 * @return the replacement string
	 */
	private String getReplaceString() throws Exception {
		if( !useMatchEvaluator() )
		{
			if (okToUse(fReplaceField)) {
				return fReplaceField.getText();
			}
		}
		else
		{
			String toReplace = fTarget.getSelectionText();
			if(fMatchEvaluator == null || !fMatchEvaluatorField.getText().equals(fLastMatchEvaluatorCode))
			{
				if(fJavacCompiler == null || !fJavacCompiler.exists() || !fJavacCompiler.getName().equals("javac.exe"))
				{
					throw new Exception("You have to set the java compiler!");
				}
				MatchEvaluatorFromItsFunctionBodyGenerator generator = new MatchEvaluatorFromItsFunctionBodyGenerator(fJavacCompiler);
				fMatchEvaluator = generator.getMatchEvaluatorFromItsFunctionBody(fMatchEvaluatorField.getText());
				fLastMatchEvaluatorCode = fMatchEvaluatorField.getText();
			}
			return RegexUtils.replaceAll(toReplace, fFindField.getText(), fMatchEvaluator);
		}
		return ""; //$NON-NLS-1$
	}
	

	/**
	 * Returns the first line of the given selection.
	 *
	 * @param selection the selection
	 * @return the first line of the selection
	 */
	private String getFirstLine(String selection) {
		if (selection.length() > 0) {
			int[] info= TextUtilities.indexOf(TextUtilities.DELIMITERS, selection, 0);
			if (info[0] > 0)
				return selection.substring(0, info[0]);
			else if (info[0] == -1)
				return selection;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		handleDialogClose();
		return super.close();
	}

	/**
	 * Removes focus changed listener from browser and stores settings for re-open.
	 */
	private void handleDialogClose() {

		// remove listeners
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);
		}

		if (fParentShell != null) {
			fParentShell.removeShellListener(fActivationListener);
			fParentShell= null;
		}

		getShell().removeShellListener(fActivationListener);

		// store current settings in case of re-open
		storeSettings();

		if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		// prevent leaks
		fActiveShell= null;
		fTarget= null;

	}

	/**
	 * Writes the current selection to the dialog settings.
	 * @since 3.0
	 */
	private void writeSelection() {
		if (fTarget == null)
			return;

		IDialogSettings s= getDialogSettings();
		s.put("selection", fTarget.getSelectionText()); //$NON-NLS-1$
	}

	/**
	 * Stores the current state in the dialog settings.
	 * @since 2.0
	 */
	private void storeSettings() {
		fDialogPositionInit= getDialogBoundaries();
		fWrapInit= isWrapSearch();
		fWholeWordInit= isWholeWordSetting();
		fCaseInit= isCaseSensitiveSearch();
		fIsRegExInit= isRegExSearch();
		fUseMatchEvaluator = useMatchEvaluator();
		fIncrementalInit= isIncrementalSearch();
		fForwardInit= isForwardSearch();
		fLastMatchEvaluatorCode = fMatchEvaluatorField.getText();

		writeConfiguration();
	}

	private boolean useMatchEvaluator() {
		if(okToUse(fUseMatchEvaluatorCheckBox))
		{
			return fUseMatchEvaluatorCheckBox.getSelection();
		}
		return false;
	}

	/**
	 * Initializes the string to search for and the appropriate
	 * text in the Find field based on the selection found in the
	 * action's target.
	 */
	private void initFindStringFromSelection() {
		if (fTarget != null && okToUse(fFindField)) {
			String fullSelection= fTarget.getSelectionText();
			boolean isRegEx= isRegExSearchAvailableAndChecked();
			fFindField.removeModifyListener(fFindModifyListener);
			if (fullSelection.length() > 0) {
				String firstLine= getFirstLine(fullSelection);
				String pattern= isRegEx ? FindReplaceDocumentAdapter.escapeForRegExPattern(fullSelection) : firstLine;
				fFindField.setText(pattern);
				if (!firstLine.equals(fullSelection)) {
					// multiple lines selected
					useSelectedLines(true);
					fGlobalRadioButton.setSelection(false);
					fSelectedRangeRadioButton.setSelection(true);
					fUseSelectedLines= true;
				}
			} else {
				if ("".equals(fFindField.getText())) { //$NON-NLS-1$
					if (fFindHistory.size() > 0)
						fFindField.setText((String) fFindHistory.get(0));
					else
						fFindField.setText(""); //$NON-NLS-1$
				}
			}
			fFindField.setSelection(new Point(0, fFindField.getText().length()));
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Initializes the anchor used as starting point for incremental searching.
	 * @since 2.0
	 */
	private void initIncrementalBaseLocation() {
		if (fTarget != null && isIncrementalSearch() && !isRegExSearchAvailableAndChecked()) {
			fIncrementalBaseLocation= fTarget.getSelection();
		} else {
			fIncrementalBaseLocation= new Point(0, 0);
		}
	}

	// ------- history ---------------------------------------

	/**
	 * Retrieves and returns the option case sensitivity from the appropriate check box.
	 * @return <code>true</code> if case sensitive
	 */
	private boolean isCaseSensitiveSearch() {
		if (okToUse(fCaseCheckBox)) {
			return fCaseCheckBox.getSelection();
		}
		return fCaseInit;
	}

	/**
	 * Retrieves and returns the regEx option from the appropriate check box.
	 *
	 * @return <code>true</code> if case sensitive
	 * @since 3.0
	 */
	private boolean isRegExSearch() {
		if (okToUse(fIsRegExCheckBox)) {
			return fIsRegExCheckBox.getSelection();
		}
		return fIsRegExInit;
	}

	/**
	 * If the target supports regular expressions search retrieves and returns
	 * regEx option from appropriate check box.
	 *
	 * @return <code>true</code> if regEx is available and checked
	 * @since 3.0
	 */
	private boolean isRegExSearchAvailableAndChecked() {
		if (okToUse(fIsRegExCheckBox)) {
			return fIsTargetSupportingRegEx && fIsRegExCheckBox.getSelection();
		}
		return fIsRegExInit;
	}

	/**
	 * Retrieves and returns the option search direction from the appropriate check box.
	 * @return <code>true</code> if searching forward
	 */
	private boolean isForwardSearch() {
		if (okToUse(fForwardRadioButton)) {
			return fForwardRadioButton.getSelection();
		}
		return fForwardInit;
	}

	/**
	 * Retrieves and returns the option search whole words from the appropriate check box.
	 * @return <code>true</code> if searching for whole words
	 */
	private boolean isWholeWordSetting() {
		if (okToUse(fWholeWordCheckBox)) {
			return fWholeWordCheckBox.getSelection();
		}
		return fWholeWordInit;
	}

	/**
	 * Returns <code>true</code> if searching should be restricted to entire
	 * words, <code>false</code> if not. This is the case if the respective
	 * checkbox is turned on, regex is off, and the checkbox is enabled, i.e.
	 * the current find string is an entire word.
	 *
	 * @return <code>true</code> if the search is restricted to whole words
	 */
	private boolean isWholeWordSearch() {
		return isWholeWordSetting() && !isRegExSearchAvailableAndChecked() && (okToUse(fWholeWordCheckBox) ? fWholeWordCheckBox.isEnabled() : true);
	}

	/**
	 * Retrieves and returns the option wrap search from the appropriate check box.
	 * @return <code>true</code> if wrapping while searching
	 */
	private boolean isWrapSearch() {
		if (okToUse(fWrapCheckBox)) {
			return fWrapCheckBox.getSelection();
		}
		return fWrapInit;
	}

	/**
	 * Retrieves and returns the option incremental search from the appropriate check box.
	 * @return <code>true</code> if incremental search
	 * @since 2.0
	 */
	private boolean isIncrementalSearch() {
		if (okToUse(fIncrementalCheckBox)) {
			return fIncrementalCheckBox.getSelection();
		}
		return fIncrementalInit;
	}

	/**
	 * Creates a button.
	 * @param parent the parent control
	 * @param label the button label
	 * @param id the button id
	 * @param dfltButton is this button the default button
	 * @param listener a button pressed listener
	 * @return the new button
	 */
	private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
		Button button= createButton(parent, id, label, dfltButton);
		button.addSelectionListener(listener);
		storeButtonWithMnemonicInMap(button);
		return button;
	}

	/**
	 * Stores the button and its mnemonic in {@link #fMnemonicButtonMap}.
	 * 
	 * @param button button whose mnemonic has to be stored
	 * @since 3.7
	 */
	private void storeButtonWithMnemonicInMap(Button button) {
		char mnemonic= LegacyActionTools.extractMnemonic(button.getText());
		if (mnemonic != LegacyActionTools.MNEMONIC_NONE)
			fMnemonicButtonMap.put(new Character(Character.toLowerCase(mnemonic)), button);
	}

	/**
	 * Returns the status line manager of the active editor or <code>null</code> if there is no such editor.
	 * @return the status line manager of the active editor
	 */
	private IEditorStatusLine getStatusLineManager() {
		return  ServiceLocator.getInjector().getInstance(IEditorStatusLine.class );
	}

	/**
	 * Sets the given status message in the status line.
	 *
	 * @param error <code>true</code> if it is an error
	 * @param message the error message
	 */
	private void statusMessage(boolean error, String message) {
		fStatusLabel.setText(message);

		if (error)
			fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
		else
			fStatusLabel.setForeground(null);

		IEditorStatusLine statusLine= getStatusLineManager();
		if (statusLine != null)
			statusLine.setMessage(error, message, null);

		if (error)
			getShell().getDisplay().beep();
	}

	/**
	 * Sets the given error message in the status line.
	 * @param message the message
	 */
	private void statusError(String message) {
		statusMessage(true, message);
	}

	/**
	 * Sets the given message in the status line.
	 * @param message the message
	 */
	private void statusMessage(String message) {
		statusMessage(false, message);
	}

	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Indicate to the user the number of replacements
	 * that occur.
	 */
	private void performReplaceAll() {

		int replaceCount= 0;
		final String findString= getFindString();

		if (findString != null && findString.length() > 0) {

			class ReplaceAllRunnable implements Runnable {
				public int numberOfOccurrences;
				public void run() {
					numberOfOccurrences= replaceAll(findString, isForwardSearch(), isCaseSensitiveSearch(), isWholeWordSearch(), isRegExSearchAvailableAndChecked());	//$NON-NLS-1$
				}
			}

			try {
				ReplaceAllRunnable runnable= new ReplaceAllRunnable();
				BusyIndicator.showWhile(fActiveShell.getDisplay(), runnable);
				replaceCount= runnable.numberOfOccurrences;

				if (replaceCount != 0) {
					if (replaceCount == 1) { // not plural
						statusMessage(EditorMessages.FindReplace_Status_replacement_label);
					} else {
						String msg= EditorMessages.FindReplace_Status_replacements_label;
						msg= msg.replace("{0}", Integer.toString(replaceCount) );
						statusMessage(msg);
					}
				} else {
					statusMessage(EditorMessages.FindReplace_Status_noMatch_label);
				}
			} catch (PatternSyntaxException ex) {
				statusError(ex.getLocalizedMessage());
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
		writeSelection();
		updateButtonState();
	}

	/**
	 * Validates the state of the find/replace target.
	 * @return <code>true</code> if target can be changed, <code>false</code> otherwise
	 * @since 2.1
	 */
	private boolean validateTargetState() {

		if (fTarget instanceof IFindReplaceTargetExtension2) {
			IFindReplaceTargetExtension2 extension= (IFindReplaceTargetExtension2) fTarget;
			if (!extension.validateTargetState()) {
				statusError(EditorMessages.FindReplaceDialog_read_only);
				updateButtonState();
				return false;
			}
		}
		return isEditable();
	}

	/**
	 * Replaces the current selection of the target with the user's
	 * replace string.
	 *
	 * @return <code>true</code> if the operation was successful
	 */
	private boolean performReplaceSelection() {

		if (!validateTargetState())
			return false;
		
		String replaceString;
		try {
			replaceString = getReplaceString();
		} catch (Exception e) {
			statusError(e.getMessage());
			return false;
		}
		if (replaceString == null)
			replaceString= ""; //$NON-NLS-1$

		boolean replaced;
		try {
			replaceSelection(replaceString, isRegExSearchAvailableAndChecked() && !fUseMatchEvaluatorCheckBox.getSelection() );
			replaced= true;
			writeSelection();
		} catch (PatternSyntaxException ex) {
			statusError(ex.getLocalizedMessage());
			replaced= false;
		} catch (IllegalStateException ex) {
			replaced= false;
		}

		return replaced;
	}

	/**
	 * Locates the user's findString in the text of the target.
	 */
	private void performSearch() {
		performSearch(isForwardSearch());
	}

	/**
	 * Locates the user's findString in the text of the target.
	 *
	 * @param forwardSearch <code>true</code> if searching forwards, <code>false</code> otherwise
	 * @since 3.7
	 */
	private void performSearch(boolean forwardSearch) {
		performSearch(isIncrementalSearch() && !isRegExSearchAvailableAndChecked(), true, forwardSearch);
	}

	/**
	 * Locates the user's findString in the text of the target.
	 * 
	 * @param mustInitIncrementalBaseLocation <code>true</code> if base location must be initialized
	 * @param beep if <code>true</code> beeps when search does not find a match or needs to wrap
	 * @param forwardSearch the search direction
	 * @since 3.0
	 */
	private void performSearch(boolean mustInitIncrementalBaseLocation, boolean beep, boolean forwardSearch) {

		if (mustInitIncrementalBaseLocation)
			initIncrementalBaseLocation();

		String findString= getFindString();
		boolean somethingFound= false;

		if (findString != null && findString.length() > 0) {

			try {
					somethingFound= findNext(findString, forwardSearch, isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch(), isIncrementalSearch() && !isRegExSearchAvailableAndChecked(), isRegExSearchAvailableAndChecked(), beep);
			} catch (PatternSyntaxException ex) {
				statusError(ex.getLocalizedMessage());
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
		writeSelection();
		updateButtonState(!somethingFound);
	}

	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Returns the number of replacements
	 * that occur.
	 *
	 * @param findString the string to search for
	 * @param replaceString the replacement string
	 * @param forwardSearch	the search direction
	 * @param caseSensitive should the search be case sensitive
	 * @param wholeWord does the search string represent a complete word
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the number of occurrences
	 *
	 * @since 3.0
	 */
	private int replaceAll(String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {

		int replaceCount= 0;
		int findReplacePosition= 0;

		findReplacePosition= 0;
		forwardSearch= true;

		if (!validateTargetState())
			return replaceCount;

		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).setReplaceAllMode(true);

		try {
			int index= 0;
			while (index != -1) {
				index= findAndSelect(findReplacePosition, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
				if (index != -1) { // substring not contained from current position
					
					String replaceString;
					try {
						replaceString = getReplaceString();
					} catch (Exception e) {
						statusError(e.getMessage());
						return replaceCount;
					}
					replaceString = replaceString == null ? "" : replaceString;
					Point selection= replaceSelection(replaceString, regExSearch && !fUseMatchEvaluatorCheckBox.getSelection());
					replaceCount++;

					if (forwardSearch)
						findReplacePosition= selection.x + selection.y;
					else {
						findReplacePosition= selection.x - 1;
						if (findReplacePosition == -1)
							break;
					}
				}
			}
		} finally {
			if (fTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) fTarget).setReplaceAllMode(false);
		}

		return replaceCount;
	}

	// ------- UI creation ---------------------------------------

	/**
	 * Attaches the given layout specification to the <code>component</code>.
	 *
	 * @param component the component
	 * @param horizontalAlignment horizontal alignment
	 * @param grabExcessHorizontalSpace grab excess horizontal space
	 * @param verticalAlignment vertical alignment
	 * @param grabExcessVerticalSpace grab excess vertical space
	 */
	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd;
		if (component instanceof Button && (((Button)component).getStyle() & SWT.PUSH) != 0) {
			SWTUtil.setButtonDimensionHint((Button)component);
			gd= (GridData)component.getLayoutData();
		} else {
			gd= new GridData();
			component.setLayoutData(gd);
			gd.horizontalAlignment= horizontalAlignment;
			gd.grabExcessHorizontalSpace= grabExcessHorizontalSpace;
		}
		gd.verticalAlignment= verticalAlignment;
		gd.grabExcessVerticalSpace= grabExcessVerticalSpace;
	}

	/**
	 * Adds enough space in the control's layout data margin for the content assist
	 * decoration.
	 * @param control the control that needs a margin
	 * @since 3.3
	 */
	private void addDecorationMargin(Control control) {
		Object layoutData= control.getLayoutData();
		if (!(layoutData instanceof GridData))
			return;
		GridData gd= (GridData)layoutData;
		FieldDecoration dec= FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		gd.horizontalIndent= dec.getImage().getBounds().width;
	}

	/**
	 * Updates the enabled state of the buttons.
	 */
	private void updateButtonState() {
		updateButtonState(false);
	}

	/**
	 * Updates the enabled state of the buttons.
	 *
	 * @param disableReplace <code>true</code> if replace button must be disabled
	 * @since 3.0
	 */
	private void updateButtonState(boolean disableReplace) {
		if (okToUse(getShell()) && okToUse(fFindNextButton)) {

			boolean selection= false;
			if (fTarget != null)
				selection= fTarget.getSelectionText().length() > 0;

			boolean enable= fTarget != null && (fActiveShell == fParentShell || fActiveShell == getShell());
			String str= getFindString();
			boolean findString= str != null && str.length() > 0;

			fWholeWordCheckBox.setEnabled(isWord(str) && !isRegExSearchAvailableAndChecked());

			fFindNextButton.setEnabled(enable && findString);
			fReplaceSelectionButton.setEnabled(!disableReplace && enable && isEditable() && selection && (!fNeedsInitialFindBeforeReplace || !isRegExSearchAvailableAndChecked()));
			fReplaceFindButton.setEnabled(!disableReplace && enable && isEditable() && findString && selection && (!fNeedsInitialFindBeforeReplace || !isRegExSearchAvailableAndChecked()));
			fReplaceAllButton.setEnabled(enable && isEditable() && findString);
		}
	}

	/**
	 * Tests whether each character in the given string is a letter.
	 *
	 * @param str the string to check
	 * @return <code>true</code> if the given string is a word
	 * @since 3.0
	 */
	private boolean isWord(String str) {
		if (str == null || str.length() == 0)
			return false;

		for (int i= 0; i < str.length(); i++) {
			if (!Character.isJavaIdentifierPart(str.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Updates the given combo with the given content.
	 * @param combo combo to be updated
	 * @param content to be put into the combo
	 */
	private void updateCombo(Combo combo, List content) {
		combo.removeAll();
		for (int i= 0; i < content.size(); i++) {
			combo.add(content.get(i).toString());
		}
	}

	// ------- open / reopen ---------------------------------------

	/**
	 * Called after executed find/replace action to update the history.
	 */
	private void updateFindAndReplaceHistory() {
		updateFindHistory();
		if (okToUse(fReplaceField)) {
			updateHistory(fReplaceField, fReplaceHistory);
		}

	}

	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);

			// XXX: Workaround for Combo bug on Linux (see bug 404202 and bug 410603)
			if (Util.isLinux())
				fFindModifyListener.ignoreNextEvent();

			updateHistory(fFindField, fFindHistory);
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Updates the combo with the history.
	 * @param combo to be updated
	 * @param history to be put into the combo
	 */
	private void updateHistory(Combo combo, List history) {
		String findString= combo.getText();
		int index= history.indexOf(findString);
		if (index != 0) {
			if (index != -1) {
				history.remove(index);
			}
			history.add(0, findString);
			Point selection= combo.getSelection();
			updateCombo(combo, history);
			combo.setText(findString);
			combo.setSelection(selection);
		}
	}

	/**
	 * Returns whether the target is editable.
	 * @return <code>true</code> if target is editable
	 */
	private boolean isEditable() {
		boolean isEditable= (fTarget == null ? false : fTarget.isEditable());
		return fIsTargetEditable && isEditable;
	}

	/**
	 * Updates this dialog because of a different target.
	 * @param target the new target
	 * @param isTargetEditable <code>true</code> if the new target can be modified
	 * @param initializeFindString <code>true</code> if the find string of this dialog should be initialized based on the viewer's selection
	 * @since 2.0
	 */
	public void updateTarget(IFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString) {

		fIsTargetEditable= isTargetEditable;
		fNeedsInitialFindBeforeReplace= true;

		if (target != fTarget) {
			if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) fTarget).endSession();

			fTarget= target;
			if (fTarget != null)
				fIsTargetSupportingRegEx= fTarget instanceof IFindReplaceTargetExtension3;

			if (fTarget instanceof IFindReplaceTargetExtension) {
				((IFindReplaceTargetExtension) fTarget).beginSession();

				fGlobalInit= true;
				fGlobalRadioButton.setSelection(fGlobalInit);
				fSelectedRangeRadioButton.setSelection(!fGlobalInit);
				fUseSelectedLines= !fGlobalInit;
			}
		}

		if (okToUse(fIsRegExCheckBox))
			fIsRegExCheckBox.setEnabled(fIsTargetSupportingRegEx);

		if (okToUse(fWholeWordCheckBox))
			fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());

		if (okToUse(fIncrementalCheckBox))
			fIncrementalCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());

		if (okToUse(fReplaceLabel)) {
			fReplaceLabel.setEnabled(isEditable());
			fReplaceField.setEnabled(isEditable());
			if (initializeFindString) {
				initFindStringFromSelection();
				fGiveFocusToFindField= true;
			}
			initIncrementalBaseLocation();
			updateButtonState();
		}

		setContentAssistsEnablement(isRegExSearchAvailableAndChecked());
	}

	/**
	 * Sets the parent shell of this dialog to be the given shell.
	 *
	 * @param shell the new parent shell
	 */
	public void setParentShell(Shell shell) {
		if (shell != fParentShell) {

			if (fParentShell != null)
				fParentShell.removeShellListener(fActivationListener);

			fParentShell= shell;
			fParentShell.addShellListener(fActivationListener);
		}

		fActiveShell= shell;
	}


	//--------------- configuration handling --------------

	/**
	 * Returns the dialog settings object used to share state
	 * between several find/replace dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings= ServiceLocator.getInjector().getInstance(IDialogSettings.class );
		fDialogSettings= settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 * @since 3.2
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings= ServiceLocator.getInjector().getInstance(IDialogSettings.class );
		IDialogSettings section= settings.getSection(sectionName);
		if (section == null)
			section= settings.addNewSection(sectionName);
		return section;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
	 * @since 3.2
	 */
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
	}

	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();

		fWrapInit= s.get("wrap") == null || s.getBoolean("wrap"); //$NON-NLS-1$ //$NON-NLS-2$
		fCaseInit= s.getBoolean("casesensitive"); //$NON-NLS-1$
		fWholeWordInit= s.getBoolean("wholeword"); //$NON-NLS-1$
		fIncrementalInit= s.getBoolean("incremental"); //$NON-NLS-1$
		fIsRegExInit= s.getBoolean(DialogSettingsConstants.IS_REG_EX); //$NON-NLS-1$
		fLastMatchEvaluatorCode = s.get("lastMatchEvaluatorCode");
		fUseMatchEvaluator = s.getBoolean(DialogSettingsConstants.USE_MATCH_EVALUATOR);
		String temp = s.get(DialogSettingsConstants.PATH_TO_JAVAC);
		if( temp != null && temp.endsWith("javac.exe") )
		{
			fJavacCompiler = new File(temp);
			if(!fJavacCompiler.exists()) fJavacCompiler = null;
		}
		String[] findHistory= s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistory != null) {
			List history= getFindHistory();
			history.clear();
			for (int i= 0; i < findHistory.length; i++)
				history.add(findHistory[i]);
		}

		String[] replaceHistory= s.getArray("replacehistory"); //$NON-NLS-1$
		if (replaceHistory != null) {
			List history= getReplaceHistory();
			history.clear();
			for (int i= 0; i < replaceHistory.length; i++)
				history.add(replaceHistory[i]);
		}
	}

	/**
	 * Stores its current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();

		s.put("wrap", fWrapInit); //$NON-NLS-1$
		s.put("casesensitive", fCaseInit); //$NON-NLS-1$
		s.put("wholeword", fWholeWordInit); //$NON-NLS-1$
		s.put("incremental", fIncrementalInit); //$NON-NLS-1$
		s.put( DialogSettingsConstants.IS_REG_EX, fIsRegExInit); //$NON-NLS-1$
		s.put("lastMatchEvaluatorCode", fLastMatchEvaluatorCode);
		s.put(DialogSettingsConstants.USE_MATCH_EVALUATOR, fUseMatchEvaluator);
		if( fJavacCompiler != null && fJavacCompiler.exists() && fJavacCompiler.getName().equals("javac.exe"))
		{
			s.put(DialogSettingsConstants.PATH_TO_JAVAC, fJavacCompiler.getAbsolutePath());
		}
		List history= getFindHistory();
		String findString= getFindString();
		if (findString.length() > 0)
			history.add(0, findString);
		writeHistory(history, s, "findhistory"); //$NON-NLS-1$

		history= getReplaceHistory();
		String replaceString;
		try {
			replaceString = getReplaceString();
			if (replaceString.length() > 0)
				history.add(0, replaceString);
		} catch (Exception e) {
			
		}
		
		writeHistory(history, s, "replacehistory"); //$NON-NLS-1$
	}

	/**
	 * Writes the given history into the given dialog store.
	 *
	 * @param history the history
	 * @param settings the dialog settings
	 * @param sectionName the section name
	 * @since 3.2
	 */
	private void writeHistory(List history, IDialogSettings settings, String sectionName) {
		int itemCount= history.size();
		Set distinctItems= new HashSet(itemCount);
		for (int i= 0; i < itemCount; i++) {
			String item= (String)history.get(i);
			if (distinctItems.contains(item)) {
				history.remove(i--);
				itemCount--;
			} else {
				distinctItems.add(item);
			}
		}

		while (history.size() > 8)
			history.remove(8);

		String[] names= new String[history.size()];
		history.toArray(names);
		settings.put(sectionName, names);

	}
	
}
