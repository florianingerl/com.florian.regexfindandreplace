package com.florian.regexfindandreplace.dialogs.swt.uitests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;

public class FindReplaceTarget
		implements IFindReplaceTargetExtension3, IFindReplaceTargetExtension2, IFindReplaceTarget {

	private String text = "";

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	private Point selection = new Point(0, 0);

	private Matcher lastMatcher;

	public void setCaretPosition(int caretPosition) {
		if (text == null) {
			selection = new Point(0, 0);
			return;
		}

		if (caretPosition < 0)
			caretPosition = 0;
		else if (caretPosition > text.length())
			caretPosition = text.length();
		selection.x = caretPosition;
		selection.y = 0;
	}

	@Override
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord, boolean regExSearch) {
		if (regExSearch) {
			int flags = 0;
			if (!caseSensitive)
				flags |= Pattern.CASE_INSENSITIVE;
			if (wholeWord)
				findString = "\\b" + findString + "\\b";
			Pattern pattern = Pattern.compile(findString, flags);
			lastMatcher = pattern.matcher(text);

			if (searchForward) {
				if (!lastMatcher.find(offset))
					return -1;
				selection.x = lastMatcher.start();
				selection.y = lastMatcher.end() - lastMatcher.start();
				return selection.x;
			} else {
				int lastStart = -1, lastEnd = -1;
				while (lastMatcher.find()) {
					if (lastMatcher.end() > offset)
						break;
					lastStart = lastMatcher.start();
					lastEnd = lastMatcher.end();
				}
				if (lastStart != -1) {
					selection.x = lastStart;
					selection.y = lastEnd - lastStart;
					return selection.x;
				}
			}
		} else {
			throw new UnsupportedOperationException("Non-regex search is not yet implemented!");
			// lastMatcher = null;
		}
		return -1;
	}

	@Override
	public void replaceSelection(String replacement, boolean regExReplace) {
		if (regExReplace) {
			if (lastMatcher == null || selection.x != lastMatcher.start()
					|| selection.x + selection.y != lastMatcher.end())
				throw new UnsupportedOperationException("findAndSelect with regex options needs to be called first");
			StringBuilder sb = new StringBuilder();
			sb.append(text.substring(0, lastMatcher.start()));
			lastMatcher.reset(this.text.substring(lastMatcher.start(), lastMatcher.end()));
			String temp = lastMatcher.replaceFirst(replacement);
			sb.append(temp);
			sb.append(text.substring(lastMatcher.end()));
			text = sb.toString();
			selection.y = temp.length();
		} else {
			replaceSelection(replacement);
		}
	}

	@Override
	public boolean canPerformFind() {
		return true;
	}

	@Override
	public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord) {
		return findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord, false);
	}

	@Override
	public Point getSelection() {
		return selection;
	}

	@Override
	public String getSelectionText() {
		if (text == null)
			return "";
		String temp = text.substring(selection.x, selection.x + selection.y);
		return temp != null ? temp : "";
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public void replaceSelection(String replacement) {
		StringBuilder sb = new StringBuilder();
		sb.append(text.substring(0, selection.x));
		sb.append(replacement);
		sb.append(text.substring(selection.x + selection.y));
		text = sb.toString();
		selection.y = replacement.length();
	}

	@Override
	public boolean validateTargetState() {
		return true;
	}

}
