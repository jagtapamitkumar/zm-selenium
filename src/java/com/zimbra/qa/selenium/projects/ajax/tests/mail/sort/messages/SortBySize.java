/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011, 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.mail.sort.messages;

import java.util.List;
import org.testng.annotations.Test;
import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.items.FolderItem.SystemFolder;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.SetGroupMailByMessagePreference;

public class SortBySize extends SetGroupMailByMessagePreference {

	public SortBySize() {
		logger.info("New "+ SortBySize.class.getCanonicalName());
		super.startingAccountPreferences.put("zimbraPrefReadingPaneLocation", "bottom");
	}


	@Test (description = "Sort a list of messages by size (large->small)",
			groups = { "functional", "L2" })

	public void SortBySize_01() throws HarnessException {

		// Create the message data
		FolderItem inbox = FolderItem.importFromSOAP(app.zGetActiveAccount(), SystemFolder.Inbox);
		String subjectA = "large" + ConfigProperties.getUniqueString();
		String subjectB = "small" + ConfigProperties.getUniqueString();

		app.zGetActiveAccount().soapSend(
				"<AddMsgRequest xmlns='urn:zimbraMail'>"
    		+		"<m l='"+ inbox.getId() +"' f='f'>"
        	+			"<content>From: foo@foo.com\n"
        	+				"To: foo@foo.com \n"
        	+				"Subject: "+ subjectA +"\n"
        	+				"MIME-Version: 1.0 \n"
        	+				"Content-Type: multipart/mixed;\n"
        	+				" boundary=\"=_89a6ebf2-98b6-4041-8c1c-73d961174d7a\"\n"
        	+				"\n"
        	+				"--=_89a6ebf2-98b6-4041-8c1c-73d961174d7a\n"
        	+				"Content-Type: text/plain; charset=utf-8\n"
        	+				"Content-Transfer-Encoding: 7bit\n"
        	+				"\n"
        	+				"simple text string in the body\n"
        	+				"\n"
        	+				"--=_89a6ebf2-98b6-4041-8c1c-73d961174d7a\n"
        	+				"Content-Type: text/plain; name=foobar.txt\n"
        	+				"Content-Disposition: attachment; filename=foobar.txt\n"
        	+				"Content-Transfer-Encoding: base64\n"
        	+				"\n"
        	+				"VG90YWwgdGltZSBmb3Igd2hpY2ggYXBwbGljYXRpb24gdGhyZWFkcyB3ZXJlIHN0b3BwZWQ6IDAu\n"
        	+				"MDAwNzI1MCBzZWNvbmRzCkVSUk9SOiBDb21waWxhdGlvbiBlcnJvcgpqYXZhLmlvLkZpbGVOb3RG\n"
        	+				"Zm9yIHdoaWNoIGFwcGxpY2F0aW9uIHRocmVhZHMgd2VyZSBzdG9wcGVkOiAwLjAwMDEyODAgc2Vj\n"
        	+				"b25kcwo=\n"
        	+				"\n"
        	+				"--=_89a6ebf2-98b6-4041-8c1c-73d961174d7a--\n"
        	+				"\n"
        	+			"</content>"
        	+		"</m>"
			+	"</AddMsgRequest>");

		app.zGetActiveAccount().soapSend(
				"<AddMsgRequest xmlns='urn:zimbraMail'>"
    		+		"<m l='"+ inbox.getId() +"' >"
        	+			"<content>From: foo@foo.com\n"
        	+				"To: foo@foo.com \n"
        	+				"Subject: "+ subjectB +"\n"
        	+				"MIME-Version: 1.0 \n"
        	+				"Content-Type: text/plain; charset=utf-8 \n"
        	+				"Content-Transfer-Encoding: 7bit\n"
        	+				"\n"
        	+				"simple text string in the body\n"
        	+			"</content>"
        	+		"</m>"
			+	"</AddMsgRequest>");

		// Refresh current view
		app.zPageMail.zToolbarPressButton(Button.B_REFRESH);

		// Click on Inbox
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, inbox);

		// First, sort by subject to clear the order
		app.zPageMail.zToolbarPressButton(Button.B_MAIL_LIST_SORTBY_SUBJECT);

		// Now, click on "attachment"
		app.zPageMail.zToolbarPressButton(Button.B_MAIL_LIST_SORTBY_SIZE);

		// Get all the messages in the inbox
		app.zGetActiveAccount().soapSend(
				"<GetPrefsRequest xmlns='urn:zimbraAccount'>"
    		+		"<pref name='zimbraPrefSortOrder'/>"
			+	"</GetPrefsRequest>");

		List<MailItem> messages = app.zPageMail.zListGetMessages();
		ZAssert.assertNotNull(messages, "Verify the message list exists");

		MailItem itemA = null;
		for (MailItem m : messages) {
			if ( subjectA.equals(m.gSubject) ) {
				itemA = m;
			}
			if ( subjectB.equals(m.gSubject) ) {
				ZAssert.assertNotNull(itemA, "Item B is in the list.  Verify Item A has already been found.");
			}
		}

		ZAssert.assertNotNull(itemA, "Verify Item A was found.");
	}


	@Test (description = "Sort a list of messages by size (small->large)",
			groups = { "functional", "L2" })

	public void SortBySize_02() throws HarnessException {

		// Create the message data
		FolderItem inbox = FolderItem.importFromSOAP(app.zGetActiveAccount(), SystemFolder.Inbox);
		String subjectA = "hasAttachment" + ConfigProperties.getUniqueString();
		String subjectB = "noAttachment" + ConfigProperties.getUniqueString();

		app.zGetActiveAccount().soapSend(
				"<AddMsgRequest xmlns='urn:zimbraMail'>"
    		+		"<m l='"+ inbox.getId() +"' f='f'>"
        	+			"<content>From: foo@foo.com\n"
        	+				"To: foo@foo.com \n"
        	+				"Subject: "+ subjectA +"\n"
        	+				"MIME-Version: 1.0 \n"
        	+				"Content-Type: multipart/mixed;\n"
        	+				" boundary=\"=_89a6ebf2-98b6-4041-8c1c-73d961174d7a\"\n"
        	+				"\n"
        	+				"--=_89a6ebf2-98b6-4041-8c1c-73d961174d7a\n"
        	+				"Content-Type: text/plain; charset=utf-8\n"
        	+				"Content-Transfer-Encoding: 7bit\n"
        	+				"\n"
        	+				"simple text string in the body\n"
        	+				"\n"
        	+				"--=_89a6ebf2-98b6-4041-8c1c-73d961174d7a\n"
        	+				"Content-Type: text/plain; name=foobar.txt\n"
        	+				"Content-Disposition: attachment; filename=foobar.txt\n"
        	+				"Content-Transfer-Encoding: base64\n"
        	+				"\n"
        	+				"VG90YWwgdGltZSBmb3Igd2hpY2ggYXBwbGljYXRpb24gdGhyZWFkcyB3ZXJlIHN0b3BwZWQ6IDAu\n"
        	+				"MDAwNzI1MCBzZWNvbmRzCkVSUk9SOiBDb21waWxhdGlvbiBlcnJvcgpqYXZhLmlvLkZpbGVOb3RG\n"
        	+				"Zm9yIHdoaWNoIGFwcGxpY2F0aW9uIHRocmVhZHMgd2VyZSBzdG9wcGVkOiAwLjAwMDEyODAgc2Vj\n"
        	+				"b25kcwo=\n"
        	+				"\n"
        	+				"--=_89a6ebf2-98b6-4041-8c1c-73d961174d7a--\n"
        	+				"\n"
        	+			"</content>"
        	+		"</m>"
			+	"</AddMsgRequest>");

		app.zGetActiveAccount().soapSend(
				"<AddMsgRequest xmlns='urn:zimbraMail'>"
    		+		"<m l='"+ inbox.getId() +"' >"
        	+			"<content>From: foo@foo.com\n"
        	+				"To: foo@foo.com \n"
        	+				"Subject: "+ subjectB +"\n"
        	+				"MIME-Version: 1.0 \n"
        	+				"Content-Type: text/plain; charset=utf-8 \n"
        	+				"Content-Transfer-Encoding: 7bit\n"
        	+				"\n"
        	+				"simple text string in the body\n"
        	+			"</content>"
        	+		"</m>"
			+	"</AddMsgRequest>");

		// Refresh current view
		app.zPageMail.zToolbarPressButton(Button.B_REFRESH);

		// Click on Inbox
		app.zTreeMail.zTreeItem(Action.A_LEFTCLICK, inbox);

		// First, sort by subject to clear the order
		app.zPageMail.zToolbarPressButton(Button.B_MAIL_LIST_SORTBY_SUBJECT);

		// Now, click on "attachment"
		app.zPageMail.zToolbarPressButton(Button.B_MAIL_LIST_SORTBY_SIZE);

		// Now, click on "attachment" (reverse)
		app.zPageMail.zToolbarPressButton(Button.B_MAIL_LIST_SORTBY_SIZE);

		// Get all the messages in the inbox
		app.zGetActiveAccount().soapSend(
				"<GetPrefsRequest xmlns='urn:zimbraAccount'>"
    		+		"<pref name='zimbraPrefSortOrder'/>"
			+	"</GetPrefsRequest>");

		List<MailItem> messages = app.zPageMail.zListGetMessages();
		ZAssert.assertNotNull(messages, "Verify the message list exists");

		MailItem itemB = null;
		for (MailItem m : messages) {
			if ( subjectB.equals(m.gSubject) ) {
				itemB = m;
			}
			if ( subjectA.equals(m.gSubject) ) {
				ZAssert.assertNotNull(itemB, "Item A is in the list.  Verify Item B has already been found.");
			}
		}

		ZAssert.assertNotNull(itemB, "Verify Item B was found.");
	}
}