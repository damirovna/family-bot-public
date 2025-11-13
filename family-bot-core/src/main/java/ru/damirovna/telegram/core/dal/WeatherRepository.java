package ru.damirovna.telegram.core.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.damirovna.telegram.core.model.Weather;

import java.sql.SQLException;
import java.util.Optional;

public class WeatherRepository extends BaseRepository<Weather> {
    private static final String INSERT_QUERY = "INSERT INTO public.weather(\n" +
            "\tdescription, temperature, max_temperature, min_temperature, wind_speed, date_of_creation, location_id, feels_like)\n" +
            "\tVALUES (?, ?, ?, ?, ?, ?, ?, ?) returning location_id";
    private static final String FIND_BY_LOCATION = "SELECT \n" +
            "\tw.*, l.location_name \n" +
            "FROM \n" +
            "\tweather w\n" +
            "\tleft outer join locations l\n" +
            "\t\ton w.location_id =l.id\n" +
            "WHERE location_id =?";

    private static final String UPDATE_QUERY = "UPDATE public.weather\n" +
            "\tSET description=?, temperature=?, max_temperature=?, min_temperature=?, wind_speed=?, date_of_creation=?, feels_like=?\n" +
            "\tWHERE location_id=?";

    public WeatherRepository(JdbcTemplate jdbc, RowMapper<Weather> mapper) {
        super(jdbc, mapper);
    }

    public void save(Weather weather, int locationId) throws SQLException {
        Optional<Weather> findWeather = getWeatherByLocation(locationId);
        if (!findWeather.isEmpty()) {
            update(UPDATE_QUERY,
                    weather.getDescription(),
                    weather.getTemp(),
                    weather.getTempMax(),
                    weather.getTempMin(),
                    weather.getWindSpeed(),
                    weather.getDate(),
                    weather.getFeelsLike(),
                    locationId
            );
            return;
        }
        insert(
                INSERT_QUERY,
                weather.getDescription(),
                weather.getTemp(),
                weather.getTempMax(),
                weather.getTempMin(),
                weather.getWindSpeed(),
                weather.getDate(),
                locationId,
                weather.getFeelsLike()
        );
    }

    public Optional<Weather> getWeatherByLocation(int locationId) {
        return findOne(FIND_BY_LOCATION, locationId);
    }
}
