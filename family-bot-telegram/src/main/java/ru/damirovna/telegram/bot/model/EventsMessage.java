package ru.damirovna.telegram.bot.model;


import ru.damirovna.telegram.common.DayOfWeek;
import ru.damirovna.telegram.core.model.Event;

import java.util.Calendar;
import java.util.List;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_DAY;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;


public class EventsMessage {
    private final List<Event> events;

    public EventsMessage(List<Event> events) {
        this.events = events;
    }

    public String getMessageForWeek() {
        if ((events == null) || (events.isEmpty())) {
            return "Нет запланированных событий";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Количество событий: ");
        sb.append(events.size());
        sb.append("\n");

        int currentDayOfWeek = -1;
        for (Event e : events) {
            if (currentDayOfWeek != e.getStart().get(Calendar.DAY_OF_WEEK)) {
                sb.append("\n\n<b>");
                sb.append(DayOfWeek.values()[e.getStart().get(Calendar.DAY_OF_WEEK) - 1].getNameOfWeek());
                sb.append(" (");
                sb.append(DATE_FORMATTER_GET_DAY.format(e.getStart().getTime()));
                sb.append(")");
                sb.append("</b>");
                sb.append("\n\n");
                currentDayOfWeek = e.getStart().get(Calendar.DAY_OF_WEEK);
            }
            sb.append("<u>");
            sb.append(e.getSummary());
            sb.append("</u>");
            sb.append("\n");
            if ((e.getEnd().get(Calendar.DAY_OF_MONTH) - e.getStart().get(Calendar.DAY_OF_MONTH)) > 0) {
                sb.append("Весь день: ");
                sb.append(DATE_FORMATTER_GET_DAY.format(e.getStart().getTime()));
            } else {
                sb.append(DATE_FORMATTER_GET_TIME.format(e.getStart().getTime()));
                sb.append(" - ");
                sb.append(DATE_FORMATTER_GET_TIME.format(e.getEnd().getTime()));
            }
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
