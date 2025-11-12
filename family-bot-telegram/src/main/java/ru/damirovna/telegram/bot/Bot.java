package ru.damirovna.telegram.bot;

import lombok.Getter;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.damirovna.telegram.bot.constants.BotProcess;
import ru.damirovna.telegram.bot.keyboards.MainKeyboard;
import ru.damirovna.telegram.bot.keyboards.VerifyKeyboard;
import ru.damirovna.telegram.bot.mapper.LocationMapper;
import ru.damirovna.telegram.bot.mapper.UserMapper;
import ru.damirovna.telegram.bot.mapper.WeatherMapper;
import ru.damirovna.telegram.bot.model.EventMessage;
import ru.damirovna.telegram.bot.model.UserData;
import ru.damirovna.telegram.bot.model.WeatherMessage;
import ru.damirovna.telegram.core.model.Event;
import ru.damirovna.telegram.core.model.User;
import ru.damirovna.telegram.core.model.Weather;
import ru.damirovna.telegram.core.service.events.EventManager;
import ru.damirovna.telegram.core.service.user.UserManager;
import ru.damirovna.telegram.core.service.weather.WeatherApiManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static ru.damirovna.telegram.bot.constants.BotCommands.*;
import static ru.damirovna.telegram.bot.constants.UserMessagesText.*;
import static ru.damirovna.telegram.common.Constants.DATA_FORMATTER_GET_TIME;

public final class Bot extends TelegramLongPollingCommandBot {

    private static final String botToken = System.getenv("BOT_TOKEN");
    private static final String botName = System.getenv("BOT_NAME");
    @Getter
    private static final Map<Long, UserData> userDataMap = new HashMap<>();
    private final UserManager userManager = new UserManager();

    public Bot(String botToken) {
        super(botToken);
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot(Bot.botToken));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message message = update.getMessage();
        String messageText = message.getText();
        Long chatId = message.getChatId();
        UserData userData = getUserData(chatId);
        userData.setName(message.getFrom().getUserName());
        userDataMap.put(chatId, userData);
        if (!(chatId.equals(278706331L) || chatId.equals(43681888L))) {
            return;
        }
        if (userDataMap.containsKey(chatId) && BotProcess.SET_TIME.equals(userDataMap.get(chatId).getCurrentProcess())) {
            setTime(chatId, messageText);
            return;
        }
        if (message.hasLocation()) {
            saveLocation(chatId, message.getLocation());
            return;
        }
//        TODO add event creation
        switch (messageText) {
            case START -> start(chatId);
            case SET_TIME -> setTime(chatId);
            case YES -> verify(chatId, true);
            case NO -> verify(chatId, false);
            case GET_WEATHER -> getWeather(chatId);
            case GET_EVENTS -> getEvents(chatId);
            default -> sendMessage(chatId, DO_NOT_UNDERSTAND_MSG, MainKeyboard.getMainKeyboard());
        }

    }

//    TODO add worck with DB
//    TODO add scheduled actions:
//      daily messages
//      daily weatherUpdate

    private void setTime(Long chatId) {
        UserData userData = getUserData(chatId);
        if (userData.getTimeForMessages() != null) {
            sendMessage(chatId, TIME_VALUE_MSG + DATA_FORMATTER_GET_TIME.format(userData.getTimeForMessages()), MainKeyboard.getMainKeyboard());
            sendMessage(chatId, DO_YOU_WANT_CHANGE_TIME_MSG, VerifyKeyboard.getKeyboard());
            userData.setCurrentProcess(BotProcess.VERIFY_TIME);
            userDataMap.put(chatId, userData);
        } else {
            userData.setCurrentProcess(BotProcess.SET_TIME);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, ENTER_TIME_MSG, null);
        }
//        try {
//            userRepository.update(userData);
//        } catch (SQLException e) {
//            sendMessage(chatId, USER_SAVING_ERROR, MainKeyboard.getMainKeyboard());
//        }
    }

    private void getEvents(Long chatId) {
        EventManager manager = new EventManager();
        try {
            List<Event> eventList = manager.getEvents(7);
            EventMessage message = new EventMessage(eventList);
            sendMessage(chatId, message.getMessageForWeek(), MainKeyboard.getMainKeyboard());
        } catch (IOException e) {
            sendMessage(chatId, "Ошибка размещения ключа клиента", MainKeyboard.getMainKeyboard());
        } catch (GeneralSecurityException e) {
            sendMessage(chatId, "Ошибка авторизации в гугл", MainKeyboard.getMainKeyboard());
        }
    }

    private void getWeather(Long chatId) {
        UserData userData = getUserData(chatId);
        if (userData.getLocation() == null) {
            sendMessage(chatId, LOCATION_IS_EMPTY, MainKeyboard.getMainKeyboard());
            return;
        }
        try {
            if (userData.getLastWeatherMessage() != null) {
                if (userData.getLastWeatherMessage().isActual()) {
                    sendMessage(chatId, userData.getLastWeatherMessage().toString(), MainKeyboard.getMainKeyboard());
                    return;
                }
            }
            ru.damirovna.telegram.bot.model.Location location = userData.getLocation();
            WeatherApiManager weatherApiManager = new WeatherApiManager();
            Weather coreWeather = weatherApiManager.getWeather(location.getLongitude(), location.getLatitude());
            WeatherMessage weather = WeatherMapper.mapToWeatherMessage(coreWeather);
            userData.setLastWeatherMessage(weather);
            userDataMap.put(chatId, userData);
            userManager.updateUser(UserMapper.mapToUser(userData));
            sendMessage(chatId, weather.toString(), MainKeyboard.getMainKeyboard());
        } catch (IOException | ParseException | SQLException e) {
            sendMessage(chatId, WEATHER_ERROR, MainKeyboard.getMainKeyboard());
        }
    }

    private void saveLocation(Long chatId, Location location) {
        UserData userData = getUserData(chatId);
        ru.damirovna.telegram.bot.model.Location newLocation = new ru.damirovna.telegram.bot.model.Location(location.getLongitude(), location.getLatitude());
        userData.setLocation(newLocation);
        try {
            ru.damirovna.telegram.core.model.Location coreLocation = LocationMapper.mapToCoreLocation(newLocation);
            userManager.saveLocation(UserMapper.mapToUser(userData), coreLocation);
            sendMessage(chatId, LOCATION_SAVING_OK + coreLocation.getName(), MainKeyboard.getMainKeyboard());
        } catch (Exception e) {
            sendMessage(chatId, LOCATION_SAVING_ERROR, MainKeyboard.getMainKeyboard());
        }
        userDataMap.put(chatId, userData);

    }

    private Date checkDate(String s) throws ParseException {
        s = (s.indexOf(':') == 2) ? s : "0" + s;
        Date newDate = DATA_FORMATTER_GET_TIME.parse(s);
        if (DATA_FORMATTER_GET_TIME.format(newDate).equals(s)) {
            return newDate;
        } else {
            throw new ParseException(s + "is bad String for pattern " + DATA_FORMATTER_GET_TIME.toPattern(), 0);
        }
    }

    private void setTime(Long chatId, String time) {

        UserData userData = getUserData(chatId);
        try {
            Date newDate = checkDate(time);
            userData.setTimeForMessages(newDate);
            userData.setCurrentProcess(BotProcess.WAIT);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, TIME_VALUE_MSG + DATA_FORMATTER_GET_TIME.format(newDate), MainKeyboard.getMainKeyboard());
        } catch (ParseException e) {
            sendMessage(chatId, DO_NOT_UNDERSTAND_MSG, MainKeyboard.getMainKeyboard());
        }

    }

    private void verify(Long chatId, boolean answer) {
        UserData userData = userDataMap.get(chatId);
        BotProcess currentProcess = userData.getCurrentProcess();
        if (BotProcess.VERIFY_TIME.equals(currentProcess) && answer) {
            userData.setCurrentProcess(BotProcess.SET_TIME);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, ENTER_TIME_MSG, null);
        }
        if (userData.getTimeForMessages() != null && !answer) {
            sendMessage(chatId, TIME_VALUE_MSG + DATA_FORMATTER_GET_TIME.format(userData.getTimeForMessages()), MainKeyboard.getMainKeyboard());
        }

    }

    private UserData getUserData(Long chatId) {
        UserData userData;
        if (userDataMap.containsKey(chatId)) {
            userData = userDataMap.get(chatId);
        } else {
            Optional<User> optionalUser = userManager.getUserByChatId(chatId);
            if (optionalUser.isEmpty()) {
                userData = new UserData();
                userData.setChatId(chatId);
            } else {
                userData = UserMapper.mapToUserData(optionalUser.get());
            }
            userDataMap.put(chatId, userData);
        }
        return userData;
    }

    private void start(Long chatId) {
        getUserData(chatId);
        sendMessage(chatId, HELLO_MSG, MainKeyboard.getMainKeyboard());
    }

    public void sendMessage(Long chatId, String message, ReplyKeyboardMarkup keyboard) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.setParseMode(ParseMode.HTML);
        if (Objects.nonNull(keyboard)) {
            sendMessage.setReplyMarkup(keyboard);
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
