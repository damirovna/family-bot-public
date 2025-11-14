package ru.damirovna.telegram.core.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.damirovna.telegram.core.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_FULL_DATE;

public class EventMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.setTime(DATE_FORMATTER_FULL_DATE.parse(resultSet.getString("start")));
            end.setTime(DATE_FORMATTER_FULL_DATE.parse(resultSet.getString("end")));
            return new Event(
                    resultSet.getInt("id"),
                    resultSet.getInt("parent_id"),
                    resultSet.getString("google_id"),
                    resultSet.getString("summary"),
                    resultSet.getString("location"),
                    start,
                    end
            );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }
}
