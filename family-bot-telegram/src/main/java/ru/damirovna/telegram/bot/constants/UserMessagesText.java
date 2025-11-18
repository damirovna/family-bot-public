package ru.damirovna.telegram.bot.constants;


import lombok.Data;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_DAY_WITH_YEAR;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;

@Data
public class UserMessagesText {
    public static final String DO_NOT_UNDERSTAND_MSG = "Прошу прощения, я вас не понимаю";

    public static final String DATE_PARSE_ERROR = "Введён некорректный формат даты, повторите попытку ввода";

    public static final String HELLO_MSG = "Погнали!";
    public static final String TIME_VALUE_MSG = "Установленное время оповещений: ";
    public static final String ENTER_TIME_MSG = "Введите время оповещений: ";
    public static final String ENTER_EVENT_SUMMARY_MSG = "Введите название события";
    public static final String ENTER_EVENT_START_DATE = "Введите дату начала события: ";
    public static final String ENTER_EVENT_START_NEW_DATE = "Введите дату начала события в формате " + DATE_FORMATTER_GET_DAY_WITH_YEAR.toPattern() + ":";

    public static final String ENTER_EVENT_START = "Введите время начала события в формате: " + DATE_FORMATTER_GET_TIME.toPattern();
    public static final String ENTER_EVENT_END = "Введите время конца события в формате: " + DATE_FORMATTER_GET_TIME.toPattern();
    public static final String ENTER_EVENT_LOCATION = "Введите локацию события в текстовом формате";
    public static final String ENTER_GOOGLE_CALENDAR_SAVING = "Сохранить в гугл календарь?";
    public static final String EDIT_NEW_EVENT = "Какое поле вы хотели бы изменить?";

    public static final String DO_YOU_WANT_CHANGE_TIME_MSG = "Хотите установить другое время?";
    public static final String DO_YOU_WANT_CREATE_EVENT_MSG = "Хотите создать событие: \n";
    public static final String NEW_EVENT_CREATION_IS_CANCELLED = "Создание нового события было отменено";

    public static final String LOCATION_IS_EMPTY = "Для получения данных о погоде, пожалуйста, отправьте боту свою Геопозицию";

    public static final String LOCATION_SAVING_ERROR = "При сохранении локации в базу данных произошла ошибка";

    public static final String LOCATION_SAVING_OK = "Ваша локация сохранена, название локации: ";

    public static final String WEATHER_SAVING_ERROR = "При сохранении погоды в базу данных произошла ошибка";

    public static final String USER_SAVING_ERROR = "При сохранении пользователя в базу данных произошла ошибка";
    public static final String WEATHER_ERROR = "Простите, в данное время не могу получить данные о погоде в вашем местоположении";

    public static final String USER_KEY_ERROR = "Ошибка размещения ключа клиента";
    public static final String GOOGLE_AUTH_ERROR = "Ошибка авторизации в гугл";

    public static final String EVENT_SAVING_ERROR = "При сохранении событий в базу данных произошла ошибка";
    public static final String EVENT_SAVING_SUCCESS = "Событие создано успешно";


}
