package ru.damirovna.telegram.core.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.damirovna.telegram.core.model.Weather;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WeatherRowMapper implements RowMapper<Weather> {

    @Override
    public Weather mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Weather(
                resultSet.getLong("temperature"),
                resultSet.getLong("feels_like"),
                resultSet.getLong("min_temperature"),
                resultSet.getLong("max_temperature"),
                resultSet.getInt("wind_speed"),
                resultSet.getString("description"),
                resultSet.getString("date_of_creation"),
                null
        );
    }
}
