package ru.damirovna.telegram.core.service.events;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleCalendarManager {
    private static final String APPLICATION_NAME = "TelegramBot";
    private static final String CALENDAR_ID = "lp1j9hmj5b3n998f2nv11f18sg@group.calendar.google.com";
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static Calendar calendar;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(System.getenv("HOME") + "/" + System.getenv("PRIVATE_KEY_NAME"));

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private Date plusDaysToDate(Date from, int n) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(from);
        c.add(java.util.Calendar.DATE, n);
        return c.getTime();
    }
//    TODO Add events to DB

    public List<Event> getEvents(int days) throws IOException, GeneralSecurityException {
        if (calendar == null) {
            getCalendar();
        }
        Date now = new Date();
        DateTime from = new DateTime(now);
        DateTime to = new DateTime(plusDaysToDate(now, days));
        Events events = calendar.events().list(CALENDAR_ID)
                .setMaxResults(100)
                .setTimeMin(from)
                .setTimeMax(to)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    //TODO create new Event on bot
    public String addEventToGoogleCalendar(ru.damirovna.telegram.core.model.Event event) throws IOException, GeneralSecurityException {
        if (calendar == null) {
            getCalendar();
        }
        EventDateTime start = new EventDateTime().setDate(new DateTime(event.getStart().getTime()));
        start.setTimeZone(event.getStart().getTimeZone().getDisplayName());
        EventDateTime end = new EventDateTime().setDate(new DateTime(event.getEnd().getTime()));
        end.setTimeZone(event.getEnd().getTimeZone().getDisplayName());

        Event googleEvent = new Event()
                .setSummary(event.getSummary())
                .setLocation(event.getLocation())
                .setStart(start)
                .setEnd(end);
        Event newGoogleEvent = calendar.events().insert(CALENDAR_ID, googleEvent).execute();
        return newGoogleEvent.getId();
    }

    public void getCalendar() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        calendar =
                new Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }
}
