package ru.damirovna.telegram.core.service.weather;

import ru.damirovna.telegram.core.dal.LocationRepository;
import ru.damirovna.telegram.core.dal.WeatherRepository;
import ru.damirovna.telegram.core.dal.mappers.LocationRowMapper;
import ru.damirovna.telegram.core.dal.mappers.WeatherRowMapper;
import ru.damirovna.telegram.core.model.Location;
import ru.damirovna.telegram.core.model.Weather;
import ru.damirovna.telegram.core.service.BaseManager;

import java.sql.SQLException;
import java.util.Optional;

public class WeatherManager extends BaseManager {
    private final WeatherRepository weatherRepository = new WeatherRepository(jdbcTemplate, new WeatherRowMapper());
    private final LocationRepository locationRepository = new LocationRepository(jdbcTemplate, new LocationRowMapper());

    public boolean saveNewWeather(Weather weather) {
        try {
            Optional<Location> locationOptional = locationRepository.getLocationByName(weather.getLocationName());
            if (locationOptional.isEmpty()) {
                return false;
            }
            weatherRepository.save(weather, locationOptional.get().getId());
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Optional<Weather> getWeatherByLocation(String locationName) {
        Optional<Location> locationOptional = locationRepository.getLocationByName(locationName);
        if (locationOptional.isEmpty()) {
            return Optional.empty();
        } else {
            return weatherRepository.getWeatherByLocation(locationOptional.get().getId());
        }
    }


}
