/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2013, 2014, 2015, 2016 Synacor, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.calendar.mountpoints.viewer.actions;

import java.util.Calendar;
import org.testng.annotations.Test;
import com.zimbra.qa.selenium.framework.core.Bugs;
import com.zimbra.qa.selenium.framework.items.*;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.AjaxCore;

public class VerifyDisabledUI extends AjaxCore {

	public VerifyDisabledUI() {
		logger.info("New "+ VerifyDisabledUI.class.getCanonicalName());
		super.startingPage = app.zPageCalendar;
	}


	@Test (description = "Verify Share Calendar, Reinvite Attendees, Forward, Delete, Move & Tag Appointment right click menus are non-functional on mountpoint appointment (read-only share)",
			groups = { "functional", "L2" })

	public void VerifyDisabledUI_01() throws HarnessException {

		String apptSubject = ConfigProperties.getUniqueString();
		String apptBody = ConfigProperties.getUniqueString();
		String foldername = "folder" + ConfigProperties.getUniqueString();
		String mountpointname = "mountpoint" + ConfigProperties.getUniqueString();

		Calendar now = Calendar.getInstance();
		ZDate startUTC = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 11, 0, 0);
		ZDate endUTC   = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 12, 0, 0);

		FolderItem calendarFolder = FolderItem.importFromSOAP(ZimbraAccount.Account5(), FolderItem.SystemFolder.Calendar);

		// Create a folder to share
		ZimbraAccount.Account5().soapSend(
					"<CreateFolderRequest xmlns='urn:zimbraMail'>"
				+		"<folder name='" + foldername + "' l='" + calendarFolder.getId() + "' view='appointment'/>"
				+	"</CreateFolderRequest>");

		FolderItem folder = FolderItem.importFromSOAP(ZimbraAccount.Account5(), foldername);

		// Share it
		ZimbraAccount.Account5().soapSend(
					"<FolderActionRequest xmlns='urn:zimbraMail'>"
				+		"<action id='"+ folder.getId() +"' op='grant'>"
				+			"<grant d='"+ app.zGetActiveAccount().EmailAddress +"' gt='usr' perm='r' view='appointment'/>"
				+		"</action>"
				+	"</FolderActionRequest>");

		// Mount it
		app.zGetActiveAccount().soapSend(
					"<CreateMountpointRequest xmlns='urn:zimbraMail'>"
				+		"<link l='1' name='"+ mountpointname +"'  rid='"+ folder.getId() +"' zid='"+ ZimbraAccount.Account5().ZimbraId +"' view='appointment' color='5'/>"
				+	"</CreateMountpointRequest>");

		// Create appointment
		ZimbraAccount.Account5().soapSend(
				"<CreateAppointmentRequest xmlns='urn:zimbraMail'>"
				+		"<m l='"+ folder.getId() +"' >"
				+			"<inv method='REQUEST' type='event' status='CONF' draft='0' class='PUB' fb='B' transp='O' allDay='0' name='"+ apptSubject +"'>"
				+				"<s d='"+ startUTC.toTimeZone(ZTimeZone.getLocalTimeZone().getID()).toYYYYMMDDTHHMMSS() +"' tz='"+ ZTimeZone.getLocalTimeZone().getID() +"'/>"
				+				"<e d='"+ endUTC.toTimeZone(ZTimeZone.getLocalTimeZone().getID()).toYYYYMMDDTHHMMSS() +"' tz='"+ ZTimeZone.getLocalTimeZone().getID() +"'/>"
				+				"<or a='"+ ZimbraAccount.Account5().EmailAddress +"'/>"
				+				"<at role='REQ' ptst='NE' rsvp='1' a='" + app.zGetActiveAccount().EmailAddress + "'/>"
				+			"</inv>"
				+			"<e a='"+ app.zGetActiveAccount().EmailAddress +"' t='t'/>"
				+			"<su>"+ apptSubject +"</su>"
				+			"<mp content-type='text/plain'>"
				+				"<content>" + apptBody + "</content>"
				+			"</mp>"
				+		"</m>"
				+	"</CreateAppointmentRequest>");

		// Mark ON/OFF to calendar folders
		app.zTreeCalendar.zMarkOnOffCalendarFolder("Calendar");
		app.zTreeCalendar.zMarkOnOffCalendarFolder(mountpointname);

		// Verify appointment exists in current view
        ZAssert.assertTrue(app.zPageCalendar.zVerifyAppointmentExists(apptSubject), "Verify appointment displayed in current view");

		// Verify Forward, Delete, Move & Tag Appointment menus are disabled
		app.zPageCalendar.zListItem(Action.A_RIGHTCLICK, apptSubject);
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_REINVITE_ATTENDEES_DISABLED), "Verify 'Reinvite Attendees' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_FORWARD_DISABLED), "Verify 'Forward' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_DELETE_DISABLED), "Verify 'Delete' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_MOVE_DISABLED), "Verify 'Move' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_TAG_APPOINTMENT_DISABLED), "Verify 'Tag Appointment' menu is disabled");

		// Verify Share Calendar menu is disabled
		app.zTreeCalendar.zTreeItem(Action.A_RIGHTCLICK, mountpointname);
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_SHARE_CALENDAR_DISABLED), "Verify 'Share Calendar' menu is disabled");

		// Mark ON/OFF to calendar folders
		app.zTreeCalendar.zMarkOnOffCalendarFolder("Calendar");
		app.zTreeCalendar.zMarkOnOffCalendarFolder(mountpointname);

	}


	@Bugs (ids = "99947")
	@Test (description = "Verify Share Calendar, Reinvite Attendees, Forward, Delete, Move & Tag Appointment right click menus are non-functional on mountpoint appointment (read-only share)",
			groups = { "functional-skip", "application-bug" })

	public void VerifyDisabledUI_02() throws HarnessException {

		String apptSubject = ConfigProperties.getUniqueString();
		String apptBody = ConfigProperties.getUniqueString();
		String foldername = "folder" + ConfigProperties.getUniqueString();
		String mountpointname = "mountpoint" + ConfigProperties.getUniqueString();

		Calendar now = Calendar.getInstance();
		ZDate startUTC = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 12, 0, 0);
		ZDate endUTC   = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 13, 0, 0);

		FolderItem calendarFolder = FolderItem.importFromSOAP(ZimbraAccount.Account5(), FolderItem.SystemFolder.Calendar);

		// Create a folder to share
		ZimbraAccount.Account5().soapSend(
					"<CreateFolderRequest xmlns='urn:zimbraMail'>"
				+		"<folder name='" + foldername + "' l='" + calendarFolder.getId() + "' view='appointment'/>"
				+	"</CreateFolderRequest>");

		FolderItem folder = FolderItem.importFromSOAP(ZimbraAccount.Account5(), foldername);

		// Share it
		ZimbraAccount.Account5().soapSend(
					"<FolderActionRequest xmlns='urn:zimbraMail'>"
				+		"<action id='"+ folder.getId() +"' op='grant'>"
				+			"<grant d='"+ app.zGetActiveAccount().EmailAddress +"' gt='usr' perm='r' view='appointment'/>"
				+		"</action>"
				+	"</FolderActionRequest>");

		// Mount it
		app.zGetActiveAccount().soapSend(
					"<CreateMountpointRequest xmlns='urn:zimbraMail'>"
				+		"<link l='1' name='"+ mountpointname +"'  rid='"+ folder.getId() +"' zid='"+ ZimbraAccount.Account5().ZimbraId +"' view='appointment' color='5'/>"
				+	"</CreateMountpointRequest>");

		// Create appointment
		ZimbraAccount.Account5().soapSend(
				"<CreateAppointmentRequest xmlns='urn:zimbraMail'>"
				+		"<m l='"+ folder.getId() +"' >"
				+			"<inv method='REQUEST' type='event' status='CONF' draft='0' class='PRI' fb='B' transp='O' allDay='0' name='"+ apptSubject +"'>"
				+				"<s d='"+ startUTC.toTimeZone(ZTimeZone.getLocalTimeZone().getID()).toYYYYMMDDTHHMMSS() +"' tz='"+ ZTimeZone.getLocalTimeZone().getID() +"'/>"
				+				"<e d='"+ endUTC.toTimeZone(ZTimeZone.getLocalTimeZone().getID()).toYYYYMMDDTHHMMSS() +"' tz='"+ ZTimeZone.getLocalTimeZone().getID() +"'/>"
				+				"<or a='"+ ZimbraAccount.Account5().EmailAddress +"'/>"
				+				"<at role='REQ' ptst='NE' rsvp='1' a='" + ZimbraAccount.Account2().EmailAddress + "'/>"
				+			"</inv>"
				+			"<e a='"+ ZimbraAccount.Account2().EmailAddress +"' t='t'/>"
				+			"<su>"+ apptSubject +"</su>"
				+			"<mp content-type='text/plain'>"
				+				"<content>" + apptBody + "</content>"
				+			"</mp>"
				+		"</m>"
				+	"</CreateAppointmentRequest>");

		// Mark ON/OFF to calendar folders
		app.zTreeCalendar.zMarkOnOffCalendarFolder("Calendar");
		app.zTreeCalendar.zMarkOnOffCalendarFolder(mountpointname);
		app.zPageCalendar.zToolbarPressButton(Button.B_REFRESH);

		// Verify Forward, Delete, Move & Tag Appointment menus are disabled
		app.zPageCalendar.zListItem(Action.A_RIGHTCLICK, apptSubject);

		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_FORWARD_DISABLED), "Verify 'Forward' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_MOVE_DISABLED), "Verify 'Move' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_CREATE_A_COPY_DISABLED), "Verify 'Create a copy' menu is disabled");
		ZAssert.assertTrue(app.zPageCalendar.zVerifyDisabledControl(Button.O_SHOW_ORIGINAL_DISABLED), "Verify 'Show Original' menu is disabled");

	}

}
