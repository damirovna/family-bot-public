package ru.damirovna.telegram.bot.model;

import lombok.Data;


@Data
public class Location {

    private int id;
    private double longitude;
    private double latitude;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
