package ru.damirovna.telegram.bot.model;

import lombok.Data;
import ru.damirovna.telegram.bot.constants.BotProcess;

import java.util.Date;
import java.util.concurrent.Future;

@Data
public class UserData {
    private Long chatId;
    private Date timeForMessages;
    private Location location;

    private BotProcess currentProcess;

    private WeatherMessage lastWeatherMessage;

    private String name;

    private Future<?> notification;

    public UserData() {
        currentProcess = BotProcess.WAIT;
    }
}
