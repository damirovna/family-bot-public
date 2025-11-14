package ru.damirovna.telegram.core.model;

import lombok.Data;

import java.util.Calendar;

@Data
public class Event {

    private int id;
    private int parentId;
    private String googleId;
    private String summary;
    private String location;

    private Calendar start;

    private Calendar end;


    public Event(int id, int parentId, String googleId, String summary, String location, Calendar start, Calendar end) {
        this.id = id;
        this.parentId = parentId;
        this.googleId = googleId;
        this.summary = summary;
        this.location = location;
        this.start = start;
        this.end = end;
    }

    public Event() {
    }

    public Event(String summary, String location, Calendar start, Calendar end) {
        this.summary = summary;
        this.location = location;
        this.start = start;
        this.end = end;
    }

    public static Event getEventByGoogleEvent(com.google.api.services.calendar.model.Event e) {
        Event event = new Event();
        event.summary = e.getSummary();
        event.location = e.getLocation();
        event.start = Calendar.getInstance();
        event.end = Calendar.getInstance();
        event.googleId = e.getId();
        if (e.getStart().getDateTime() == null) {
            event.start.setTimeInMillis(e.getStart().getDate().getValue());
            event.end.setTimeInMillis(e.getEnd().getDate().getValue());
        } else {
            event.start.setTimeInMillis(e.getStart().getDateTime().getValue());
            event.end.setTimeInMillis(e.getEnd().getDateTime().getValue());
        }
        return event;
    }
}
