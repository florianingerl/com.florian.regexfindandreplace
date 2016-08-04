package com.florian.regexfindandreplace.handlers.unittests;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.graphics.Point;

public class SimpleFindReplaceTarget implements IFindReplaceTarget {

	@Override
	public boolean canPerformFind() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Point getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSelectionText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void replaceSelection(String text) {
		// TODO Auto-generated method stub
		
	}

}
