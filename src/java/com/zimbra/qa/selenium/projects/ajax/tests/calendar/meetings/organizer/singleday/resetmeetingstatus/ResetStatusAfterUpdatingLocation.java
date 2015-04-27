/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2012, 2013, 2014 Zimbra, Inc.
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
package com.zimbra.qa.selenium.projects.ajax.tests.calendar.meetings.organizer.singleday.resetmeetingstatus;

import java.util.Calendar;

import org.testng.annotations.*;

import com.zimbra.qa.selenium.framework.core.Bugs;
import com.zimbra.qa.selenium.framework.ui.*;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.CalendarWorkWeekTest;
import com.zimbra.qa.selenium.projects.ajax.ui.calendar.FormApptNew;
import com.zimbra.qa.selenium.projects.ajax.ui.calendar.FormApptNew.Field;

public class ResetStatusAfterUpdatingLocation extends CalendarWorkWeekTest {	
	
	public ResetStatusAfterUpdatingLocation() {
		logger.info("New "+ ResetStatusAfterUpdatingLocation.class.getCanonicalName());
		super.startingPage = app.zPageCalendar;
	}
	
	@Bugs(ids = "49881")
	@Test(description = "Check reset status of meeting after Updating location",
			groups = { "functional" })
			
	public void ResetStatusAfterUpdatingLocation_01() throws HarnessException {
		
		// Create a meeting			
		String tz = ZTimeZone.TimeZoneEST.getID();
		String apptSubject = ZimbraSeleniumProperties.getUniqueString();
		String apptAttendee = ZimbraAccount.AccountA().EmailAddress;
		ZimbraResource location1 = new ZimbraResource(ZimbraResource.Type.LOCATION);
		String apptLocation1 = location1.EmailAddress;
		ZimbraResource location2 = new ZimbraResource(ZimbraResource.Type.LOCATION);
		String apptLocation2 = location2.EmailAddress;
		
		// Absolute dates in UTC zone
		Calendar now = this.calendarWeekDayUTC;
		ZDate startUTC = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 14, 0, 0);
		ZDate endUTC   = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 15, 0, 0);
		
		app.zGetActiveAccount().soapSend(
                "<CreateAppointmentRequest xmlns='urn:zimbraMail'>" +
                     "<m>"+
                     	"<inv method='REQUEST' type='event' status='CONF' draft='0' class='PUB' fb='B' transp='O' allDay='0' name='"+ apptSubject +"' loc='"+ apptLocation1+ "'>" +
                     		"<s d='"+ startUTC.toTimeZone(tz).toYYYYMMDDTHHMMSS() +"' tz='"+ tz +"'/>" +
                     		"<e d='"+ endUTC.toTimeZone(tz).toYYYYMMDDTHHMMSS() +"' tz='"+ tz +"'/>" +
                     		"<or a='"+ app.zGetActiveAccount().EmailAddress +"'/>" + 
                     		"<at role='REQ' ptst='NE' rsvp='1' a='" + apptAttendee + "' d='2'/>" +
                     		"<at cutype='RES' a='" + apptLocation1 + "' rsvp='1' role='REQ' url='" + apptLocation1 + "' ptst='AC'/>" +
                     	"</inv>" +
                     	"<e a='"+ ZimbraAccount.AccountA().EmailAddress +"' t='t'/>" +
                     	"<e a='"+ apptLocation1 +"' t='t'/>" +
                     	"<mp content-type='text/plain'>" +
                     		"<content>"+ ZimbraSeleniumProperties.getUniqueString() +"</content>" +
                     	"</mp>" +
                     "<su>"+ apptSubject +"</su>" +
                     "</m>" +
               "</CreateAppointmentRequest>");
        app.zPageCalendar.zToolbarPressButton(Button.B_REFRESH);
        
        //Login as attendee and accept the invite
        app.zPageMain.zLogout();
		app.zPageLogin.zLogin(ZimbraAccount.AccountA());
		app.zPageMail.zToolbarPressButton(Button.B_GETMAIL);
		app.zPageCalendar.zNavigateTo();
		app.zPageCalendar.zListItem(Action.A_RIGHTCLICK, Button.O_ACCEPT_MENU, apptSubject);		
		app.zPageMain.zLogout();			
		
		//Login as current account and modify appointment
		app.zPageLogin.zLogin(ZimbraAccount.AccountZWC());		
		app.zPageCalendar.zNavigateTo();
        
        // Remove location1, add location2 and re-send the appointment
        FormApptNew apptForm = (FormApptNew)app.zPageCalendar.zListItem(Action.A_DOUBLECLICK, apptSubject);
        apptForm.zRemoveLocation(apptLocation1);
        apptForm.zFillField(Field.Location, apptLocation2);
        apptForm.zToolbarPressButton(Button.B_SEND);
        SleepUtil.sleepVeryLong(); 
        
		// --- Check that the organizer shows the attendee as "Needs Action" ---
		app.zGetActiveAccount().soapSend(
				"<SearchRequest xmlns='urn:zimbraMail' types='appointment' calExpandInstStart='"+ startUTC.addDays(-10).toMillis() +"' calExpandInstEnd='"+ endUTC.addDays(10).toMillis() +"'>"
			+		"<query>"+ apptSubject +"</query>"
			+	"</SearchRequest>");
	
		String organizerInvId = app.zGetActiveAccount().soapSelectValue("//mail:appt", "invId");

			app.zGetActiveAccount().soapSend(
				"<GetAppointmentRequest  xmlns='urn:zimbraMail' id='"+ organizerInvId +"'/>");
	
		String attendeeStatus = app.zGetActiveAccount().soapSelectValue("//mail:at[@a='"+ ZimbraAccount.AccountA().EmailAddress +"']", "ptst");

		// Verify attendee status shows as ptst=NE
		ZAssert.assertEquals(attendeeStatus, "NE", "Verify that the attendee shows as 'Needs Action'");
		
		// --- Check that the attendee showing status as "Needs Action" ---
		ZimbraAccount.AccountA().soapSend(
				"<SearchRequest xmlns='urn:zimbraMail' types='appointment' calExpandInstStart='"+ startUTC.addDays(-10).toMillis() +"' calExpandInstEnd='"+ endUTC.addDays(10).toMillis() +"'>"
			+		"<query>"+ apptSubject +"</query>"
			+	"</SearchRequest>");
	
		String attendeeInvId = ZimbraAccount.AccountA().soapSelectValue("//mail:appt", "invId");

		ZimbraAccount.AccountA().soapSend(
				"<GetAppointmentRequest  xmlns='urn:zimbraMail' id='"+ attendeeInvId +"'/>");
	
		String myStatus = ZimbraAccount.AccountA().soapSelectValue("//mail:at[@a='"+ ZimbraAccount.AccountA().EmailAddress +"']", "ptst");

		// Verify attendee status shows as ptst=NE
		ZAssert.assertEquals(myStatus, "NE", "Verify that the attendee shows as 'Needs Action'");
		
		
        
		
	}
	
}