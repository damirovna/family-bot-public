package ru.damirovna.telegram.bot.model;

import lombok.Data;


@Data
public class LocationBot {

    private int id;
    private double longitude;
    private double latitude;

    public LocationBot(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
