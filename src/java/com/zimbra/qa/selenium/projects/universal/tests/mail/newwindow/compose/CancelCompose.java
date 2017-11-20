/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2012, 2013, 2014, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.selenium.projects.universal.tests.mail.newwindow.compose;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.universal.core.SetGroupMailByMessagePreference;
import com.zimbra.qa.selenium.projects.universal.pages.mail.SeparateWindowFormMailNew;


public class CancelCompose extends SetGroupMailByMessagePreference {

	public CancelCompose() {
		logger.info("New "+ CancelCompose.class.getCanonicalName());
		
		super.startingAccountPreferences.put("zimbraPrefComposeFormat", "text");
		super.startingAccountPreferences.put("zimbraPrefComposeInNewWindow", "TRUE");
		
	}
	
	@Test (description = "Compose a message in a separate window - click Cancel",
			groups = { "smoke", "L1" })
	
	public void CancelCompose_01() throws HarnessException {
		
		SeparateWindowFormMailNew window = null;
		String windowTitle = "Zimbra: Compose";
		
		try {
			
			// Open the new mail form
			window = (SeparateWindowFormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW_IN_NEW_WINDOW);
			
			window.zSetWindowTitle(windowTitle);
			window.zWaitForActive();
			
			ZAssert.assertTrue(window.zIsActive(), "Verify the window is active");
			
			window.zToolbarPressButton(Button.B_CANCEL);
			
			//ZAssert.assertFalse(window.zIsActive(), "Verify the window is closed");
			// get title by calling getTitle() method once it's implemented, 
			// for now just hardcoding window title name
			boolean status = window.zIsClosed(windowTitle);
			ZAssert.assertTrue(status, "Verify the window is closed");

		} finally {
			app.zPageMain.zCloseWindow(window, windowTitle, app);			
		}
		
	}

}