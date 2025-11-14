package ru.damirovna.telegram.core.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.damirovna.telegram.core.model.Location;
import ru.damirovna.telegram.core.model.User;
import ru.damirovna.telegram.core.model.Weather;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setChatId(Long.valueOf(resultSet.getString("chat_id")));
        try {
            if (resultSet.getString("date_notification") != null) {
                user.setTimeForMessages(DATE_FORMATTER_GET_TIME.parse(resultSet.getString("date_notification")));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        user.setName(resultSet.getString("name"));
        user.setCurrentProcess(resultSet.getString("current_process"));
        Location location = new Location(resultSet.getInt("location_id"), resultSet.getDouble("longitude"), resultSet.getDouble("latitude"), resultSet.getString("location_name"));
        user.setLocation(location);
        if (resultSet.getString("description") != null) {
            Weather weather = new Weather(
                    resultSet.getLong("temperature"),
                    resultSet.getLong("feels_like"),
                    resultSet.getLong("min_temperature"),
                    resultSet.getLong("max_temperature"),
                    resultSet.getInt("wind_speed"),
                    resultSet.getString("description"),
                    resultSet.getString("date_of_creation"),
                    resultSet.getString("location_name")
            );
            user.setLastWeather(weather);
        }
        return user;
    }
}
