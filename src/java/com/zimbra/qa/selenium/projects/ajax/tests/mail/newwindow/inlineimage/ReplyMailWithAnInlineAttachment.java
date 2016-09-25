package com.zimbra.qa.selenium.projects.ajax.tests.mail.newwindow.inlineimage;

/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2016 Synacor, Inc.
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

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.PrefGroupMailByMessageTest;
import com.zimbra.qa.selenium.projects.ajax.ui.mail.SeparateWindowDisplayMail;

public class ReplyMailWithAnInlineAttachment extends PrefGroupMailByMessageTest {

	public ReplyMailWithAnInlineAttachment() {
		logger.info("New "+ ReplyMailWithAnInlineAttachment.class.getCanonicalName());

		super.startingAccountPreferences.put("zimbraPrefComposeFormat", "html");
	}

	@Test( description = "Reply a mail  with an inline attachment by pressing Reply button>>attach>>Inline - in separate window",
			groups = { "smoke" })
	
	public void ReplyMailWithAnInlineAttachment_01() throws HarnessException {

		if (OperatingSystem.isWindows() == true) {

			String subject = "subject"+ ConfigProperties.getUniqueString();
			String bodyText = "text" + ConfigProperties.getUniqueString();
			String bodyHTML = "text <strong>bold"+ ConfigProperties.getUniqueString() +"</strong> text";
			String contentHTML = XmlStringUtil.escapeXml(
					"<html>" +
							"<head></head>" +
							"<body>"+ bodyHTML +"</body>" +
					"</html>");


			// Send a message to the account
			ZimbraAccount.AccountA().soapSend(
					"<SendMsgRequest xmlns='urn:zimbraMail'>" +
							"<m>" +
							"<e t='t' a='"+ app.zGetActiveAccount().EmailAddress +"'/>" +
							"<su>"+ subject +"</su>" +
							"<mp ct='multipart/alternative'>" +
							"<mp ct='text/plain'>" +
							"<content>"+ bodyText +"</content>" +
							"</mp>" +
							"<mp ct='text/html'>" +
							"<content>"+ contentHTML +"</content>" +
							"</mp>" +
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
			final String fileName = "samplejpg.jpg";
			final String filePath = ConfigProperties.getBaseDirectory()+ "\\data\\public\\other\\" + fileName;

			SeparateWindowDisplayMail window = null;
			String windowTitle = "Zimbra: " + subject;

			try {

				// Choose Actions -> Launch in Window
				window = (SeparateWindowDisplayMail)app.zPageMail.zToolbarPressPulldown(Button.B_ACTIONS, Button.B_LAUNCH_IN_SEPARATE_WINDOW);

				window.zSetWindowTitle(windowTitle);
				window.zWaitForActive();

				ZAssert.assertTrue(window.zIsActive(), "Verify the window is active");

				window.zToolbarPressButton(Button.B_REPLY);
				
				windowTitle = "Zimbra: Reply";
				window.zSetWindowTitle(windowTitle);
				ZAssert.assertTrue(window.zIsActive(), "Verify the window is active");

				//Add an attachment
				// Click Attach>>inline image
				window.zPressButton(Button.O_ATTACH_DROPDOWN);
				window.zPressButton(Button.B_ATTACH_INLINE);
				zUploadInlineImageAttachment(filePath);

				ZAssert.assertTrue(app.zPageMail.zVerifyInlineImageAttachmentExistsInComposeWindow(windowTitle, 1),"Verify inline image is present in Reply compose window");

				//click Send
				window.zToolbarPressButton(Button.B_SEND);
				
				windowTitle = "Zimbra: " + subject;
				window.zSetWindowTitle(windowTitle);
				
			} finally {
				app.zPageMain.zCloseWindow(window, windowTitle, app);
			}

			// Verify UI for attachment
			app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, sent);
			app.zPageMail.zListItem(Action.A_LEFTCLICK, subject);
			ZAssert.assertTrue(app.zPageMail.zVerifyInlineImageAttachmentExistsInMail(),"Verify inline attachment exists in the email");

			// From the receiving end, verify the message details
			// Need 'in:inbox' to seprate the message from the sent message
			MailItem received = MailItem.importFromSOAP(ZimbraAccount.AccountA(), "in:inbox subject:("+subject +")");

			ZAssert.assertEquals(received.dFromRecipient.dEmailAddress, app.zGetActiveAccount().EmailAddress, "Verify the from field is correct");
			ZAssert.assertEquals(received.dToRecipients.get(0).dEmailAddress, ZimbraAccount.AccountA().EmailAddress, "Verify the to field is correct");
			ZAssert.assertStringContains(received.dSubject, "Re: " + subject, "Verify reply subject field is correct");

		} else {
			throw new SkipException("File upload operation is allowed only for Windows OS, skipping this test...");
		}
	}

}