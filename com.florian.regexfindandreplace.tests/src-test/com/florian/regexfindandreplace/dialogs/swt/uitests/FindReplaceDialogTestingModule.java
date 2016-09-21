package com.florian.regexfindandreplace.dialogs.swt.uitests;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import com.florian.regexfindandreplace.IClassPathProvider;
import com.florian.regexfindandreplace.IJavacLocator;
import com.florian.regexfindandreplace.JavacLocator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FindReplaceDialogTestingModule extends AbstractModule {

	private IDialogSettings dialogSettings;
	private IEditorStatusLine editorStatusLine;
	private IJavacLocator javacLocator = null;

	public FindReplaceDialogTestingModule(IDialogSettings dialogSettings, IEditorStatusLine editorStatusLine) {
		this(dialogSettings, editorStatusLine, null);
	}

	public FindReplaceDialogTestingModule(IDialogSettings dialogSettings, IEditorStatusLine editorStatusLine,
			IJavacLocator javacLocator) {
		this.dialogSettings = dialogSettings;
		this.editorStatusLine = editorStatusLine;
		this.javacLocator = javacLocator;
	}

	@Override
	protected void configure() {
		bind(IClassPathProvider.class).toInstance(new IClassPathProvider() {

			@Override
			public String getClassPath() {
				StringBuilder sb = new StringBuilder("\"");
				File base = new File(".");
				base = new File(new File(base.getAbsolutePath()).getParentFile().getParentFile(),
						"com.florian.regexfindandreplace");
				sb.append(new File(new File(base, "lib"), "regex-0.0.1-SNAPSHOT.jar").getAbsolutePath()).append(";");
				sb.append(new File(base, "bin").getAbsolutePath()).append(";");

				sb.append("\"");
				return sb.toString();
			}
		});

	}

	@Provides
	public IDialogSettings getDialogSettings() {
		return dialogSettings;
	}

	@Provides
	public IEditorStatusLine getEditorStatusLine() {
		return editorStatusLine;
	}

	@Provides
	public IJavacLocator getJavacLocator() {
		if (javacLocator == null)
			return new JavacLocator();

		return javacLocator;
	}

}
