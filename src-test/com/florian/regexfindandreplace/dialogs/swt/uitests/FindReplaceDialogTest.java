package com.florian.regexfindandreplace.dialogs.swt.uitests;

import static org.junit.Assert.*;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.dialogs.EditorMessages;
import com.florian.regexfindandreplace.dialogs.swt.DialogSettingsConstants;
import com.florian.regexfindandreplace.dialogs.swt.FindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.ISWTBotFindConstant;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class FindReplaceDialogTest extends AbstractFindReplaceDialogTest {

	@Test
	public void replaceAll_WithAMatchEvaluator() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		openFindReplaceDialog();

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("Florian is 23 years old. His sister is 2 years older. She is 25 years old!");
		updateTarget(target, true, false);

		assertTrue(findReplaceDialogWrapper.getfIsRegExCheckBox().isChecked());

		SWTBotCheckBox useMatchEvaluatorCheckBox = findReplaceDialogWrapper.getfUseMatchEvaluatorCheckBox();
		assertTrue(useMatchEvaluatorCheckBox.isVisible());
		assertTrue(useMatchEvaluatorCheckBox.isChecked());

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		SWTBotLabel matchEvaluatorLabel = findReplaceDialogWrapper.getfMatchEvaluatorLabel();
		assertTrue(matchEvaluatorLabel.isVisible());
		assertTrue(findReplaceDialogWrapper.getfCaseCheckBox().isChecked());
		assertEquals("}},0);", findReplaceDialogWrapper.getfMatchEvaluatorFlagsLabel().getText());

		findField.setText("\\d{2}");
		assertEquals(
				"editorContent = RegexUtils.replaceNext/All(\ninput: editorContent,\nregex: \"\\d{2}\",\nnew IMatchEvaluator(){\n@Override\npublic String evaluateMatch(MatchResult match){",
				matchEvaluatorLabel.getText());

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+(i+1);");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("Florian is 24 years old. His sister is 2 years older. She is 26 years old!", target.getText());
		Mockito.verify(statusLine).setMessage(false, "2 matches replaced", null);

		assertEquals("2 matches replaced", findReplaceDialogWrapper.getfStatusLabel().getText());

	}

	@Test
	public void replaceAll_WithAMatchEvaluatorAndACaseInsensitiveSearch() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, false);

		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		openFindReplaceDialog();

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("The word Maintenance doesn't start with a capital letter");
		updateTarget(target, true, false);

		SWTBotCheckBox isRegExCheckBox = findReplaceDialogWrapper.getfIsRegExCheckBox();
		assertTrue(isRegExCheckBox.isChecked());

		SWTBotCheckBox useMatchEvaluatorCheckBox = findReplaceDialogWrapper.getfUseMatchEvaluatorCheckBox();
		assertTrue(useMatchEvaluatorCheckBox.isVisible());
		assertTrue(useMatchEvaluatorCheckBox.isChecked());

		SWTBotCheckBox isCaseSensitive = findReplaceDialogWrapper.getfCaseCheckBox();
		assertFalse(isCaseSensitive.isChecked());
		SWTBotLabel matchEvaluatorFlagsLabel = findReplaceDialogWrapper.getfMatchEvaluatorFlagsLabel();
		assertEquals("}},Pattern.CASE_INSENSITIVE);", matchEvaluatorFlagsLabel.getText());

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		SWTBotLabel matchEvaluatorLabel = findReplaceDialogWrapper.getfMatchEvaluatorLabel();
		assertTrue(matchEvaluatorLabel.isVisible());

		findField.setText("maintenance");
		assertEquals(
				"editorContent = RegexUtils.replaceNext/All(\ninput: editorContent,\nregex: \"maintenance\",\nnew IMatchEvaluator(){\n@Override\npublic String evaluateMatch(MatchResult match){",
				matchEvaluatorLabel.getText());

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("return \"maintenance\";");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("The word maintenance doesn't start with a capital letter", target.getText());
		Mockito.verify(statusLine).setMessage(false, "1 match replaced", null);

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("1 match replaced", statusLabel.getText());

	}

	@Test
	public void replaceAll_WithNormalRegexReplace() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.USE_MATCH_EVALUATOR, false);

		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		openFindReplaceDialog();
		
		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("Florian is 23 years old. His sister is 2 years older. She is 25 years old!");
		updateTarget(target, true, false);

		SWTBotCheckBox isRegExCheckBox = findReplaceDialogWrapper.getfIsRegExCheckBox();
		assertTrue(isRegExCheckBox.isChecked());

		SWTBotCheckBox useMatchEvaluatorCheckBox = findReplaceDialogWrapper.getfUseMatchEvaluatorCheckBox();
		assertTrue(useMatchEvaluatorCheckBox.isVisible());
		assertTrue(!useMatchEvaluatorCheckBox.isChecked());

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{2}");
		SWTBotCombo replaceField = findReplaceDialogWrapper.getfReplaceField();
		replaceField.setText("($0+1)");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("Florian is (23+1) years old. His sister is 2 years older. She is (25+1) years old!",
				target.getText());
		Mockito.verify(statusLine).setMessage(false, "2 matches replaced", null);

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("2 matches replaced", statusLabel.getText());
	}
	
	@Test
	public void replaceAll_WithAMatchEvaluatorWhereAnExceptionIsThrownOnTheSecondMatch_AnAppropriateErrorMessageIsDisplayAndTheMatchIsSelected()
	{
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);

		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		openFindReplaceDialog();
		
		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("A100BHermannC200D");
		updateTarget(target, true, false);
		
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(\\d{3}|Hermann)");
		
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		
		assertEquals("A101BHermannC200D", target.getText() );
		assertEquals("Hermann", target.getSelectionText() );
		
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("1 match replaced,\nNumberFormatException occured", statusLabel.getText() );
		
		Mockito.verify(statusLine ).setMessage(true, "NumberFormatException occured", null);
	}
	
	@Test
	public void replaceFindNext_WithAMatchEvaluatorInForwardDirection_MatchesAreCorrectlyReplacedAndCorrectTextIsSelected()
	{
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);

		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		openFindReplaceDialog();
		
		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("A100B200C");
		updateTarget(target, true, false);
		
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{3}");
		
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		
		assertEquals("100", target.getSelectionText() );
		
		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		replaceFindButton.click();
		
		assertEquals("A101B200C", target.getText() );
		assertEquals("200", target.getSelectionText() );
		
		SWTBotCheckBox wrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue( wrapCheckBox.isChecked() );
		
		replaceFindButton.click();
		assertEquals("A101B201C", target.getText() );

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("Wrapped search", statusLabel.getText() );
		
		Mockito.verify( statusLine ).setMessage(false, "Wrapped search", null);
		
		assertEquals("101", target.getSelectionText() );
		wrapCheckBox.deselect();
		
		target.setCaretPosition(4);
		findNextButton.click();
		
		replaceFindButton.click();
		assertEquals("A101B202C", target.getText() );
		assertEquals("202", target.getSelectionText() );
		assertEquals("String Not Found", statusLabel.getText() );
		
		Mockito.verify(statusLine).setMessage(false, "String Not Found", null);
		
	}
	
	@Test
	public void replaceFindNext_WithAMatchEvaluatorInBackwardDirection_MatchesAreCorrectlyReplacedAndCorrectTextIsSelected()
	{
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);

		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);

		openFindReplaceDialog();
		
		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("A100B200C");
		updateTarget(target, true, false);
		target.setCaretPosition(target.getText().length());
		
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{3}");
		
		SWTBotRadio fBackwardRadioButton = findReplaceDialogWrapper.getfBackwardRadioButton();
		fBackwardRadioButton.click();
		assertTrue( fBackwardRadioButton.isSelected() );
		
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		
		assertEquals("200", target.getSelectionText() );
		
		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		replaceFindButton.click();
		
		assertEquals("A100B201C", target.getText() );
		assertEquals("100", target.getSelectionText() );
		
		SWTBotCheckBox wrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue( wrapCheckBox.isChecked() );
		
		replaceFindButton.click();
		assertEquals("A101B201C", target.getText() );

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("Wrapped search", statusLabel.getText() );
		
		Mockito.verify( statusLine ).setMessage(false, "Wrapped search", null);
		
		assertEquals("201", target.getSelectionText() );
		wrapCheckBox.deselect();
		
		target.setCaretPosition(4);
		findNextButton.click();
		
		replaceFindButton.click();
		assertEquals("A102B201C", target.getText() );
		assertEquals("102", target.getSelectionText() );
		assertEquals("String Not Found", statusLabel.getText() );
		
		Mockito.verify(statusLine).setMessage(false, "String Not Found", null);
		
	}

}
