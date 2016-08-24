package com.florian.regexfindandreplace.dialogs.swt.uitests;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import com.florian.regexfindandreplace.dialogs.swt.FindReplaceDialog;

public class FindReplaceDialogWrapper {
	private FindReplaceDialog dialog = null;

	public FindReplaceDialogWrapper(FindReplaceDialog dialog) {
		this.dialog = dialog;
	}

	private SWTBotLabel fStatusLabel;

	public SWTBotLabel getfStatusLabel() {
		if (fStatusLabel == null)
			fStatusLabel = new SWTBotLabel(dialog.getfStatusLabel());
		return fStatusLabel;
	}

	private SWTBotRadio fForwardRadioButton;

	public SWTBotRadio getfForwardRadioButton() {
		if (fForwardRadioButton == null)
			fForwardRadioButton = new SWTBotRadio(dialog.getfForwardRadioButton());
		return fForwardRadioButton;
	}

	private SWTBotRadio fGlobalRadioButton;

	public SWTBotRadio getfGlobalRadioButton() {
		if (fGlobalRadioButton == null)
			fGlobalRadioButton = new SWTBotRadio(dialog.getfGlobalRadioButton());
		return fGlobalRadioButton;
	}

	private SWTBotRadio fSelectedRangeRadioButton;

	public SWTBotRadio getfSelectedRangeRadioButton() {
		if (fSelectedRangeRadioButton == null)
			fSelectedRangeRadioButton = new SWTBotRadio(dialog.getfSelectedRangeRadioButton());
		return fSelectedRangeRadioButton;
	}

	private SWTBotCheckBox fCaseCheckBox;

	public SWTBotCheckBox getfCaseCheckBox() {
		if (fCaseCheckBox == null)
			fCaseCheckBox = new SWTBotCheckBox(dialog.getfCaseCheckBox());
		return fCaseCheckBox;
	}

	private SWTBotCheckBox fWrapCheckBox;

	public SWTBotCheckBox getfWrapCheckBox() {
		if (fWrapCheckBox == null)
			fWrapCheckBox = new SWTBotCheckBox(dialog.getfWrapCheckBox());
		return fWrapCheckBox;
	}

	private SWTBotCheckBox fWholeWordCheckBox;

	public SWTBotCheckBox getfWholeWordCheckBox() {
		if (fWholeWordCheckBox == null)
			fWholeWordCheckBox = new SWTBotCheckBox(dialog.getfWholeWordCheckBox());
		return fWholeWordCheckBox;
	}

	private SWTBotCheckBox fIncrementalCheckBox;

	public SWTBotCheckBox getfIncrementalCheckBox() {
		if (fIncrementalCheckBox == null)
			fIncrementalCheckBox = new SWTBotCheckBox(dialog.getfIncrementalCheckBox());
		return fIncrementalCheckBox;
	}

	private SWTBotCheckBox fIsRegExCheckBox;

	public SWTBotCheckBox getfIsRegExCheckBox() {
		if (fIsRegExCheckBox == null)
			fIsRegExCheckBox = new SWTBotCheckBox(dialog.getfIsRegExCheckBox());
		return fIsRegExCheckBox;
	}

	private SWTBotButton fReplaceSelectionButton;

	public SWTBotButton getfReplaceSelectionButton() {
		if (fReplaceSelectionButton == null)
			fReplaceSelectionButton = new SWTBotButton(dialog.getfReplaceSelectionButton());
		return fReplaceSelectionButton;
	}

	private SWTBotButton fReplaceFindButton;

	public SWTBotButton getfReplaceFindButton() {
		if (fReplaceFindButton == null)
			fReplaceFindButton = new SWTBotButton(dialog.getfReplaceFindButton());
		return fReplaceFindButton;
	}

	private SWTBotButton fFindNextButton;

	public SWTBotButton getfFindNextButton() {
		if (fFindNextButton == null)
			fFindNextButton = new SWTBotButton(dialog.getfFindNextButton());
		return fFindNextButton;
	}

	private SWTBotButton fReplaceAllButton;

	public SWTBotButton getfReplaceAllButton() {
		if (fReplaceAllButton == null)
			fReplaceAllButton = new SWTBotButton(dialog.getfReplaceAllButton());
		return fReplaceAllButton;
	}

	private SWTBotCombo fFindField;

	public SWTBotCombo getfFindField() {
		if (fFindField == null)
			fFindField = new SWTBotCombo(dialog.getfFindField());
		return fFindField;
	}

	private SWTBotCombo fReplaceField;

	public SWTBotCombo getfReplaceField() {
		if (fReplaceField == null)
			fReplaceField = new SWTBotCombo(dialog.getfReplaceField());
		return fReplaceField;
	}

	private SWTBotCheckBox fUseMatchEvaluatorCheckBox;

	public SWTBotCheckBox getfUseMatchEvaluatorCheckBox() {
		if (fUseMatchEvaluatorCheckBox == null)
			fUseMatchEvaluatorCheckBox = new SWTBotCheckBox(dialog.getfUseMatchEvaluatorCheckBox());
		return fUseMatchEvaluatorCheckBox;
	}

	private SWTBotText fMatchEvaluatorField;

	public SWTBotText getfMatchEvaluatorField() {
		if (fMatchEvaluatorField == null)
			fMatchEvaluatorField = new SWTBotText(dialog.getfMatchEvaluatorField());
		return fMatchEvaluatorField;
	}

	private SWTBotLabel fMatchEvaluatorLabel;

	public SWTBotLabel getfMatchEvaluatorLabel() {
		if (fMatchEvaluatorLabel == null)
			fMatchEvaluatorLabel = new SWTBotLabel(dialog.getfMatchEvaluatorLabel());
		return fMatchEvaluatorLabel;
	}

	private SWTBotLabel fMatchEvaluatorFlagsLabel;

	public SWTBotLabel getfMatchEvaluatorFlagsLabel() {
		if (fMatchEvaluatorFlagsLabel == null)
			fMatchEvaluatorFlagsLabel = new SWTBotLabel(dialog.getfMatchEvaluatorFlagsLabel());
		return fMatchEvaluatorFlagsLabel;
	}

	private SWTBotButton fCloseButton;

	public SWTBotButton getfCloseButton() {
		if (fCloseButton == null)
			fCloseButton = new SWTBotButton(dialog.getfCloseButton());
		return fCloseButton;
	}

	private SWTBotRadio fbackwardRadioButton;

	public SWTBotRadio getfBackwardRadioButton() {
		if (fbackwardRadioButton == null)
			fbackwardRadioButton = new SWTBotRadio(dialog.getfBackwardRadioButton());
		return fbackwardRadioButton;
	}

	private SWTBotText fJavacCompilerField;

	public SWTBotText getfJavacCompilerField() {
		if (fJavacCompilerField == null)
			fJavacCompilerField = new SWTBotText(dialog.getfJavacCompilerField());
		return fJavacCompilerField;
	}

}
