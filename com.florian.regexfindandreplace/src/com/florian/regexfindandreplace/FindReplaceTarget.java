package com.florian.regexfindandreplace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public class FindReplaceTarget
		implements IFindReplaceTarget, IFindReplaceTargetExtension, IFindReplaceTargetExtension3 {
	/** The range for this target. */
	private FindReplaceRange fRange;
	/** The highlight color of the range of this target. */
	private Color fScopeHighlightColor;
	/** The document partitioner remembered in case of a "Replace All". */
	private Map fRememberedPartitioners;
	/**
	 * The active rewrite session.
	 * 
	 * @since 3.1
	 */
	private DocumentRewriteSession fRewriteSession;

	private Method modelSelection2WidgetSelection;
	private Method canPerformFind;
	private Method stopSequentialRewriteMode;
	private Method startSequentialRewriteMode;
	private Method redraws;
	private Method internalRevealRange;
	private Method selectionChanged;
	private TextViewer textViewer;

	private FindReplaceDocumentAdapter findReplaceDocumentAdapter;

	public FindReplaceTarget(TextViewer textViewer) throws Exception {
		this.textViewer = textViewer;
		Class<TextViewer> clazz = TextViewer.class;
		modelSelection2WidgetSelection = clazz.getDeclaredMethod("modelSelection2WidgetSelection", Point.class);
		modelSelection2WidgetSelection.setAccessible(true);

		canPerformFind = clazz.getDeclaredMethod("canPerformFind");
		canPerformFind.setAccessible(true);

		stopSequentialRewriteMode = clazz.getDeclaredMethod("stopSequentialRewriteMode");
		stopSequentialRewriteMode.setAccessible(true);

		startSequentialRewriteMode = clazz.getDeclaredMethod("startSequentialRewriteMode", Boolean.class);
		startSequentialRewriteMode.setAccessible(true);

		redraws = clazz.getDeclaredMethod("redraws");
		redraws.setAccessible(true);

		internalRevealRange = clazz.getDeclaredMethod("internalRevealRange", Integer.class, Integer.class);
		internalRevealRange.setAccessible(true);

		selectionChanged = clazz.getDeclaredMethod("selectionChanged", Integer.class, Integer.class);
		selectionChanged.setAccessible(true);
	}

	private void selectionChanged(int offset, int length) {
		try {
			selectionChanged.invoke(textViewer, offset, length);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void internalRevealRange(int start, int end) {
		try {
			internalRevealRange.invoke(textViewer, start, end);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private boolean redraws() {
		try {
			return (boolean) redraws.invoke(textViewer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}

	private FindReplaceDocumentAdapter getFindReplaceDocumentAdapter() {
		if (findReplaceDocumentAdapter == null) {
			findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(textViewer.getDocument());
		}
		return findReplaceDocumentAdapter;
	}

	private void startSequentialRewriteMode(boolean normalized) {
		try {
			startSequentialRewriteMode.invoke(textViewer, normalized);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private boolean canPerformFind2() {
		try {
			return (boolean) canPerformFind.invoke(textViewer);
		} catch (Exception e) {
			return false;
		}
	}

	private void stopSequentialRewriteMode() {

		try {
			stopSequentialRewriteMode.invoke(textViewer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	/*
	 * @see IFindReplaceTarget#getSelectionText()
	 */
	public String getSelectionText() {
		Point s = textViewer.getSelectedRange();
		if (s.x > -1 && s.y > -1) {
			try {
				IDocument document = textViewer.getDocument();
				return document.get(s.x, s.y);
			} catch (BadLocationException x) {
			}
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * @see IFindReplaceTarget#replaceSelection(String)
	 */
	public void replaceSelection(String text) {
		replaceSelection(text, false);
	}

	/*
	 * @see IFindReplaceTarget#replaceSelection(String)
	 */
	public void replaceSelection(String text, boolean regExReplace) {
		Point s = textViewer.getSelectedRange();
		if (s.x > -1 && s.y > -1) {
			try {
				IRegion matchRegion = getFindReplaceDocumentAdapter().replace(text, regExReplace);
				int length = -1;
				if (matchRegion != null)
					length = matchRegion.getLength();

				if (text != null && length > 0)
					textViewer.setSelectedRange(s.x, length);
			} catch (BadLocationException x) {
			}
		}
	}

	/*
	 * @see IFindReplaceTarget#isEditable()
	 */
	public boolean isEditable() {
		return textViewer.isEditable();
	}

	private Point modelSelection2WidgetSelection(Point modelSelection) {
		try {
			return (Point) modelSelection2WidgetSelection.invoke(textViewer, modelSelection);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/*
	 * @see IFindReplaceTarget#getSelection()
	 */
	public Point getSelection() {
		Point modelSelection = textViewer.getSelectedRange();
		Point widgetSelection = modelSelection2WidgetSelection(modelSelection);
		return widgetSelection != null ? widgetSelection : new Point(-1, -1);
	}

	public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord, boolean regExSearch) {

		int modelOffset = widgetOffset == -1 ? -1 : textViewer.widgetOffset2ModelOffset(widgetOffset);

		if (fRange != null) {
			IRegion range = fRange.getRange();
			modelOffset = findAndSelectInRange2(modelOffset, findString, searchForward, caseSensitive, wholeWord,
					range.getOffset(), range.getLength(), regExSearch);
		} else {
			modelOffset = findAndSelect2(modelOffset, findString, searchForward, caseSensitive, wholeWord, regExSearch);
		}

		widgetOffset = modelOffset == -1 ? -1 : textViewer.modelOffset2WidgetOffset(modelOffset);
		return widgetOffset;
	}

	private int findAndSelect2(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive,
			boolean wholeWord, boolean regExSearch) {
		if (textViewer.getTextWidget() == null)
			return -1;

		try {

			int widgetOffset = (startPosition == -1 ? startPosition
					: textViewer.modelOffset2WidgetOffset(startPosition));
			FindReplaceDocumentAdapter adapter = getFindReplaceDocumentAdapter();
			IRegion matchRegion = adapter.find(widgetOffset, findString, forwardSearch, caseSensitive, wholeWord,
					regExSearch);
			if (matchRegion != null) {
				int widgetPos = matchRegion.getOffset();
				int length = matchRegion.getLength();

				// Prevents setting of widget selection with line delimiters at
				// beginning or end
				char startChar = adapter.charAt(widgetPos);
				char endChar = adapter.charAt(widgetPos + length - 1);
				boolean borderHasLineDelimiter = startChar == '\n' || startChar == '\r' || endChar == '\n'
						|| endChar == '\r';
				boolean redraws = redraws();
				if (borderHasLineDelimiter && redraws)
					textViewer.setRedraw(false);

				if (redraws()) {
					textViewer.getTextWidget().setSelectionRange(widgetPos, length);
					internalRevealRange(widgetPos, widgetPos + length);
					selectionChanged(widgetPos, length);
				} else {
					textViewer.setSelectedRange(textViewer.widgetOffset2ModelOffset(widgetPos), length);
					if (redraws)
						textViewer.setRedraw(true);
				}

				return textViewer.widgetOffset2ModelOffset(widgetPos);
			}

		} catch (BadLocationException x) {
			x.printStackTrace();
		}

		return -1;
	}

	protected int findAndSelectInRange2(int startPosition, String findString, boolean forwardSearch,
			boolean caseSensitive, boolean wholeWord, int rangeOffset, int rangeLength, boolean regExSearch) {
		if (textViewer.getTextWidget() == null)
			return -1;

		try {

			int modelOffset;
			if (forwardSearch && (startPosition == -1 || startPosition < rangeOffset)) {
				modelOffset = rangeOffset;
			} else if (!forwardSearch && (startPosition == -1 || startPosition > rangeOffset + rangeLength)) {
				modelOffset = rangeOffset + rangeLength;
			} else {
				modelOffset = startPosition;
			}

			int widgetOffset = textViewer.modelOffset2WidgetOffset(modelOffset);
			if (widgetOffset == -1)
				return -1;

			FindReplaceDocumentAdapter adapter = getFindReplaceDocumentAdapter();
			IRegion matchRegion = adapter.find(widgetOffset, findString, forwardSearch, caseSensitive, wholeWord,
					regExSearch);
			int widgetPos = -1;
			int length = 0;
			if (matchRegion != null) {
				widgetPos = matchRegion.getOffset();
				length = matchRegion.getLength();
			}
			int modelPos = widgetPos == -1 ? -1 : textViewer.widgetOffset2ModelOffset(widgetPos);

			if (widgetPos != -1 && (modelPos < rangeOffset || modelPos + length > rangeOffset + rangeLength))
				widgetPos = -1;

			if (widgetPos > -1) {

				// Prevents setting of widget selection with line delimiters at
				// beginning or end
				char startChar = adapter.charAt(widgetPos);
				char endChar = adapter.charAt(widgetPos + length - 1);
				boolean borderHasLineDelimiter = startChar == '\n' || startChar == '\r' || endChar == '\n'
						|| endChar == '\r';
				boolean redraws = redraws();
				if (borderHasLineDelimiter && redraws)
					textViewer.setRedraw(false);

				if (redraws()) {
					textViewer.getTextWidget().setSelectionRange(widgetPos, length);
					internalRevealRange(widgetPos, widgetPos + length);
					selectionChanged(widgetPos, length);
				} else {
					textViewer.setSelectedRange(modelPos, length);
					if (redraws)
						textViewer.setRedraw(true);
				}

				return modelPos;
			}

		} catch (BadLocationException x) {
			x.printStackTrace();
		}

		return -1;
	}

	/*
	 * @see IFindReplaceTarget#canPerformFind()
	 */
	public boolean canPerformFind() {
		return canPerformFind2();
	}

	/*
	 * @see IFindReplaceTargetExtension#beginSession()
	 * 
	 * @since 2.0
	 */
	public void beginSession() {
		fRange = null;
	}

	/*
	 * @see IFindReplaceTargetExtension#endSession()
	 * 
	 * @since 2.0
	 */
	public void endSession() {
		if (fRange != null) {
			fRange.uninstall();
			fRange = null;
		}
	}

	/*
	 * @see IFindReplaceTargetExtension#getScope()
	 * 
	 * @since 2.0
	 */
	public IRegion getScope() {
		return fRange == null ? null : fRange.getRange();
	}

	/*
	 * @see IFindReplaceTargetExtension#getLineSelection()
	 * 
	 * @since 2.0
	 */
	public Point getLineSelection() {
		Point point = textViewer.getSelectedRange();

		try {
			IDocument document = textViewer.getDocument();

			// beginning of line
			int line = document.getLineOfOffset(point.x);
			int offset = document.getLineOffset(line);

			// end of line
			IRegion lastLineInfo = document.getLineInformationOfOffset(point.x + point.y);
			int lastLine = document.getLineOfOffset(point.x + point.y);
			int length;
			if (lastLineInfo.getOffset() == point.x + point.y && lastLine > 0)
				length = document.getLineOffset(lastLine - 1) + document.getLineLength(lastLine - 1) - offset;
			else
				length = lastLineInfo.getOffset() + lastLineInfo.getLength() - offset;

			return new Point(offset, length);

		} catch (BadLocationException e) {
			// should not happen
			return new Point(point.x, 0);
		}
	}

	/*
	 * @see IFindReplaceTargetExtension#setSelection(int, int)
	 * 
	 * @since 2.0
	 */
	public void setSelection(int modelOffset, int modelLength) {
		textViewer.setSelectedRange(modelOffset, modelLength);
	}

	/*
	 * @see IFindReplaceTargetExtension#setScope(IRegion)
	 * 
	 * @since 2.0
	 */
	public void setScope(IRegion scope) {
		if (fRange != null)
			fRange.uninstall();

		if (scope == null) {
			fRange = null;
			return;
		}

		fRange = new FindReplaceRange(textViewer, scope);
		fRange.setHighlightColor(fScopeHighlightColor);
		fRange.install();
	}

	/*
	 * @see IFindReplaceTargetExtension#setScopeHighlightColor(Color)
	 * 
	 * @since 2.0
	 */
	public void setScopeHighlightColor(Color color) {
		if (fRange != null)
			fRange.setHighlightColor(color);
		fScopeHighlightColor = color;
	}

	/*
	 * @see IFindReplaceTargetExtension#setReplaceAllMode(boolean)
	 * 
	 * @since 2.0
	 */
	public void setReplaceAllMode(boolean replaceAll) {

		// http://bugs.eclipse.org/bugs/show_bug.cgi?id=18232

		IDocument document = textViewer.getDocument();

		if (replaceAll) {

			if (document instanceof IDocumentExtension4) {
				IDocumentExtension4 extension = (IDocumentExtension4) document;
				fRewriteSession = extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
			} else {
				textViewer.setRedraw(false);
				startSequentialRewriteMode(false);

				if (textViewer.getUndoManager() != null)
					textViewer.getUndoManager().beginCompoundChange();

				fRememberedPartitioners = TextUtilities.removeDocumentPartitioners(document);
			}

		} else {

			if (document instanceof IDocumentExtension4) {
				IDocumentExtension4 extension = (IDocumentExtension4) document;
				extension.stopRewriteSession(fRewriteSession);
			} else {
				textViewer.setRedraw(true);
				stopSequentialRewriteMode();

				if (textViewer.getUndoManager() != null)
					textViewer.getUndoManager().endCompoundChange();

				if (fRememberedPartitioners != null)
					TextUtilities.addDocumentPartitioners(document, fRememberedPartitioners);
			}
		}
	}

	@Override
	public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive,
			boolean wholeWord) {
		try {
			return findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord, false);
		} catch (PatternSyntaxException x) {
			return -1;
		}
	}

}