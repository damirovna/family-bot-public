package ru.damirovna.telegram.core.model;

import lombok.Data;

import java.util.Date;

@Data
public class Event {
    private String summary;
    private String location;

    private Date start;

    private Date end;


    public static Event getEventByGoogleEvent(com.google.api.services.calendar.model.Event e) {
        Event event = new Event();
        event.summary = e.getSummary();
        event.location = e.getLocation();
        event.start = new Date(e.getStart().getDateTime().getValue());
        event.end = new Date(e.getEnd().getDateTime().getValue());
        return event;
    }


}
