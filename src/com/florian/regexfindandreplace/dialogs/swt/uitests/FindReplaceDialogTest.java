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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferenceConstants;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.florian.regexfindandreplace.activators.ServiceLocator;
import com.florian.regexfindandreplace.dialogs.swt.DialogSettingsConstants;
import com.florian.regexfindandreplace.dialogs.swt.FindReplaceDialog;
import com.florian.regexfindandreplace.dialogs.swt.ISWTBotFindConstant;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class FindReplaceDialogTest {

	
	private SWTBot bot;
	private FindReplaceDialog dialog;
	
	@BeforeClass
	public static void beforeClass()
	{
		SWTBotPreferences.TIMEOUT = 20000;
	}
	
	@Test
	public void replaceAll_WithAMatchEvaluator() {
		   
		IDialogSettings dialogSettings = new DialogSettings("root");
		IDialogSettings newSection = dialogSettings.addNewSection(FindReplaceDialog.class.getName() );
		newSection.put(DialogSettingsConstants.IS_REG_EX, true);
		newSection.put(DialogSettingsConstants.USE_MATCH_EVALUATOR, true);
		newSection.put(DialogSettingsConstants.PATH_TO_JAVAC, "C:/Program Files/Java/jdk1.8.0_92/bin/javac.exe");
		
		IEditorStatusLine statusLine = Mockito.mock( IEditorStatusLine.class );
		Injector injector = Guice.createInjector(new FindReplaceDialogTestingModule(dialogSettings, statusLine ) );
		ServiceLocator.setInjector(injector);
		
		final Display display = Display.getDefault();
        final Shell shell = new Shell(display, SWT.SHELL_TRIM);

        Realm.runWithDefault(
            SWTObservables.getRealm(display), new Runnable() {

            public void run() {
                
                shell.setLayout(new FillLayout() );
                Button openDialog = new Button(shell, SWT.PUSH);
                openDialog.addSelectionListener(new SelectionAdapter() {
                	@Override
                	public void widgetSelected(SelectionEvent event)
                	{
                		dialog = new FindReplaceDialog(shell);
                		dialog.create();
                		dialog.open();
                	}
                });
                openDialog.setText("Open find/replace dialog");
                
                shell.open();
                bot = new SWTBot();
                SWTBotButton botButton = bot.button("Open find/replace dialog");
                assertTrue( botButton.isEnabled() );
                botButton.click();
                
                FindReplaceTarget target = new FindReplaceTarget();
                target.setText("Florian is 23 years old. His sister is 2 years older. She is 25 years old!");
                dialog.updateTarget(target, true, false );
                
                SWTBotCheckBox isRegExCheckBox = bot.checkBoxWithId(ISWTBotFindConstant.FIND_KEY,"isRegExSearch");
                assertTrue(isRegExCheckBox.isChecked() );
                
                SWTBotCheckBox useMatchEvaluatorCheckBox = bot.checkBoxWithId(ISWTBotFindConstant.FIND_KEY, "Use a match evaluator");
                assertTrue(useMatchEvaluatorCheckBox.isVisible() );
                assertTrue(useMatchEvaluatorCheckBox.isChecked() );
                
                SWTBotCombo findField = bot.comboBoxWithId(ISWTBotFindConstant.FIND_KEY, "findField");
                SWTBotLabel matchEvaluatorLabel = bot.labelWithId(ISWTBotFindConstant.FIND_KEY, "matchEvaluatorLabel");
                assertTrue( matchEvaluatorLabel.isVisible() );
                
                findField.setText("\\d{2}");
                assertEquals( "editorContent = RegexUtils.replaceNext/All(\ninput: editorContent,\nregex: \"\\d{2}\",\nnew IMatchEvaluator(){\n@Override\npublic String evaluateMatch(MatchResult match){" ,matchEvaluatorLabel.getText() );
                
                SWTBotText matchEvaluatorField = bot.textWithId(ISWTBotFindConstant.FIND_KEY, "matchEvaluatorField");
                matchEvaluatorField.setText("int i = Integer.parseInt( match.group() ); return \"\"+(i+1);"); 
                
                SWTBotButton replaceAllButton = bot.buttonWithId(ISWTBotFindConstant.FIND_KEY, "replaceAllButton");
                replaceAllButton.click();
                
                assertEquals("Florian is 24 years old. His sister is 2 years older. She is 26 years old!", target.getText() );
                
            }
        });

        shell.dispose();
        display.dispose();
	}
	

}
