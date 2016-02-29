/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2013, 2014 Zimbra, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.selenium.projects.ajax.tests.mail.compose;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.core.Bugs;
import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew;

public class SendMailWithzimbraPrefUseKeyboardShortcutsFalse extends PrefGroupMailByMessageTest {

	public SendMailWithzimbraPrefUseKeyboardShortcutsFalse() {
		logger.info("New "+ SendMailWithzimbraPrefUseKeyboardShortcutsFalse.class.getCanonicalName());
		super.startingAccountPreferences.put("zimbraPrefComposeFormat", "text");
		super.startingAccountPreferences.put("zimbraPrefUseKeyboardShortcuts", "FALSE");
		
	}
	
	@Bugs(ids = "76547")
	@Test(	description = "Send a mail using Text editor - zimbraPrefUseKeyboardShortcuts = FALSE",
			groups = { "functional" })
	
	public void SendMailWithzimbraPrefUseKeyboardShortcutsFalse_01() throws HarnessException {
		
		// Create the message data to be sent
		String subject = "subject" + ZimbraSeleniumProperties.getUniqueString();
		
		
		// Open the new mail form
		FormMailNew mailform = (FormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW);
		ZAssert.assertNotNull(mailform, "Verify the new form opened");
		
		// Fill out the form with the data
		mailform.zFillField(FormMailNew.Field.Subject, subject);
		mailform.zFillField(FormMailNew.Field.Body, "body" + ZimbraSeleniumProperties.getUniqueString());

		// Selenium JS can't repro the bug.  I suppose the To: field is still enabled
		// at the JS level.  But, clicking manually into the field doesn't work.
		//
		// Maybe WebDriver will be better at repro, since it mimics real usage better.
		//
		mailform.sFocus(FormMailNew.Locators.zToField);
		mailform.zClick(FormMailNew.Locators.zToField);
		mailform.zWaitForBusyOverlay();
		mailform.zKeyboard.zTypeCharacters(ZimbraAccount.AccountA().EmailAddress);
		
		// Send the message
		mailform.zSubmit();

		MailItem received = MailItem.importFromSOAP(ZimbraAccount.AccountA(), "subject:("+ subject +")");
		ZAssert.assertNotNull(received, "Verify the message is received by Account A");
	
	}

}