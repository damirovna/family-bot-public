package ru.damirovna.telegram.core.model;

import lombok.Data;
import ru.damirovna.telegram.core.service.location.LocationApiManager;

import java.io.IOException;

@Data
public class Location {
    private int id;
    private double longitude;
    private double latitude;

    private String name;

    public Location(double longitude, double latitude) throws IOException {
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = LocationApiManager.getLocationName(this);
    }

    public Location(int id, double longitude, double latitude) throws IOException {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = LocationApiManager.getLocationName(this);
    }

    public Location(int id, double longitude, double latitude, String name) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
    }

    public Location(double longitude, double latitude, String name) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
    }
}
