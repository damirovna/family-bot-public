package ru.damirovna.telegram.bot.constants;


import lombok.Data;

@Data
public class UserMessagesText {
    public static final String DO_NOT_UNDERSTAND_MSG = "Прошу прощения, я вас не понимаю";

    public static final String HELLO_MSG = "Погнали!";
    public static final String TIME_VALUE_MSG = "Установленное время оповещений: ";
    public static final String ENTER_TIME_MSG = "Введите время оповещений: ";
    public static final String DO_YOU_WANT_CHANGE_TIME_MSG = "Хотите установить другое время?";

    public static final String LOCATION_IS_EMPTY = "Для получения данных о погоде, пожалуйста, отправьте боту свою Геопозицию";

    public static final String LOCATION_SAVING_ERROR = "При сохранении локации в базу данных произошла ошибка";

    public static final String LOCATION_SAVING_OK = "Ваша локация сохранена, название локации: ";

    public static final String WEATHER_SAVING_ERROR = "При сохранении погоды в базу данных произошла ошибка";

    public static final String USER_SAVING_ERROR = "При сохранении пользователя в базу данных произошла ошибка";
    public static final String WEATHER_ERROR = "Простите, в данное время не могу получить данные о погоде в вашем местоположении";


}
