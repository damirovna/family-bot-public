package ru.damirovna.telegram.bot.model;


import ru.damirovna.telegram.common.DayOfWeek;
import ru.damirovna.telegram.core.model.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static ru.damirovna.telegram.common.Constants.DATA_FORMATTER_GET_TIME;


public class EventMessage {
    private final List<Event> events;

    public EventMessage(List<Event> events) {
        this.events = events;
    }

    public String getMessageForWeek() {
        if ((events == null) || (events.isEmpty())) {
            return "Нет запланированных событий";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Количество событий на ближайшую неделю: ");
        sb.append(events.size());
        sb.append("\n");

        Calendar c = Calendar.getInstance();
        int currentDayOfWeek = -1;
        for (Event e : events) {
            Date start = e.getStart();
            Date end = e.getEnd();
            c.setTime(start);
            if (currentDayOfWeek != c.get(Calendar.DAY_OF_WEEK)) {
                sb.append("<b>");
                sb.append(DayOfWeek.values()[c.get(Calendar.DAY_OF_WEEK) - 1].getNameOfWeek());
                sb.append("</b>");
                sb.append("\n");
                currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            }
            sb.append("<u>");
            sb.append(e.getSummary());
            sb.append("</u>");
            sb.append("\n");
            sb.append(DATA_FORMATTER_GET_TIME.format(start));
            sb.append(" - ");
            sb.append(DATA_FORMATTER_GET_TIME.format(end));
            sb.append("\n");
            if (e.getLocation() != null) {
                sb.append("\uD83D\uDCCD <i>");
                sb.append(e.getLocation());
                sb.append("</i>\n");
            }
        }
        return sb.toString();
    }
}
