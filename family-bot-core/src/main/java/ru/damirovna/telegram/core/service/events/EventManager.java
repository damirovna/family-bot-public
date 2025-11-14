package ru.damirovna.telegram.core.service.events;


import ru.damirovna.telegram.core.dal.EventRepository;
import ru.damirovna.telegram.core.dal.mappers.EventMapper;
import ru.damirovna.telegram.core.model.Event;
import ru.damirovna.telegram.core.service.BaseManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventManager extends BaseManager {
    private static final GoogleCalendarManager googleCalendarManager = new GoogleCalendarManager();
    private final EventRepository eventRepository = new EventRepository(jdbcTemplate, new EventMapper());

    public List<Event> getEvents(int days) throws IOException, GeneralSecurityException, SQLException {
        List<Event> events = new ArrayList<>();
        List<Event> eventsInDB = eventRepository.findAll();
        Set<String> set = new HashSet<>();
        for (Event event : eventsInDB) {
            set.add(event.getGoogleId());
        }
        List<com.google.api.services.calendar.model.Event> googleEvents = googleCalendarManager.getEvents(days);
        for (com.google.api.services.calendar.model.Event e : googleEvents) {
            Event coreEvent = Event.getEventByGoogleEvent(e);
            events.add(coreEvent);
            if (!set.contains(e.getId())) {
                eventRepository.save(coreEvent);
            }
        }
        return events;
    }

    public void saveEvent(Event event, boolean doSaveInGoogle) throws SQLException, IOException, GeneralSecurityException {
        if (doSaveInGoogle) {
            String googleId = googleCalendarManager.addEventToGoogleCalendar(event);
            event.setGoogleId(googleId);
        }
        eventRepository.save(event);

    }
}
