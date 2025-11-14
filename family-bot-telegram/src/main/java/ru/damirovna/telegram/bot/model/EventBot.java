package ru.damirovna.telegram.bot.model;

import lombok.Data;

import java.util.Calendar;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_DAY;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;

@Data
public class EventBot {
    private String summary;
    private String location;

    private Calendar start;

    private Calendar end;
    private Boolean isInGoogleCalendar;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<u>");
        sb.append(summary);
        sb.append("</u>");
        sb.append("\n");
        if ((end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH)) > 0) {
            sb.append("Весь день: ");
            sb.append(DATE_FORMATTER_GET_DAY.format(start.getTime()));
        } else {
            sb.append(DATE_FORMATTER_GET_TIME.format(start.getTime()));
            sb.append(" - ");
            sb.append(DATE_FORMATTER_GET_TIME.format(end.getTime()));
        }
        sb.append("\n");
        if ((location != null) && (!location.isBlank())) {
            sb.append("\uD83D\uDCCD <i>");
            sb.append(location);
            sb.append("</i>\n");
        }
        if (isInGoogleCalendar) {
            sb.append("С добавлением в Google календарь?");
        } else {
            sb.append("Без добавления в Google календарь?");
        }
        return sb.toString();
    }
}
