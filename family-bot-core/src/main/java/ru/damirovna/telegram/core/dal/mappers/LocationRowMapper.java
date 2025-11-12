package ru.damirovna.telegram.core.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.damirovna.telegram.core.model.Location;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationRowMapper implements RowMapper<Location> {

    @Override
    public Location mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Location(resultSet.getInt("id"), resultSet.getDouble("longitude"), resultSet.getDouble("latitude"), resultSet.getString("location_name"));
    }
}
