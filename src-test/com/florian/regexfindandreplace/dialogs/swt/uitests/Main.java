package com.florian.regexfindandreplace.dialogs.swt.uitests;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

import com.florian.regexfindandreplace.IMatchEvaluator;

public class Main {

	private static String input = "" +	
"	private Label fReplaceLabel;"+
"private Label  fStatusLabel;"+
"	private Button fForwardRadioButton;"+
"private Button  fGlobalRadioButton;"+
"private Button  fSelectedRangeRadioButton;"+
"	private Button fCaseCheckBox;"+
"private Button  fWrapCheckBox;"+
"private Button  fWholeWordCheckBox;"+
"private Button  fIncrementalCheckBox;"+
"	private Button fIsRegExCheckBox;"+
"	private Button fReplaceSelectionButton;"+
"private Button  fReplaceFindButton;"+
"private Button  fFindNextButton;"+
"private Button  fReplaceAllButton;"+
"	private Combo fFindField;"+
" private Combo fReplaceField;"+
"	private Composite fMatchEvaluatorPanel;"+
"	private Button fUseMatchEvaluatorCheckBox;"+
"	private Text fMatchEvaluatorField;"+
"	private Label fMatchEvaluatorLabel;"+
"	private Label fMatchEvaluatorFlagsLabel;"+
"	private Button fCloseButton;"+
"private Button fBackwardRadioButton;";
	
	public static void main(String[] args) {
		
		Pattern pattern = Pattern.compile("private\\s*(Label|Button|Combo|Composite|Text)\\s*([\\w_]+)\\s*;");
		Matcher matcher = pattern.matcher(input);
		
		StringBuilder sb = new StringBuilder();
		while(matcher.find() )
		{
			sb.append("private ");
			String fieldName = matcher.group(2);
			String type = matcher.group(1);
			if(type.equals("Button"))
			{
				if(fieldName.endsWith("RadioButton"))
				{
					type = "Radio";
				}
				else if(fieldName.endsWith("CheckBox"))
				{
					type = "CheckBox";
				}
			}
			sb.append("SWTBot").append(type).append(" ").append(fieldName).append(";\n");
			sb.append("public SWTBot").append(type).append(" ").append("get").append(fieldName).append("(){\n");
			sb.append("if(").append(fieldName).append("== null)").append(fieldName).append("= new SWTBot").append(type).append("(").append("dialog.get").append(fieldName).append("());\n");
			sb.append("return ").append(fieldName).append(";}\n\n");
			
		}
		
		System.out.println(sb.toString());

	}

}
