package ru.damirovna.telegram.bot.mapper;

import ru.damirovna.telegram.bot.model.EventBot;
import ru.damirovna.telegram.core.model.Event;

public class EventMapper {
    public static Event mapToEvent(EventBot eventBot) {
        return new Event(eventBot.getSummary(),
                eventBot.getLocation(),
                eventBot.getStart(),
                eventBot.getEnd()
        );
    }

    public static EventBot mapToEventBot(Event event) {
        EventBot result = new EventBot();
        result.setSummary(event.getSummary());
        result.setEnd(event.getEnd());
        result.setStart(event.getStart());
        result.setLocation(event.getLocation());
        result.setIsInGoogleCalendar((event.getGoogleId() != null) && !event.getGoogleId().isEmpty());
        return result;
    }
}
