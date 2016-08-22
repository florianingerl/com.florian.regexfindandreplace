/*******************************************************************************
 * Copyright (c) 2016 Florian Ingerl.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florian Ingerl, imelflorianingerl@gmail.com - initial API and implementation
 *******************************************************************************/

package com.florian.regexfindandreplace.dialogs.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class HelpDialog extends Dialog {

	public HelpDialog(Shell parentShell) {
		super(parentShell);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());

		Browser browser = new Browser(container, SWT.NONE);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		browser.setLayoutData(gd);
		browser.setText(
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
						+ "<html><head>" + "<title></title>" + "  <style type=\"text/css\">"
						+ "    <!--code { font-family: Courier New, Courier; font-size: 10pt; margin: 0px; }-->"
						+ "  </style>"
						+ "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
						+ "</head><body>" + "<!-- ======================================================== -->"
						+ "<!-- = Java Sourcecode to HTML automatically converted code = -->"
						+ "<!-- =   Java2Html Converter 5.0 [2006-02-26] by Markus Gebhard  markus@jave.de   = -->"
						+ "<!-- =     Further information: http://www.java2html.de     = -->"
						+ "<div align=\"left\" class=\"java\">"
						+ "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bgcolor=\"#ffffff\">"
						+ "  <!-- start headline -->" + "   <tr>" + "    <td colspan=\"2\">"
						+ "     <center><font size=\"+2\">" + "      <code><b>" + "IMatchEvaluator.java"
						+ "      </b></code>" + "     </font></center>" + "    </td>" + "   </tr>"
						+ "  <!-- end headline -->" + "   <tr>" + "  <!-- start source code -->"
						+ "   <td nowrap=\"nowrap\" valign=\"top\" align=\"left\">" + "    <code>"
						+ "<font color=\"#808080\">1</font>&nbsp;<font color=\"#7f0055\"><b>package&nbsp;</b></font><font color=\"#000000\">com.florian.regexfindandreplace;</font><br />"
						+ "<font color=\"#808080\">2</font>&nbsp;<font color=\"#ffffff\"></font><br />"
						+ "<font color=\"#808080\">3</font>&nbsp;<font color=\"#7f0055\"><b>import&nbsp;</b></font><font color=\"#000000\">java.util.regex.*;</font><br />"
						+ "<font color=\"#808080\">4</font>&nbsp;<font color=\"#ffffff\"></font><br />"
						+ "<font color=\"#808080\">5</font>&nbsp;<font color=\"#7f0055\"><b>public&nbsp;interface&nbsp;</b></font><font color=\"#000000\">IMatchEvaluator&nbsp;</font><font color=\"#000000\">{</font><br />"
						+ "<font color=\"#808080\">6</font>&nbsp;<font color=\"#ffffff\"></font><br />"
						+ "<font color=\"#808080\">7</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;</font><font color=\"#000000\">String&nbsp;evaluateMatch</font><font color=\"#000000\">(</font><font color=\"#000000\">MatchResult&nbsp;match</font><font color=\"#000000\">)&nbsp;</font><font color=\"#7f0055\"><b>throws&nbsp;</b></font><font color=\"#000000\">Exception;</font><br />"
						+ "<font color=\"#808080\">8</font>&nbsp;<font color=\"#000000\">}</font></code>" + "    "
						+ "   </td>" + "  <!-- end source code -->" + "   </tr>" + "</table>" + "</div>"
						+ "<div align=\"left\" class=\"java\">"
						+ "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bgcolor=\"#ffffff\">"
						+ "  <!-- start headline -->" + "   <tr>" + "    <td colspan=\"2\">"
						+ "     <center><font size=\"+2\">" + "      <code><b>" + "RegexUtils.java"
						+ "      </b></code>" + "     </font></center>" + "    </td>" + "   </tr>"
						+ "  <!-- end headline -->" + "   <tr>" + "  <!-- start source code -->"
						+ "   <td nowrap=\"nowrap\" valign=\"top\" align=\"left\">" + "    <code>"
						+ "<font color=\"#808080\">01</font>&nbsp;<font color=\"#7f0055\"><b>package&nbsp;</b></font><font color=\"#000000\">com.florian.regexfindandreplace;</font><br />"
						+ "<font color=\"#808080\">02</font>&nbsp;<font color=\"#ffffff\"></font><br />"
						+ "<font color=\"#808080\">03</font>&nbsp;<font color=\"#7f0055\"><b>import&nbsp;</b></font><font color=\"#000000\">java.util.regex.Matcher;</font><br />"
						+ "<font color=\"#808080\">04</font>&nbsp;<font color=\"#7f0055\"><b>import&nbsp;</b></font><font color=\"#000000\">java.util.regex.Pattern;</font><br />"
						+ "<font color=\"#808080\">05</font>&nbsp;<font color=\"#ffffff\"></font><br />"
						+ "<font color=\"#808080\">06</font>&nbsp;<font color=\"#7f0055\"><b>public&nbsp;class&nbsp;</b></font><font color=\"#000000\">RegexUtils&nbsp;</font><font color=\"#000000\">{</font><br />"
						+ "<font color=\"#808080\">07</font>&nbsp;<font color=\"#ffffff\"></font><br />"
						+ "<font color=\"#808080\">08</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;</font><font color=\"#7f0055\"><b>public&nbsp;static&nbsp;</b></font><font color=\"#000000\">String&nbsp;replaceAll</font><font color=\"#000000\">(</font><font color=\"#000000\">String&nbsp;input,&nbsp;String&nbsp;regex,&nbsp;IMatchEvaluator&nbsp;evaluator,&nbsp;</font><font color=\"#7f0055\"><b>int&nbsp;</b></font><font color=\"#000000\">flags&nbsp;</font><font color=\"#000000\">)&nbsp;</font><font color=\"#7f0055\"><b>throws&nbsp;</b></font><font color=\"#000000\">Exception</font><br />"
						+ "<font color=\"#808080\">09</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;</font><font color=\"#000000\">{</font><br />"
						+ "<font color=\"#808080\">10</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">Pattern&nbsp;pattern&nbsp;=&nbsp;Pattern.compile</font><font color=\"#000000\">(</font><font color=\"#000000\">regex,&nbsp;flags</font><font color=\"#000000\">)</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">11</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">Matcher&nbsp;matcher&nbsp;=&nbsp;pattern.matcher</font><font color=\"#000000\">(</font><font color=\"#000000\">input</font><font color=\"#000000\">)</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">12</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><br />"
						+ "<font color=\"#808080\">13</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#7f0055\"><b>int&nbsp;</b></font><font color=\"#000000\">index&nbsp;=&nbsp;</font><font color=\"#990000\">0</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">14</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">StringBuilder&nbsp;sb&nbsp;=&nbsp;</font><font color=\"#7f0055\"><b>new&nbsp;</b></font><font color=\"#000000\">StringBuilder</font><font color=\"#000000\">()</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">15</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#7f0055\"><b>while</b></font><font color=\"#000000\">(</font><font color=\"#000000\">matcher.find</font><font color=\"#000000\">())</font><br />"
						+ "<font color=\"#808080\">16</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">{</font><br />"
						+ "<font color=\"#808080\">17</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">sb.append</font><font color=\"#000000\">(</font><font color=\"#000000\">input.substring</font><font color=\"#000000\">(</font><font color=\"#000000\">index,&nbsp;matcher.start</font><font color=\"#000000\">()))</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">18</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">index&nbsp;=&nbsp;matcher.end</font><font color=\"#000000\">()</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">19</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">sb.append</font><font color=\"#000000\">(</font><font color=\"#000000\">evaluator.evaluateMatch</font><font color=\"#000000\">(</font><font color=\"#000000\">matcher.toMatchResult</font><font color=\"#000000\">()))</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">20</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">}</font><br />"
						+ "<font color=\"#808080\">21</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#000000\">sb.append</font><font color=\"#000000\">(</font><font color=\"#000000\">input.substring</font><font color=\"#000000\">(</font><font color=\"#000000\">index</font><font color=\"#000000\">))</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">22</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><br />"
						+ "<font color=\"#808080\">23</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color=\"#7f0055\"><b>return&nbsp;</b></font><font color=\"#000000\">sb.toString</font><font color=\"#000000\">()</font><font color=\"#000000\">;</font><br />"
						+ "<font color=\"#808080\">24</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;</font><font color=\"#000000\">}</font><br />"
						+ "<font color=\"#808080\">25</font>&nbsp;<font color=\"#ffffff\">&nbsp;&nbsp;</font><br />"
						+ "<font color=\"#808080\">26</font>&nbsp;<font color=\"#000000\">}</font></code>" + "    "
						+ "   </td>" + "  <!-- end source code -->" + "   </tr>" + "</table>" + "</div>"
						+ "<!-- =       END of automatically generated HTML code       = -->"
						+ "<!-- ======================================================== -->" + "</body></html>");
		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Help dialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 800);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
