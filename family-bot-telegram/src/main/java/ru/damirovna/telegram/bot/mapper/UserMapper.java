package ru.damirovna.telegram.bot.mapper;

import ru.damirovna.telegram.bot.constants.BotProcess;
import ru.damirovna.telegram.bot.model.UserData;
import ru.damirovna.telegram.core.model.User;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static UserData mapToUserData(User user) {
        UserData userData = new UserData();
        if (user.getCurrentProcess() != null) {
            userData.setCurrentProcess(BotProcess.valueOf(user.getCurrentProcess()));
        }
        if (user.getLocation() != null) {
            userData.setLocation(LocationMapper.mapToBotLocation(user.getLocation()));
        }
        userData.setName(userData.getName());
        userData.setChatId(user.getChatId());
        if (user.getLastWeather() != null) {
            userData.setLastWeatherMessage(WeatherMapper.mapToWeatherMessage(user.getLastWeather()));
        }
        userData.setTimeForMessages(user.getTimeForMessages());
        return userData;
    }

    public static User mapToUser(UserData userData) throws ParseException, IOException {
        User user = new User();
        if (userData.getCurrentProcess() != null) {
            user.setCurrentProcess(userData.getCurrentProcess().name());
        }
        if (userData.getLocation() != null) {
            user.setLocation(LocationMapper.mapToCoreLocation(userData.getLocation()));
        }
        user.setName(userData.getName());
        user.setChatId(userData.getChatId());
        if (userData.getLastWeatherMessage() != null) {
            user.setLastWeather(WeatherMapper.mapToWeather(userData.getLastWeatherMessage()));
        }
        if (userData.getTimeForMessages() != null) {
            user.setTimeForMessages(userData.getTimeForMessages());
        }
        return user;
    }

    public static List<UserData> mapToUserDataList(List<User> list) {
        List<UserData> result = new ArrayList<>();
        for (User user : list) {
            result.add(mapToUserData(user));
        }
        return result;
    }
}
