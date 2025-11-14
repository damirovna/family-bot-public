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
import ru.damirovna.telegram.bot.keyboards.EditEventKeyboard;
import ru.damirovna.telegram.bot.keyboards.MainKeyboard;
import ru.damirovna.telegram.bot.keyboards.VerifyKeyboard;
import ru.damirovna.telegram.bot.mapper.EventMapper;
import ru.damirovna.telegram.bot.mapper.LocationMapper;
import ru.damirovna.telegram.bot.mapper.UserMapper;
import ru.damirovna.telegram.bot.mapper.WeatherMapper;
import ru.damirovna.telegram.bot.model.*;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static ru.damirovna.telegram.bot.constants.BotCommands.*;
import static ru.damirovna.telegram.bot.constants.UserMessagesText.*;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_FULL_DATE;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;

public final class Bot extends TelegramLongPollingCommandBot {

    private static final String botToken = System.getenv("BOT_TOKEN");
    private static final String botName = System.getenv("BOT_NAME");
    @Getter
    private static final Map<Long, UserData> userDataMap = new HashMap<>();
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final UserManager userManager = new UserManager();
    private final EventManager eventManager = new EventManager();

    public Bot(String botToken) {
        super(botToken);
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdown, "Shutdown-thread"));
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
        switch (userData.getCurrentProcess()) {
            case SET_TIME -> {
                setTime(userData, messageText);
                return;
            }
            case SET_EVENT_SUMMARY -> {
                fillEventSummary(userData, messageText);
                return;
            }
            case SET_EVENT_START_TIME -> {
                fillEventStart(userData, messageText);
                return;
            }
            case SET_EVENT_END_TIME -> {
                fillEventEnd(userData, messageText);
                return;
            }
            case SET_EVENT_LOCATION -> {
                fillEventLocation(userData, messageText);
                return;
            }
        }
        if (message.hasLocation()) {
            saveLocation(userData, message.getLocation());
            return;
        }
//        TODO add event creation
        switch (messageText) {
            case START -> start(userData);
            case SET_TIME -> setTime(userData);
            case YES -> verify(userData, true);
            case NO -> verify(userData, false);
            case GET_WEATHER -> getWeather(userData);
            case GET_EVENTS -> getEvents(chatId);
            case ADD_NEW_EVENT -> createNewEvent(userData);
            case EDIT_EVENT_SUMMARY -> setEventBotProcess(userData, BotProcess.SET_EVENT_SUMMARY);
            case EDIT_EVENT_START -> userData.setCurrentProcess(BotProcess.SET_EVENT_START_TIME);
            case EDIT_EVENT_END -> userData.setCurrentProcess(BotProcess.SET_EVENT_END_TIME);
            case EDIT_EVENT_LOCATION -> userData.setCurrentProcess(BotProcess.SET_EVENT_LOCATION);
            case EDIT_EVENT_IN_GOOGLE_CALENDAR -> userData.setCurrentProcess(BotProcess.SET_GOOGLE_CALENDAR_SAVING);
            default -> sendMessage(chatId, DO_NOT_UNDERSTAND_MSG, MainKeyboard.getMainKeyboard());
        }

    }

    private void setEventBotProcess(UserData userData, BotProcess botProcess) {
        userData.setCurrentProcess(botProcess);
        switch (botProcess) {
            case SET_EVENT_SUMMARY -> sendMessage(userData.getChatId(), ENTER_EVENT_SUMMARY_MSG, null);
            case SET_EVENT_START_TIME -> sendMessage(userData.getChatId(), ENTER_EVENT_START, null);
            case SET_EVENT_END_TIME -> sendMessage(userData.getChatId(), ENTER_EVENT_END, null);
            case SET_EVENT_LOCATION -> sendMessage(userData.getChatId(), ENTER_EVENT_LOCATION, null);
            case SET_GOOGLE_CALENDAR_SAVING ->
                    sendMessage(userData.getChatId(), ENTER_GOOGLE_CALENDAR_SAVING, VerifyKeyboard.getKeyboard());
            case VERIFY_EVENT_CREATION ->
                    sendMessage(userData.getChatId(), DO_YOU_WANT_CREATE_EVENT_MSG + userData.getNewEvent().toString(), VerifyKeyboard.getKeyboard());
        }
    }

    private void fillEventLocation(UserData userData, String messageText) {
        userData.getNewEvent().setLocation(messageText);
        if (userData.getNewEvent().getIsInGoogleCalendar() == null) {
            setEventBotProcess(userData, BotProcess.SET_GOOGLE_CALENDAR_SAVING);
        } else {
            setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
        }
    }

    //TODO можно вводить только время окончания события
    private void fillEventEnd(UserData userData, String messageText) {
        try {
            Calendar end = (Calendar) userData.getNewEvent().getStart().clone();
            Date newDate = checkDate(messageText, DATE_FORMATTER_GET_TIME);
            end.set(Calendar.HOUR, newDate.getHours());
            end.set(Calendar.MINUTE, newDate.getMinutes());
            if (end.before(userData.getNewEvent().getStart())) {
                end.add(Calendar.DATE, 1);
            }
            userData.getNewEvent().setEnd(end);
            if (userData.getNewEvent().getLocation() == null) {
                setEventBotProcess(userData, BotProcess.SET_EVENT_LOCATION);
            } else {
                setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
            }
        } catch (ParseException e) {
            sendMessage(userData.getChatId(), DATE_PARSE_ERROR, null);
        }
    }

    private void fillEventStart(UserData userData, String messageText) {
        try {
            Calendar start = Calendar.getInstance();
            start.setTime(checkDate(messageText, DATE_FORMATTER_FULL_DATE));
            userData.getNewEvent().setStart(start);
            if (userData.getNewEvent().getEnd() == null) {
                setEventBotProcess(userData, BotProcess.SET_EVENT_END_TIME);
            } else {
                setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
            }

        } catch (ParseException e) {
            sendMessage(userData.getChatId(), DATE_PARSE_ERROR, null);
        }
    }

    private void fillEventSummary(UserData userData, String messageText) {
        userData.getNewEvent().setSummary(messageText);
        if (userData.getNewEvent().getStart() == null) {
            setEventBotProcess(userData, BotProcess.SET_EVENT_START_TIME);
        } else {
            setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
        }

    }

    private void createNewEvent(UserData userData) {
        userData.setNewEvent(new EventBot());
        setEventBotProcess(userData, BotProcess.SET_EVENT_SUMMARY);
    }

    public void sendDailyNotifications(Long chatId) {
        UserData userData = getUserData(chatId);
        if (userData.getTimeForMessages() == null) {
            return;
        }
        if (userData.getNotification() != null) {
            userData.getNotification().cancel(false);
        }
        Calendar now = Calendar.getInstance();
        Calendar timeForNotifications = Calendar.getInstance();
        timeForNotifications.setTime(userData.getTimeForMessages());
        int hour = timeForNotifications.get(Calendar.HOUR);
        int minutes = timeForNotifications.get(Calendar.MINUTE);
        Calendar nextNotificationTime = Calendar.getInstance();
        nextNotificationTime.set(Calendar.HOUR, hour);
        nextNotificationTime.set(Calendar.MINUTE, minutes);
        if (nextNotificationTime.before(now)) {
            nextNotificationTime.add(Calendar.DATE, 1);
        }
        ScheduledFuture<?> future = service.scheduleWithFixedDelay(new SendNotificationsTask(chatId), (nextNotificationTime.getTimeInMillis() - now.getTimeInMillis()), 86400000L, TimeUnit.MILLISECONDS);
        userData.setNotification(future);
        userDataMap.put(chatId, userData);
    }

    private void setTime(UserData userData) {
        Long chatId = userData.getChatId();
        if (userData.getTimeForMessages() != null) {
            sendMessage(chatId, TIME_VALUE_MSG + DATE_FORMATTER_GET_TIME.format(userData.getTimeForMessages()), MainKeyboard.getMainKeyboard());
            sendMessage(chatId, DO_YOU_WANT_CHANGE_TIME_MSG, VerifyKeyboard.getKeyboard());
            userData.setCurrentProcess(BotProcess.VERIFY_TIME);
            userDataMap.put(chatId, userData);
        } else {
            userData.setCurrentProcess(BotProcess.SET_TIME);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, ENTER_TIME_MSG, MainKeyboard.getMainKeyboard());
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
            EventsMessage message = new EventsMessage(eventList);
            sendMessage(chatId, message.getMessageForWeek(), MainKeyboard.getMainKeyboard());
        } catch (IOException e) {
            sendMessage(chatId, USER_KEY_ERROR, MainKeyboard.getMainKeyboard());
        } catch (GeneralSecurityException e) {
            sendMessage(chatId, GOOGLE_AUTH_ERROR, MainKeyboard.getMainKeyboard());
        } catch (SQLException e) {
            sendMessage(chatId, EVENT_SAVING_ERROR, MainKeyboard.getMainKeyboard());
        }
    }

    private void getWeather(UserData userData) {
        Long chatId = userData.getChatId();
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
            LocationBot location = userData.getLocation();
            WeatherApiManager weatherApiManager = new WeatherApiManager();
            Weather coreWeather = weatherApiManager.getWeather(location.getLongitude(), location.getLatitude());
            WeatherMessage weather = WeatherMapper.mapToWeatherMessage(coreWeather);
            userData.setLastWeatherMessage(weather);
            userDataMap.put(chatId, userData);
            userManager.updateUser(UserMapper.mapToUser(userData));
            sendMessage(chatId, weather.toString(), MainKeyboard.getMainKeyboard());
        } catch (ParseException e) {
            sendMessage(chatId, WEATHER_ERROR, MainKeyboard.getMainKeyboard());
        } catch (SQLException | IOException e) {
            sendMessage(chatId, USER_SAVING_ERROR, MainKeyboard.getMainKeyboard());
        }
    }

    private void saveLocation(UserData userData, Location location) {
        Long chatId = userData.getChatId();
        LocationBot newLocation = new LocationBot(location.getLongitude(), location.getLatitude());
        userData.setLocation(newLocation);
        try {
            ru.damirovna.telegram.core.model.Location coreLocation = LocationMapper.mapToCoreLocation(newLocation);
            userManager.saveLocation(UserMapper.mapToUser(userData), coreLocation);
            sendMessage(chatId, LOCATION_SAVING_OK + coreLocation.getName(), MainKeyboard.getMainKeyboard());
        } catch (SQLException | IOException | ParseException e) {
            sendMessage(chatId, LOCATION_SAVING_ERROR, MainKeyboard.getMainKeyboard());
        }
        userDataMap.put(chatId, userData);

    }

    private Date checkDate(String s, SimpleDateFormat format) throws ParseException {
        s = (s.indexOf(':') == 2) ? s : "0" + s;
        Date newDate = format.parse(s);
        if (format.format(newDate).equals(s)) {
            return newDate;
        } else {
            throw new ParseException(s + "is bad String for pattern " + format.toPattern(), 0);
        }
    }

    private void setTime(UserData userData, String time) {
        Long chatId = userData.getChatId();
        try {
            Date newDate = checkDate(time, DATE_FORMATTER_GET_TIME);
            userData.setTimeForMessages(newDate);
            userData.setCurrentProcess(BotProcess.WAIT);
            userDataMap.put(chatId, userData);
            userManager.updateUser(UserMapper.mapToUser(userData));
            sendMessage(chatId, TIME_VALUE_MSG + DATE_FORMATTER_GET_TIME.format(newDate), MainKeyboard.getMainKeyboard());
            sendDailyNotifications(chatId);
        } catch (ParseException e) {
            sendMessage(chatId, DATE_PARSE_ERROR, null);
        } catch (SQLException | IOException e) {
            sendMessage(chatId, USER_SAVING_ERROR, MainKeyboard.getMainKeyboard());
        }

    }

    private void verify(UserData userData, boolean answer) {
        Long chatId = userData.getChatId();
        BotProcess currentProcess = userData.getCurrentProcess();
        if (BotProcess.VERIFY_TIME.equals(currentProcess) && answer) {
            userData.setCurrentProcess(BotProcess.SET_TIME);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, ENTER_TIME_MSG, MainKeyboard.getMainKeyboard());
            return;
        }
        if (BotProcess.VERIFY_TIME.equals(currentProcess) && (userData.getTimeForMessages() != null && !answer)) {
            sendMessage(chatId, TIME_VALUE_MSG + DATE_FORMATTER_GET_TIME.format(userData.getTimeForMessages()), MainKeyboard.getMainKeyboard());
            return;
        }
        if (BotProcess.SET_GOOGLE_CALENDAR_SAVING.equals(currentProcess)) {
            userData.getNewEvent().setIsInGoogleCalendar(answer);
            setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
            return;
        }
        if (BotProcess.VERIFY_EVENT_CREATION.equals(currentProcess)) {
            if (answer) {
                try {
                    eventManager.saveEvent(EventMapper.mapToEvent(userData.getNewEvent()), userData.getNewEvent().getIsInGoogleCalendar());
                    userData.setNewEvent(null);
                    sendMessage(userData.getChatId(), EVENT_SAVING_SUCCESS, MainKeyboard.getMainKeyboard());
                } catch (SQLException | IOException | GeneralSecurityException e) {
                    sendMessage(userData.getChatId(), EVENT_SAVING_ERROR, MainKeyboard.getMainKeyboard());
                }
            } else {
                editNewEvent(userData);
            }
        }
    }

    private void editNewEvent(UserData userData) {
        userData.setCurrentProcess(BotProcess.EDIT_NEW_EVENT);
        sendMessage(userData.getChatId(), EDIT_NEW_EVENT, EditEventKeyboard.getKeyboard());
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

    private void start(UserData userData) {
        sendMessage(userData.getChatId(), HELLO_MSG, MainKeyboard.getMainKeyboard());
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

    public class SendNotificationsTask implements Runnable {

        private final Long chatId;

        public SendNotificationsTask(Long chatId) {
            this.chatId = chatId;
        }

        @Override
        public void run() {
            getWeather(userDataMap.get(chatId));
            getEvents(chatId);
        }
    }
}
