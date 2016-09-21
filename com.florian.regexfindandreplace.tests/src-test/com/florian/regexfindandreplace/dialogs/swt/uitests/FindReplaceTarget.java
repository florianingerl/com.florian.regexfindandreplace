package com.florian.regexfindandreplace.dialogs.swt.uitests;

import com.ingerlflori.util.regex.Matcher;
import com.ingerlflori.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;

public class FindReplaceTarget
		implements IFindReplaceTargetExtension3, IFindReplaceTargetExtension2, IFindReplaceTarget {

	private static Logger logger = Logger.getLogger(FindReplaceTarget.class);

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
		selection = new Point(caretPosition, 0);
	}

	@Override
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord, boolean regExSearch) {
		logger.debug("Offset = " + offset + " findString = " + findString + " text = " + text);
		if (wholeWord && regExSearch)
			throw new UnsupportedOperationException(
					"wholeWord and regExSearch is not supported. See the documentation of IFindReplaceTargetExtension3");
		if (offset == -1)
			offset = searchForward ? 0 : text.length(); // Why is it called with
														// -1??
		if (regExSearch) {
			int flags = Pattern.MULTILINE;
			if (!caseSensitive)
				flags |= Pattern.CASE_INSENSITIVE;
			Pattern pattern = Pattern.compile(findString, flags);
			lastMatcher = pattern.matcher(text);

			if (searchForward) {
				if (!lastMatcher.find(offset)) {
					logger.debug("Couldn't find regex " + findString + " starting from " + offset + " in " + text);
					return -1;
				}
				selection.x = lastMatcher.start();
				selection.y = lastMatcher.end() - lastMatcher.start();
				return selection.x;
			} else {
				System.out.println("Backward search!");
				int lastStart = -1, lastEnd = -1;
				while (lastMatcher.find()) {
					if (lastMatcher.end() <= offset) {
						lastStart = lastMatcher.start();
						lastEnd = lastMatcher.end();
					} else if (lastMatcher.end() > offset)
						break;

				}
				System.out.println("Last start: " + lastStart + " LastEnd: " + lastEnd);
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
			sb.append(text.substring(selection.x + selection.y));
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
