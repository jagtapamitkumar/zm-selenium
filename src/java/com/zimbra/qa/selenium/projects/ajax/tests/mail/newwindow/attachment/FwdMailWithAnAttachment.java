package com.zimbra.qa.selenium.projects.ajax.tests.mail.newwindow.attachment;





/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014 Zimbra, Inc.
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


import java.awt.event.KeyEvent;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.FormMailNew;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.SeparateWindowDisplayMail;


public class FwdMailWithAnAttachment extends PrefGroupMailByMessageTest {


	public FwdMailWithAnAttachment() {
		logger.info("New "+ FwdMailWithAnAttachment.class.getCanonicalName());

		//	super.startingAccountPreferences.put("zimbraPrefComposeInNewWindow", "TRUE");


	}

	@Test(	description = "Fwd a mail  with an attachment by pressing Forward button>>attach - in separate window",
			groups = { "windows" })
	public void FwdMailWithAnAttachment_01() throws HarnessException {

		String subject = "subject"+ ZimbraSeleniumProperties.getUniqueString();		


		// Send a message to the account
		ZimbraAccount.AccountA().soapSend(
				"<SendMsgRequest xmlns='urn:zimbraMail'>" +
						"<m>" +
						"<e t='t' a='"+ app.zGetActiveAccount().EmailAddress +"'/>" +
						"<su>"+ subject +"</su>" +
						"<mp ct='text/plain'>" +
						"<content>content"+ ZimbraSeleniumProperties.getUniqueString() +"</content>" +
						"</mp>" +
						"</m>" +
				"</SendMsgRequest>");


		// Refresh current view
		app.zPageMail.zVerifyMailExists(subject);

		// Select the item
		app.zPageMail.zListItem(Action.A_LEFTCLICK, subject);

		FolderItem sent = FolderItem.importFromSOAP(
				app.zGetActiveAccount(), FolderItem.SystemFolder.Sent);

		// Create file item
		final String fileName = "testtextfile.txt";
		final String filePath = ZimbraSeleniumProperties.getBaseDirectory()
				+ "\\data\\public\\other\\" + fileName;

		SeparateWindowDisplayMail window = null;

		try {

			// Choose Actions -> Launch in Window
			//	window = (SeparateWindowFormMailNew) app.zPageMail.zToolbarPressButton(Button.B_NEW_IN_NEW_WINDOW);
			window = (SeparateWindowDisplayMail)app.zPageMail.zToolbarPressPulldown(Button.B_ACTIONS, Button.B_LAUNCH_IN_SEPARATE_WINDOW);

			window.zSetWindowTitle(subject);
			window.zWaitForActive();		// Make sure the window is there

			ZAssert.assertTrue(window.zIsActive(), "Verify the window is active");

			window.zToolbarPressButton(Button.B_FORWARD);
			SleepUtil.sleepMedium();
			window.zSetWindowTitle("Zimbra: Forward");
			SleepUtil.sleepMedium();
			//window.zWaitForActive();
			ZAssert.assertTrue(window.zIsActive(), "Verify the window is active");


			window.sSelectWindow("Zimbra: Forward");
			String locator = FormMailNew.Locators.zToField;
			window.sClick(locator);
			window.sType(locator, ZimbraAccount.AccountB().EmailAddress);
			window.zKeyboard.zTypeKeyEvent(KeyEvent.VK_ENTER);
			SleepUtil.sleepSmall();
			window.zKeyboard.zTypeKeyEvent(KeyEvent.VK_TAB);
			SleepUtil.sleepSmall();			

			window.sSelectWindow("Zimbra: Forward");
			//Add an attachment
			window.zPressButton(Button.B_ATTACH);
			zUpload(filePath);

			//click Send
			window.zToolbarPressButton(Button.B_SEND);			
			window.zSetWindowTitle(subject);
			window.zWaitForActive();
			//close New window
			window.zToolbarPressButton(Button.B_CLOSE);
			SleepUtil.sleepMedium();

			// Window is closed automatically by the client
			window = null;

		} finally {

			// Make sure to close the window
			if ( window != null ) {
				window.zCloseWindow();
				window = null;
			}

		}
		// Verify UI for attachment
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, sent);
		app.zPageMail.zListItem(Action.A_LEFTCLICK, subject);
		ZAssert.assertTrue(app.zPageMail.zVerifyAttachmentExistsInMail(fileName),"Verify attachment exists in the email");

		// From the receiving end, verify the message details
		// Need 'in:inbox' to seprate the message from the sent message
		MailItem received = MailItem.importFromSOAP(ZimbraAccount.AccountB(), "in:inbox subject:("+subject +")");

		ZAssert.assertEquals(received.dFromRecipient.dEmailAddress, app.zGetActiveAccount().EmailAddress, "Verify the from field is correct");
		ZAssert.assertEquals(received.dToRecipients.get(0).dEmailAddress, ZimbraAccount.AccountB().EmailAddress, "Verify the to field is correct");
		ZAssert.assertStringContains(received.dSubject, subject, "Verify the subject field is correct");
		ZAssert.assertStringContains(received.dSubject, "Fwd", "Verify the subject field contains the 'fwd' prefix");
	}

}



