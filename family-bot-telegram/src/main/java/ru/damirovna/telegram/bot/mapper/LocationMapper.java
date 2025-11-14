package ru.damirovna.telegram.bot.mapper;

import ru.damirovna.telegram.bot.model.LocationBot;

import java.io.IOException;

public class LocationMapper {
    public static LocationBot mapToBotLocation(ru.damirovna.telegram.core.model.Location location) {
        return new LocationBot(location.getLongitude(), location.getLatitude());
    }

    public static ru.damirovna.telegram.core.model.Location mapToCoreLocation(LocationBot location) throws IOException {
        return new ru.damirovna.telegram.core.model.Location(location.getLongitude(), location.getLatitude());
    }
}
