package ru.damirovna.telegram.core.model;

import lombok.Data;

import java.util.Date;

import static ru.damirovna.telegram.common.Constants.DATA_FORMATTER_GET_DAY;

@Data
public class Weather {
    private long temp;
    private long feelsLike;
    private long tempMin;
    private long tempMax;
    private int windSpeed;

    private String description;

    private String date;

    private String locationName;

    public Weather(double temp, double feelsLike, double tempMin, double tempMax, int windSpeed, String description, String locationName) {
        this.temp = Math.round(temp - 273.15);
        this.feelsLike = Math.round(feelsLike - 273.15);
        this.tempMin = Math.round(tempMin - 273.15);
        this.tempMax = Math.round(tempMax - 273.15);
        this.windSpeed = windSpeed;
        this.date = DATA_FORMATTER_GET_DAY.format(new Date());
        this.description = description;
        this.locationName = locationName;
    }

    public Weather(int temp, int feelsLike, int tempMin, int tempMax, double windSpeed, String description, String locationName) {
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.windSpeed = (int) Math.round(windSpeed);
        this.date = DATA_FORMATTER_GET_DAY.format(new Date());
        this.description = description;
        this.locationName = locationName;
    }

    public Weather(long temperature, long feelsLike, long minTemperature, long maxTemperature, int windSpeed, String description, Date dateOfCreation, String locationName) {
        this.temp = temperature;
        this.feelsLike = feelsLike;
        this.tempMin = minTemperature;
        this.tempMax = maxTemperature;
        this.windSpeed = windSpeed;
        this.date = DATA_FORMATTER_GET_DAY.format(dateOfCreation);
        this.description = description;
        this.locationName = locationName;
    }

    public Weather(long temperature, long feelsLike, long minTemperature, long maxTemperature, int windSpeed, String description, String dateOfCreation, String locationName) {
        this.temp = temperature;
        this.feelsLike = feelsLike;
        this.tempMin = minTemperature;
        this.tempMax = maxTemperature;
        this.windSpeed = windSpeed;
        this.date = dateOfCreation;
        this.description = description;
        this.locationName = locationName;
    }


    public boolean isActual() {
        if ((date == null) || (date.isEmpty())) {
            return false;
        }
        return DATA_FORMATTER_GET_DAY.format(new Date()).equals(date);
    }

}
