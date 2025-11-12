package ru.damirovna.telegram.bot.mapper;

import ru.damirovna.telegram.bot.model.WeatherMessage;
import ru.damirovna.telegram.core.model.Weather;

import java.text.ParseException;

import static ru.damirovna.telegram.common.Constants.DATA_FORMATTER_GET_DAY;

public class WeatherMapper {
    public static WeatherMessage mapToWeatherMessage(Weather weather) {
        return new WeatherMessage(weather.getTemp(), weather.getFeelsLike(), weather.getTempMin(), weather.getTempMax(), weather.getWindSpeed(), weather.getDescription(), weather.getDate(), weather.getLocationName());
    }

    public static Weather mapToWeather(WeatherMessage weatherMessage) throws ParseException {
        return new Weather(weatherMessage.getTemp(), weatherMessage.getFeelsLike(), weatherMessage.getTempMin(), weatherMessage.getTempMax(), weatherMessage.getWindSpeed(), weatherMessage.getDescription(), DATA_FORMATTER_GET_DAY.parse(weatherMessage.getDate()), weatherMessage.getLocationName());
    }


}
