package com.florian.regexfindandreplace.dialogs.swt.uitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.dialogs.swt.DialogSettingsConstants;
import com.florian.regexfindandreplace.dialogs.swt.FindReplaceDialog;
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
		updateTarget("Florian is 23 years old. His sister is 2 years older. She is 25 years old!", false);

		assertTrue(findReplaceDialogWrapper.getfIsRegExCheckBox().isChecked());

		SWTBotCheckBox useMatchEvaluatorCheckBox = findReplaceDialogWrapper.getfUseMatchEvaluatorCheckBox();
		assertTrue(useMatchEvaluatorCheckBox.isVisible());
		assertTrue(useMatchEvaluatorCheckBox.isChecked());

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		SWTBotLabel matchEvaluatorLabel = findReplaceDialogWrapper.getfMatchEvaluatorLabel();
		assertTrue(matchEvaluatorLabel.isVisible());
		assertTrue(findReplaceDialogWrapper.getfCaseCheckBox().isChecked());
		assertEquals("}},flags:0);", findReplaceDialogWrapper.getfMatchEvaluatorFlagsLabel().getText());

		findField.setText("\\d{2}");
		assertEquals(
				"editorContent = RegexUtils.replaceNext/All(\ninput: editorContent,\nregex: \"\\d{2}\",\nnew IMatchEvaluator(){\n@Override\npublic String evaluateMatch(MatchResult match){",
				matchEvaluatorLabel.getText());

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+(i+1);");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("Florian is 24 years old. His sister is 2 years older. She is 26 years old!",
				textViewer.getDocument().get());
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
		updateTarget("The word Maintenance doesn't start with a capital letter", false);
		SWTBotCheckBox isRegExCheckBox = findReplaceDialogWrapper.getfIsRegExCheckBox();
		assertTrue(isRegExCheckBox.isChecked());
		SWTBotCheckBox useMatchEvaluatorCheckBox = findReplaceDialogWrapper.getfUseMatchEvaluatorCheckBox();
		assertTrue(useMatchEvaluatorCheckBox.isVisible());
		assertTrue(useMatchEvaluatorCheckBox.isChecked());
		SWTBotCheckBox isCaseSensitive = findReplaceDialogWrapper.getfCaseCheckBox();
		assertFalse(isCaseSensitive.isChecked());
		SWTBotLabel matchEvaluatorFlagsLabel = findReplaceDialogWrapper.getfMatchEvaluatorFlagsLabel();
		assertEquals("}},flags:Pattern.CASE_INSENSITIVE);", matchEvaluatorFlagsLabel.getText());
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
		assertEquals("The word maintenance doesn't start with a capital letter", textViewer.getDocument().get());
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
		updateTarget("Florian is 23 years old. His sister is 2 years older. She is 25 years old!", false);
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
				textViewer.getDocument().get());
		Mockito.verify(statusLine).setMessage(false, "2 matches replaced", null);
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("2 matches replaced", statusLabel.getText());
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorThatCantBeCompiled_AnAppropriateExceptionIsDisplayed() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("Hello World", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1)");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("World");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		assertEquals("Hello World", textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("0 matches replaced,\nCouldNotCompileJavaSourceCodeException occured", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(true,
				"0 matches replaced,\nCouldNotCompileJavaSourceCodeException occured", null);
		System.out.println(findReplaceDialog.getErrorLog().toString());
		assertTrue(findReplaceDialog.getErrorLog().toString().contains("';' expected"));
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorWhereAnExceptionIsThrownOnTheSecondMatch_AnAppropriateErrorMessageIsDisplayAndTheMatchIsSelected() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A100BHermannC200D", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(\\d{3}|Hermann)");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		assertEquals("A101BHermannC200D", textViewer.getDocument().get());
		assertEquals("Hermann", textWidget.getSelection());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("1 match replaced,\nMatchEvaluatorException occured", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(true, "1 match replaced,\nMatchEvaluatorException occured", null);
		System.out.println(findReplaceDialog.getErrorLog().toString());
		assertTrue(findReplaceDialog.getErrorLog().toString().contains("NumberFormatException"));
	}

	@Test
	public void replace_WithAMatchEvaluatorWhereAnExceptionIsThrownByTheMatchEvaluator_AnAppropriateErrorMessageIsDisplayed() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A100BHermannC200D", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("Hermann");
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		SWTBotButton replaceSelectionButton = findReplaceDialogWrapper.getfReplaceSelectionButton();
		replaceSelectionButton.click();
		assertEquals("Hermann", textWidget.getSelection());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("MatchEvaluatorException occured", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(true, "MatchEvaluatorException occured", null);
		assertTrue(findReplaceDialog.getErrorLog().toString().contains("NumberFormatException"));
	}

	@Test
	public void replaceFindNext_WithAMatchEvaluatorWhereAnExceptionIsThrownByTheMatchEvaluator_AnAppropriateErrorMessageIsDisplayed() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A100BHermannC200D", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("Hermann");
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		replaceFindButton.click();
		assertEquals("Hermann", textWidget.getSelection());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("MatchEvaluatorException occured", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(true, "MatchEvaluatorException occured", null);
		assertTrue(findReplaceDialog.getErrorLog().toString().contains("NumberFormatException"));
	}

	@Test
	public void replaceFindNext_WithAMatchEvaluatorInForwardDirection_MatchesAreCorrectlyReplacedAndCorrectTextIsSelected() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A100B200C", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{3}");
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		assertEquals("100", textWidget.getSelection());
		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		replaceFindButton.click();
		assertEquals("A101B200C", textViewer.getDocument().get());
		assertEquals("200", textWidget.getSelection());
		SWTBotCheckBox wrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue(wrapCheckBox.isChecked());
		replaceFindButton.click();
		assertEquals("A101B201C", textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("Wrapped search", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(false, "Wrapped search", null);
		assertEquals("101", textWidget.getSelection());
		wrapCheckBox.deselect();
		textWidget.selectRange(0, 5, 0);
		findNextButton.click();
		replaceFindButton.click();
		assertEquals("A101B202C", textViewer.getDocument().get());
		assertEquals("202", textWidget.getSelection());
		assertEquals("String Not Found", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(false, "String Not Found", null);
	}

	@Test
	public void replaceFindNext_WithAMatchEvaluatorInBackwardDirection_MatchesAreCorrectlyReplacedAndCorrectTextIsSelected() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.CASE_SENSITIVE, true);
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A100B200C", false);
		textWidget.selectRange(0, textWidget.getText().length(), 0);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{3}");
		SWTBotRadio fBackwardRadioButton = findReplaceDialogWrapper.getfBackwardRadioButton();
		fBackwardRadioButton.click();
		assertTrue(fBackwardRadioButton.isSelected());
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		assertEquals("200", textWidget.getSelection());
		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		assertTrue(replaceFindButton.isEnabled());
		replaceFindButton.click();
		assertEquals("A100B201C", textViewer.getDocument().get());
		assertEquals("100", textWidget.getSelection());
		SWTBotCheckBox wrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue(wrapCheckBox.isChecked());
		assertTrue(replaceFindButton.isEnabled());
		replaceFindButton.click();
		assertEquals("A101B201C", textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("Wrapped search", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(false, "Wrapped search", null);
		assertEquals("201", textWidget.getSelection());
		wrapCheckBox.deselect();
		// If its target.setCaretPosition(5), the test passes through
		textWidget.selectRange(0, 4, 0);
		findNextButton.click();
		assertEquals("101", textWidget.getSelection());
		replaceFindButton.click();
		assertEquals("A102B201C", textViewer.getDocument().get());
		assertEquals("102", textWidget.getSelection());
		assertEquals("String Not Found", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(false, "String Not Found", null);
	}

	@Ignore
	@Test(timeout = 5000)
	public void replaceAll_WithAMatchEvaluatorButWhenTheMatchIsJustAPosition_ShouldReplaceThisPosition() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("In Deutschland leben 80000000 Menschen!Auf der Welt leben 7000000000 Menschen", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("return \".\";");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(?<=\\d)(?=(\\d{3})+(?!\\d))");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		assertEquals("In Deutschland leben 80.000.000 Menschen!Auf der Welt leben 7.000.000.000 Menschen",
				textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("5 matches replaced", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(true, "5 matches replaced", null);
	}

	@Ignore
	@Test
	public void replaceAll_WithANormalRegexReplaceButWhenTheMatchIsJustAPosition_ShouldReplaceThisPosition() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		newSection.put(DialogSettingsConstants.USE_MATCH_EVALUATOR, false);
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("In Deutschland leben 80000000 Menschen!Auf der Welt leben 7000000000 Menschen", false);
		SWTBotCombo replaceField = findReplaceDialogWrapper.getfReplaceField();
		replaceField.setText(".");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(?<=\\d)(?=(\\d{3})+(?!\\d))");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		assertEquals("In Deutschland leben 80.000.000 Menschen!Auf der Welt leben 7.000.000.000 Menschen",
				textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("5 matches replaced", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(true, "5 matches replaced", null);
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorAndALookAheadThatCapturesAGroup_ItRecognizesTheGroup() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("17_{hex}+10_{bin}=27", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText(
				"if(match.group(1).equals(\"hex\")) return Integer.toHexString( Integer.parseInt(match.group() ) ); else if(match.group(1).equals(\"bin\")) return Integer.toBinaryString( Integer.parseInt(match.group())); return null; ");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{2}(?=_\\{(hex|bin)\\})");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		assertEquals("11_{hex}+1010_{bin}=27", textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("2 matches replaced", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(false, "2 matches replaced", null);
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorAndALookbehindThatCapturesAGroup_TheCapturedGroupIsRecognized() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("x17=17 and 010=10", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText(
				"if(match.group(1).equals(\"x\")) return Integer.toHexString( Integer.parseInt(match.group() ) ); else if(match.group(1).equals(\"0\")) return Integer.toOctalString( Integer.parseInt(match.group())); return null; ");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(?<=(x|0))[1-9]\\d*");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();
		assertEquals("x11=17 and 012=10", textViewer.getDocument().get());
		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("2 matches replaced", statusLabel.getText());
		Mockito.verify(statusLine).setMessage(false, "2 matches replaced", null);
	}

	@Test
	public void replace_WithAMatchEvaluator_ItJustWorks() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A100B200D30", false);
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i*10);");
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d+");
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		assertEquals("100", textWidget.getSelection());
		SWTBotButton replaceButton = findReplaceDialogWrapper.getfReplaceSelectionButton();
		replaceButton.click();
		assertEquals("A1000B200D30", textViewer.getDocument().get());
		assertEquals("1000", textWidget.getSelection());
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i*100);");
		findNextButton.click();
		assertEquals("200", textWidget.getSelection());
		replaceButton.click();
		assertEquals("A1000B20000D30", textViewer.getDocument().get());
		assertEquals("20000", textWidget.getSelection());
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i*1000);");
		findNextButton.click();
		assertEquals("30", textWidget.getSelection());
		replaceButton.click();
		assertEquals("A1000B20000D30000", textViewer.getDocument().get());
		assertEquals("30000", textWidget.getSelection());
	}

	@Test
	public void getDialogSettings_InAllPossibleScenarios_ShouldBeSavedBetweenSessions() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("Hello World!", false);
		checkDefaultSettings();
		SWTBotCheckBox caseCheckBox = findReplaceDialogWrapper.getfCaseCheckBox();
		caseCheckBox.select();
		SWTBotCheckBox isWrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		isWrapCheckBox.deselect();
		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("World");
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("return \"Universe\";");
		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();
		SWTBotButton replaceButton = findReplaceDialogWrapper.getfReplaceSelectionButton();
		replaceButton.click();
		assertEquals("Hello Universe!", textViewer.getDocument().get());
		SWTBotButton closeButton = findReplaceDialogWrapper.getfCloseButton();
		closeButton.click();
		openFindReplaceDialog();
		textWidget.setText("Hello World!");
		caseCheckBox = findReplaceDialogWrapper.getfCaseCheckBox();
		assertTrue(caseCheckBox.isChecked());
		isWrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertFalse(isWrapCheckBox.isChecked());
		String[] findStuff = newSection.getArray(DialogSettingsConstants.FIND_HISTORY);
		assertTrue(Arrays.asList(findStuff).contains("World"));
		/*
		 * findField = findReplaceDialogWrapper.getfFindField();
		 * assertEquals("World", findField.getText());
		 */
		matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		assertEquals("return \"Universe\";", matchEvaluatorField.getText());
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorButWithScopeSelectedLines_PerformsTheCorrectReplacement() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget(
				"Today is the 14th August\n16! Florian is 23 years old. His sister is 2 years older. She is 25 years old!",
				false);

		textWidget.selectRange(1, 0,
				"16! Florian is 23 years old. His sister is 2 years older. She is 25 years old!".length());

		findReplaceDialogWrapper.getfSelectedRangeRadioButton().click();

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{2}");

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\" + (i+1); ");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals(
				"Today is the 14th August\n17! Florian is 24 years old. His sister is 2 years older. She is 26 years old!",
				textWidget.getText());
	}

	@Test
	public void replaceAll_WithAMatchEvaluatorAndWithScopeSelectedLinesWithLookbehindsAndAheads_PerformsCorrectReplacement() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName());
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();
		updateTarget("A\nB\nCA\nB\nC", false);

		textWidget.selectRange(3, 0, "B".length());

		findReplaceDialogWrapper.getfSelectedRangeRadioButton().click();

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();

		findField.setText("(?<=A\n)B(?=\nC)");
		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();

		matchEvaluatorField.setText("return \"Barbara\";");
		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("A\nB\nCA\nBarbara\nC", textWidget.getText());

	}

	@Test
	public void pathToJavac_initially_isSetAutomatically() {
		IDialogSettings dialogSettings = new DialogSettings("root");
		IEditorStatusLine statusLine = Mockito.mock(IEditorStatusLine.class);
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine));
		ServiceLocator.setInjector(injector);
		openFindReplaceDialog();

		SWTBotButton closeButton = findReplaceDialogWrapper.getfCloseButton();
		closeButton.click();

		IDialogSettings section = dialogSettings.getSection(FindReplaceDialog.class.getName());
		File javacFile = new File(section.get(DialogSettingsConstants.PATH_TO_JAVAC));

		assertTrue(javacFile.exists() && javacFile.getName().equals("javac.exe"));
	}

	private void checkDefaultSettings() {
		SWTBotCheckBox isRegExCheckBox = findReplaceDialogWrapper.getfIsRegExCheckBox();
		assertTrue(isRegExCheckBox.isChecked());
		SWTBotCheckBox useMatchEvaluatorCheckBox = findReplaceDialogWrapper.getfUseMatchEvaluatorCheckBox();
		assertTrue(useMatchEvaluatorCheckBox.isVisible() && useMatchEvaluatorCheckBox.isChecked());
		SWTBotCheckBox isWrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue(isWrapCheckBox.isChecked());
		SWTBotCheckBox caseCheckBox = findReplaceDialogWrapper.getfCaseCheckBox();
		assertFalse(caseCheckBox.isChecked());
		SWTBotCheckBox wholeWordCheckBox = findReplaceDialogWrapper.getfWholeWordCheckBox();
		assertTrue(!wholeWordCheckBox.isEnabled() && !wholeWordCheckBox.isChecked());
		SWTBotCheckBox incrementalCheckBox = findReplaceDialogWrapper.getfIncrementalCheckBox();
		assertTrue(!incrementalCheckBox.isEnabled() && !incrementalCheckBox.isChecked());
	}

}
