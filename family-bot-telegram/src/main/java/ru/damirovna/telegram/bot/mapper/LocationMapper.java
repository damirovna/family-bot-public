package ru.damirovna.telegram.bot.mapper;

import ru.damirovna.telegram.bot.model.Location;

import java.io.IOException;

public class LocationMapper {
    public static Location mapToBotLocation(ru.damirovna.telegram.core.model.Location location) {
        return new Location(location.getLongitude(), location.getLatitude());
    }

    public static ru.damirovna.telegram.core.model.Location mapToCoreLocation(Location location) throws IOException {
        return new ru.damirovna.telegram.core.model.Location(location.getLongitude(), location.getLatitude());
    }
}
