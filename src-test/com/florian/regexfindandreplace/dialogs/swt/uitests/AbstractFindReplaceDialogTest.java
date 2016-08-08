package com.florian.regexfindandreplace.dialogs.swt.uitests;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.florian.regexfindandreplace.dialogs.swt.FindReplaceDialog;

public abstract class AbstractFindReplaceDialogTest {

	protected SWTBot bot;

	protected static Thread uiThread;

	protected static Shell shell;
	protected static FindReplaceDialog findReplaceDialog;
	protected static FindReplaceDialogWrapper findReplaceDialogWrapper;

	private static IFindReplaceTarget fTarget;
	private static boolean isTargetEditable;
	private static boolean initializeFindString;

	private final static CyclicBarrier swtBarrier = new CyclicBarrier(2);

	@BeforeClass
	public static void setupApp() {
		PropertyConfigurator.configure("log4j.properties");

		System.out.println("BeforeClass is executed!");
		if (uiThread == null) {
			uiThread = new Thread(new Runnable() {

				@Override
				public void run() {
					Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
						public void run() {
							try {
								while (true) {
									// open and layout the shell
									shell = new Shell();
									shell.setLayout(new GridLayout());
									Button openDialogButton = new Button(shell, SWT.PUSH);
									openDialogButton.setText("Open dialog");
									openDialogButton.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(SelectionEvent e) {
											findReplaceDialog = new FindReplaceDialog(true, shell);
											findReplaceDialog.create();
											findReplaceDialogWrapper = new FindReplaceDialogWrapper(findReplaceDialog);
											findReplaceDialog.open();
										}
									});
									Button updateTargetButton = new Button(shell, SWT.PUSH);
									updateTargetButton.setText("Update target");
									updateTargetButton.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(SelectionEvent e) {
											findReplaceDialog.updateTarget(fTarget, isTargetEditable,
													initializeFindString);
										}
									});
									shell.open();
									shell.layout();

									// wait for the test setup
									swtBarrier.await();

									Display display = Display.getDefault();
									while (!shell.isDisposed()) {
										if (!display.readAndDispatch()) {
											display.sleep();
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("Here an exception occured!");
							}

						}
					});
				}
			});
			uiThread.setDaemon(true);
			uiThread.start();
		}
	}

	@Before
	public final void setupSWTBot() throws InterruptedException, BrokenBarrierException {
		// synchronize with the thread opening the shell
		System.out.println("Before is executed!");
		swtBarrier.await();
		findReplaceDialog = null;
		bot = new SWTBot(shell);
	}

	protected void openFindReplaceDialog() {
		System.out.println("Open find replace dialog!");
		SWTBotButton openDialogButton = bot.button("Open dialog");
		openDialogButton.click();
	}

	protected void updateTarget(IFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString) {
		this.fTarget = target;
		this.isTargetEditable = isTargetEditable;
		this.initializeFindString = initializeFindString;
		SWTBotButton updateTargetButton = bot.button("Update target");
		updateTargetButton.click();

	}

	@After
	public void closeShell() throws InterruptedException {
		// close the shell
		System.out.println("After is executed!");
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				System.out.println("Shell is closed!");
				shell.close();
			}
		});
	}

}