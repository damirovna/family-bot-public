package ru.damirovna.telegram.core.service.events;


import ru.damirovna.telegram.core.model.Event;
import ru.damirovna.telegram.core.service.BaseManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class EventManager extends BaseManager {
    private static final GoogleCalendarManager googleCalendarManager = new GoogleCalendarManager();


    public List<Event> getEvents(int days) throws IOException, GeneralSecurityException {
        List<Event> events = new ArrayList<>();
        List<com.google.api.services.calendar.model.Event> googleEvents = googleCalendarManager.getEvents(days);
        for (com.google.api.services.calendar.model.Event e : googleEvents) {
            events.add(Event.getEventByGoogleEvent(e));
        }
        return events;
    }
}
