package ru.damirovna.telegram.core.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    private int id;

    private Long chatId;
    private Date timeForMessages;
    private Location location;

    private String currentProcess;

    private String name;

    private Weather lastWeather;
}
