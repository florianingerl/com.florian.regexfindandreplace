package com.florian.regexfindandreplace.dialogs.swt.uitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		assertEquals("}},flags:0);", findReplaceDialogWrapper.getfMatchEvaluatorFlagsLabel().getText());

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
	public void replaceAll_WithAMatchEvaluatorWhereAnExceptionIsThrownOnTheSecondMatch_AnAppropriateErrorMessageIsDisplayAndTheMatchIsSelected() {
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

		assertEquals("A101BHermannC200D", target.getText());
		assertEquals("Hermann", target.getSelectionText());

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("1 match replaced,\nNumberFormatException occured", statusLabel.getText());

		Mockito.verify(statusLine).setMessage(true, "NumberFormatException occured", null);
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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("A100B200C");
		updateTarget(target, true, false);

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i+1);");

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{3}");

		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();

		assertEquals("100", target.getSelectionText());

		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		replaceFindButton.click();

		assertEquals("A101B200C", target.getText());
		assertEquals("200", target.getSelectionText());

		SWTBotCheckBox wrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue(wrapCheckBox.isChecked());

		replaceFindButton.click();
		assertEquals("A101B201C", target.getText());

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("Wrapped search", statusLabel.getText());

		Mockito.verify(statusLine).setMessage(false, "Wrapped search", null);

		assertEquals("101", target.getSelectionText());
		wrapCheckBox.deselect();

		target.setCaretPosition(4);
		findNextButton.click();

		replaceFindButton.click();
		assertEquals("A101B202C", target.getText());
		assertEquals("202", target.getSelectionText());
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
		assertTrue(fBackwardRadioButton.isSelected());

		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();

		assertEquals("200", target.getSelectionText());

		SWTBotButton replaceFindButton = findReplaceDialogWrapper.getfReplaceFindButton();
		assertTrue(replaceFindButton.isEnabled());
		replaceFindButton.click();

		assertEquals("A100B201C", target.getText());
		assertEquals("100", target.getSelectionText());

		SWTBotCheckBox wrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertTrue(wrapCheckBox.isChecked());

		assertTrue(replaceFindButton.isEnabled());
		replaceFindButton.click();
		assertEquals("A101B201C", target.getText());

		SWTBotLabel statusLabel = findReplaceDialogWrapper.getfStatusLabel();
		assertEquals("Wrapped search", statusLabel.getText());

		Mockito.verify(statusLine).setMessage(false, "Wrapped search", null);

		assertEquals("201", target.getSelectionText());
		wrapCheckBox.deselect();

		// If its target.setCaretPosition(5), the test passes through
		target.setCaretPosition(4);
		findNextButton.click();

		assertEquals("101", target.getSelectionText());

		replaceFindButton.click();
		assertEquals("A102B201C", target.getText());
		assertEquals("102", target.getSelectionText());
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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("In Deutschland leben 80000000 Menschen!Auf der Welt leben 7000000000 Menschen");
		updateTarget(target, true, false);

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("return \".\";");

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(?<=\\d)(?=(\\d{3})+(?!\\d))");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("In Deutschland leben 80.000.000 Menschen!Auf der Welt leben 7.000.000.000 Menschen",
				target.getText());

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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("In Deutschland leben 80000000 Menschen!Auf der Welt leben 7000000000 Menschen");
		updateTarget(target, true, false);

		SWTBotCombo replaceField = findReplaceDialogWrapper.getfReplaceField();
		replaceField.setText(".");

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(?<=\\d)(?=(\\d{3})+(?!\\d))");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("In Deutschland leben 80.000.000 Menschen!Auf der Welt leben 7.000.000.000 Menschen",
				target.getText());

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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("17_{hex}+10_{bin}=27");
		updateTarget(target, true, false);

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText(
				"if(match.group(1).equals(\"hex\")) return Integer.toHexString( Integer.parseInt(match.group() ) ); else if(match.group(1).equals(\"bin\")) return Integer.toBinaryString( Integer.parseInt(match.group())); return null; ");

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d{2}(?=_\\{(hex|bin)\\})");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("11_{hex}+1010_{bin}=27", target.getText());

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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("x17=17 and 010=10");
		updateTarget(target, true, false);

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText(
				"if(match.group(1).equals(\"x\")) return Integer.toHexString( Integer.parseInt(match.group() ) ); else if(match.group(1).equals(\"0\")) return Integer.toOctalString( Integer.parseInt(match.group())); return null; ");

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("(?<=(x|0))[1-9]\\d*");

		SWTBotButton replaceAllButton = findReplaceDialogWrapper.getfReplaceAllButton();
		replaceAllButton.click();

		assertEquals("x11=17 and 012=10", target.getText());

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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("A100B200D30");
		updateTarget(target, true, false);

		SWTBotText matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i*10);");

		SWTBotCombo findField = findReplaceDialogWrapper.getfFindField();
		findField.setText("\\d+");

		SWTBotButton findNextButton = findReplaceDialogWrapper.getfFindNextButton();
		findNextButton.click();

		assertEquals("100", target.getSelectionText());

		SWTBotButton replaceButton = findReplaceDialogWrapper.getfReplaceSelectionButton();
		replaceButton.click();

		assertEquals("A1000B200D30", target.getText());
		assertEquals("1000", target.getSelectionText());

		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i*100);");

		findNextButton.click();
		assertEquals("200", target.getSelectionText());

		replaceButton.click();
		assertEquals("A1000B20000D30", target.getText());
		assertEquals("20000", target.getSelectionText());

		matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+ (i*1000);");
		findNextButton.click();
		assertEquals("30", target.getSelectionText());

		replaceButton.click();
		assertEquals("A1000B20000D30000", target.getText());
		assertEquals("30000", target.getSelectionText());

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

		FindReplaceTarget target = new FindReplaceTarget();
		target.setText("Hello World!");
		updateTarget(target, true, false);

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
		assertEquals("Hello Universe!", target.getText());

		SWTBotButton closeButton = findReplaceDialogWrapper.getfCloseButton();
		closeButton.click();

		openFindReplaceDialog();
		target = new FindReplaceTarget();
		target.setText("Hello World!");
		updateTarget(target, true, true);

		caseCheckBox = findReplaceDialogWrapper.getfCaseCheckBox();
		assertTrue(caseCheckBox.isChecked());

		isWrapCheckBox = findReplaceDialogWrapper.getfWrapCheckBox();
		assertFalse(isWrapCheckBox.isChecked());

		String[] findStuff = newSection.getArray(DialogSettingsConstants.FIND_HISTORY);
		assertTrue(Arrays.asList(findStuff).contains("World"));

		findField = findReplaceDialogWrapper.getfFindField();
		assertEquals("World", findField.getText());

		matchEvaluatorField = findReplaceDialogWrapper.getfMatchEvaluatorField();
		assertEquals("return \"Universe\";", matchEvaluatorField.getText());
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
