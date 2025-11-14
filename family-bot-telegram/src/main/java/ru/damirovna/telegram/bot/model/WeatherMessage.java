package ru.damirovna.telegram.bot.model;

import lombok.Data;

import java.util.Date;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_DAY;

@Data
public class WeatherMessage {
    private long temp;
    private long feelsLike;
    private long tempMin;
    private long tempMax;
    private int windSpeed;

    private String description;

    private String date;

    private String locationName;

    public WeatherMessage(long temp, long feelsLike, long tempMin, long tempMax, int windSpeed, String description, String date, String locationName) {
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.windSpeed = windSpeed;
        this.description = description;
        this.date = date;
        this.locationName = locationName;
    }


    public boolean isActual() {
        if ((date == null) || (date.isEmpty())) {
            return false;
        }
        return DATE_FORMATTER_GET_DAY.format(new Date()).equals(date);
    }

    @Override
    public String toString() {
        return "Погода на сегодня " + date + " в " + locationName + "\n" +
                "Погода сейчас: " + description + "\n" +
                "Текущая температура: " + temp + "\n" +
                "Ощущается как: " + feelsLike + "\n" +
                "Температура сегодня от " + tempMin + " до " + tempMax + "\n" +
                "Скорость ветра: " + windSpeed;
    }


}
