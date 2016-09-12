package com.florian.regexfindandreplace;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public class FindReplaceRange implements LineBackgroundListener, ITextListener, IPositionUpdater {

	/** Internal name for the position category used to update the range. */
	private final static String RANGE_CATEGORY = "org.eclipse.jface.text.TextViewer.find.range"; //$NON-NLS-1$

	/** The highlight color of this range. */
	private Color fHighlightColor;
	/** The position used to lively update this range's extent. */
	private Position fPosition;

	private TextViewer textViewer;

	/**
	 * Creates a new find/replace range with the given extent.
	 *
	 * @param range
	 *            the extent of this range
	 */
	public FindReplaceRange(TextViewer textViewer, IRegion range) {
		this.textViewer = textViewer;
		setRange(range);
	}

	/**
	 * Sets the extent of this range.
	 *
	 * @param range
	 *            the extent of this range
	 */
	public void setRange(IRegion range) {
		fPosition = new Position(range.getOffset(), range.getLength());
	}

	/**
	 * Returns the extent of this range.
	 *
	 * @return the extent of this range
	 */
	public IRegion getRange() {
		return new Region(fPosition.getOffset(), fPosition.getLength());
	}

	/**
	 * Sets the highlight color of this range. Causes the range to be redrawn.
	 *
	 * @param color
	 *            the highlight color
	 */
	public void setHighlightColor(Color color) {
		fHighlightColor = color;
		paint();
	}

	/*
	 * @see LineBackgroundListener#lineGetBackground(LineBackgroundEvent)
	 * 
	 * @since 2.0
	 */
	public void lineGetBackground(LineBackgroundEvent event) {
		/*
		 * Don't use cached line information because of patched redrawing
		 * events.
		 */

		if (textViewer.getTextWidget() != null) {
			int offset = textViewer.widgetOffset2ModelOffset(event.lineOffset);
			if (fPosition.includes(offset))
				event.lineBackground = fHighlightColor;
		}
	}

	/**
	 * Installs this range. The range registers itself as background line
	 * painter and text listener. Also, it creates a category with the viewer's
	 * document to maintain its own extent.
	 */
	public void install() {
		textViewer.addTextListener(this);
		textViewer.getTextWidget().addLineBackgroundListener(this);

		IDocument document = textViewer.getDocument();
		try {
			document.addPositionCategory(RANGE_CATEGORY);
			document.addPosition(RANGE_CATEGORY, fPosition);
			document.addPositionUpdater(this);
		} catch (BadPositionCategoryException e) {
			// should not happen
		} catch (BadLocationException e) {
			// should not happen
		}

		paint();
	}

	/**
	 * Uninstalls this range.
	 * 
	 * @see #install()
	 */
	public void uninstall() {

		// http://bugs.eclipse.org/bugs/show_bug.cgi?id=19612

		IDocument document = textViewer.getDocument();
		if (document != null) {
			document.removePositionUpdater(this);
			document.removePosition(fPosition);
		}

		if (textViewer.getTextWidget() != null && !textViewer.getTextWidget().isDisposed())
			textViewer.getTextWidget().removeLineBackgroundListener(this);

		textViewer.removeTextListener(this);

		clear();
	}

	/**
	 * Clears the highlighting of this range.
	 */
	private void clear() {
		if (textViewer.getTextWidget() != null && !textViewer.getTextWidget().isDisposed())
			textViewer.getTextWidget().redraw();
	}

	/**
	 * Paints the highlighting of this range.
	 */
	private void paint() {

		IRegion widgetRegion = textViewer
				.modelRange2WidgetRange(new Region(fPosition.getOffset(), fPosition.getLength()));
		int offset = widgetRegion.getOffset();
		int length = widgetRegion.getLength();

		int count = textViewer.getTextWidget().getCharCount();
		if (offset + length >= count) {
			length = count - offset; // clip

			Point upperLeft = textViewer.getTextWidget().getLocationAtOffset(offset);
			Point lowerRight = textViewer.getTextWidget().getLocationAtOffset(offset + length);
			int width = textViewer.getTextWidget().getClientArea().width;
			int height = textViewer.getTextWidget().getLineHeight(offset + length) + lowerRight.y - upperLeft.y;
			textViewer.getTextWidget().redraw(upperLeft.x, upperLeft.y, width, height, false);
		}

		textViewer.getTextWidget().redrawRange(offset, length, true);
	}

	/*
	 * @see ITextListener#textChanged(TextEvent)
	 * 
	 * @since 2.0
	 */
	public void textChanged(TextEvent event) {
		if (event.getViewerRedrawState())
			paint();
	}

	/*
	 * @see IPositionUpdater#update(DocumentEvent)
	 * 
	 * @since 2.0
	 */
	public void update(DocumentEvent event) {
		int offset = event.getOffset();
		int length = event.getLength();
		int delta = event.getText().length() - length;

		if (offset < fPosition.getOffset())
			fPosition.setOffset(fPosition.getOffset() + delta);
		else if (offset < fPosition.getOffset() + fPosition.getLength())
			fPosition.setLength(fPosition.getLength() + delta);
	}
}
