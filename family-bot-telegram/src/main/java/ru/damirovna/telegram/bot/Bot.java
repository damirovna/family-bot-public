package ru.damirovna.telegram.bot;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.damirovna.telegram.bot.keyboards.ChooseDateKeyboard;
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
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_DAY_WITH_YEAR;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;

public final class Bot extends TelegramLongPollingCommandBot {

    private static final String botToken = System.getenv("BOT_TOKEN");
    private static final String botName = System.getenv("BOT_NAME");
    @Getter
    private static final Map<Long, UserData> userDataMap = new HashMap<>();
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Bot.class);
    private final UserManager userManager = new UserManager();
    private final EventManager eventManager = new EventManager();
    private final Set<Long> fullAccessChatsId = Set.of(278706331L, 43681888L);

    public Bot(String botToken) {
        super(botToken);
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdown, "Shutdown-thread"));
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot(Bot.botToken));
        } catch (TelegramApiException e) {
            log.error("Telegram Api Error", e);
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
        if (!fullAccessChatsId.contains(chatId)) {
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
            case SET_EVENT_START_DATE -> {
                fillEventStartDate(userData, messageText);
                return;
            }
            case SET_EVENT_NEW_DATE -> {
                fillEventStartNewDate(userData, messageText);
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
            case GET_EVENTS -> getEvents(chatId, 7);
            case ADD_NEW_EVENT -> createNewEvent(userData);
            case EDIT_EVENT_SUMMARY -> setEventBotProcess(userData, BotProcess.SET_EVENT_SUMMARY);
            case EDIT_EVENT_DATE -> setEventBotProcess(userData, BotProcess.SET_EVENT_START_DATE);
            case EDIT_EVENT_START -> setEventBotProcess(userData, BotProcess.SET_EVENT_START_TIME);
            case EDIT_EVENT_END -> setEventBotProcess(userData, BotProcess.SET_EVENT_END_TIME);
            case EDIT_EVENT_LOCATION -> setEventBotProcess(userData, BotProcess.SET_EVENT_LOCATION);
            case EDIT_EVENT_IN_GOOGLE_CALENDAR -> setEventBotProcess(userData, BotProcess.SET_GOOGLE_CALENDAR_SAVING);
            case CANCEL_NEW_EVENT_CREATION -> setEventBotProcess(userData, BotProcess.WAIT);
            default -> sendMessage(chatId, DO_NOT_UNDERSTAND_MSG, MainKeyboard.getKeyboard());
        }

    }

    private void setEventBotProcess(UserData userData, BotProcess botProcess) {
        userData.setCurrentProcess(botProcess);
        switch (botProcess) {
            case SET_EVENT_SUMMARY -> sendMessage(userData.getChatId(), ENTER_EVENT_SUMMARY_MSG, null);
//            TODO: разделить задние даты и времени Сделать кнопки завтра/послезавтра? другое время
            case SET_EVENT_START_DATE ->
                    sendMessage(userData.getChatId(), ENTER_EVENT_START_DATE, ChooseDateKeyboard.getKeyboard());
            case SET_EVENT_NEW_DATE -> sendMessage(userData.getChatId(), ENTER_EVENT_START_NEW_DATE, null);
            case SET_EVENT_START_TIME -> sendMessage(userData.getChatId(), ENTER_EVENT_START, null);
            case SET_EVENT_END_TIME -> sendMessage(userData.getChatId(), ENTER_EVENT_END, null);
            case SET_EVENT_LOCATION -> sendMessage(userData.getChatId(), ENTER_EVENT_LOCATION, null);
            case SET_GOOGLE_CALENDAR_SAVING ->
                    sendMessage(userData.getChatId(), ENTER_GOOGLE_CALENDAR_SAVING, VerifyKeyboard.getKeyboard());
            case VERIFY_EVENT_CREATION -> {
                sendMessage(userData.getChatId(), DO_YOU_WANT_CREATE_EVENT_MSG + userData.getNewEvent().toString(), VerifyKeyboard.getKeyboard());
                log.debug("Create new Event :\n" + userData.getNewEvent().toString());
            }
            case WAIT -> {
                userData.setNewEvent(null);
                sendMessage(userData.getChatId(), NEW_EVENT_CREATION_IS_CANCELLED, MainKeyboard.getKeyboard());
            }
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
            end.set(Calendar.HOUR_OF_DAY, newDate.getHours());
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
            log.warn("Invalid new event end time", e);
        }
    }

    private void fillEventStart(UserData userData, String messageText) {
        try {
            Calendar start = (Calendar) userData.getNewEvent().getStart().clone();
            Date newDate = checkDate(messageText, DATE_FORMATTER_GET_TIME);
            start.set(Calendar.HOUR_OF_DAY, newDate.getHours());
            start.set(Calendar.MINUTE, newDate.getMinutes());
            userData.getNewEvent().setStart(start);
            if (userData.getNewEvent().getEnd() == null) {
                setEventBotProcess(userData, BotProcess.SET_EVENT_END_TIME);
            } else {
                setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
            }

        } catch (ParseException e) {
            sendMessage(userData.getChatId(), DATE_PARSE_ERROR, null);
            log.warn("Invalid new event start time", e);
        }
    }

    private void fillEventStartNewDate(UserData userData, String messageText) {
        try {
            Date newDate = checkDate(messageText, DATE_FORMATTER_GET_DAY_WITH_YEAR);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(newDate);
            userData.getNewEvent().setStart(calendar);
            if (userData.getNewEvent().getEnd() == null) {
                setEventBotProcess(userData, BotProcess.SET_EVENT_START_TIME);
            } else {
                setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
            }
        } catch (ParseException e) {
            sendMessage(userData.getChatId(), DATE_PARSE_ERROR, null);
            log.warn("Invalid new event start date", e);
        }
    }

    private void fillEventStartDate(UserData userData, String messageText) {
        Calendar calendar = Calendar.getInstance();
        if (messageText.startsWith(TODAY)) {
            userData.getNewEvent().setStart(calendar);
            setEventBotProcess(userData, BotProcess.SET_EVENT_START_TIME);
            return;
        }
        if (messageText.startsWith(TOMORROW)) {
            calendar.add(Calendar.DATE, 1);
            userData.getNewEvent().setStart(calendar);
            setEventBotProcess(userData, BotProcess.SET_EVENT_START_TIME);
            return;
        }
        if (messageText.startsWith(AFTER_TOMORROW)) {
            calendar.add(Calendar.DATE, 2);
            userData.getNewEvent().setStart(calendar);
            setEventBotProcess(userData, BotProcess.SET_EVENT_START_TIME);
            return;
        }
        if (messageText.equals(ENTER_DATE)) {
            setEventBotProcess(userData, BotProcess.SET_EVENT_NEW_DATE);
            return;
        }
        sendMessage(userData.getChatId(), DO_NOT_UNDERSTAND_MSG, null);

    }

    private void fillEventSummary(UserData userData, String messageText) {
        userData.getNewEvent().setSummary(messageText);
        if (userData.getNewEvent().getStart() == null) {
            setEventBotProcess(userData, BotProcess.SET_EVENT_START_DATE);
        } else {
            setEventBotProcess(userData, BotProcess.VERIFY_EVENT_CREATION);
        }

    }

    private void createNewEvent(UserData userData) {
        userData.setNewEvent(new EventBot());
        setEventBotProcess(userData, BotProcess.SET_EVENT_SUMMARY);
    }

    public void sendDailyNotifications(UserData userData) {
        if (userData.getTimeForMessages() == null) {
            return;
        }
        if (userData.getNotification() != null) {
            userData.getNotification().cancel(false);
        }
        Calendar now = Calendar.getInstance();
        Calendar timeForNotifications = Calendar.getInstance();
        timeForNotifications.setTime(userData.getTimeForMessages());
        int hour = timeForNotifications.get(Calendar.HOUR_OF_DAY);
        int minutes = timeForNotifications.get(Calendar.MINUTE);
        Calendar nextNotificationTime = Calendar.getInstance();
        nextNotificationTime.set(Calendar.HOUR_OF_DAY, hour);
        nextNotificationTime.set(Calendar.MINUTE, minutes);
        if (nextNotificationTime.before(now)) {
            nextNotificationTime.add(Calendar.DATE, 1);
        }
        ScheduledFuture<?> future = service.scheduleWithFixedDelay(new SendNotificationsTask(userData.getChatId()), (nextNotificationTime.getTimeInMillis() - now.getTimeInMillis()), 86400000L, TimeUnit.MILLISECONDS);
        userData.setNotification(future);
//        userDataMap.put(chatId, userData);
    }

    private void setTime(UserData userData) {
        Long chatId = userData.getChatId();
        if (userData.getTimeForMessages() != null) {
            sendMessage(chatId, TIME_VALUE_MSG + DATE_FORMATTER_GET_TIME.format(userData.getTimeForMessages()), MainKeyboard.getKeyboard());
            sendMessage(chatId, DO_YOU_WANT_CHANGE_TIME_MSG, VerifyKeyboard.getKeyboard());
            userData.setCurrentProcess(BotProcess.VERIFY_TIME);
            userDataMap.put(chatId, userData);
        } else {
            userData.setCurrentProcess(BotProcess.SET_TIME);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, ENTER_TIME_MSG, MainKeyboard.getKeyboard());
        }

    }

    private void getEvents(Long chatId, int daysCount) {
        EventManager manager = new EventManager();
        try {
            List<Event> eventList = manager.getEvents(daysCount);
            EventsMessage message = new EventsMessage(eventList);
            sendMessage(chatId, message.getMessageForWeek(), MainKeyboard.getKeyboard());
            log.info("Get events, events count: {}", eventList.size());
        } catch (IOException e) {
            sendMessage(chatId, USER_KEY_ERROR, MainKeyboard.getKeyboard());
            log.error("User key error", e);
        } catch (GeneralSecurityException e) {
            sendMessage(chatId, GOOGLE_AUTH_ERROR, MainKeyboard.getKeyboard());
            log.error("Google auth error", e);
        } catch (SQLException e) {
            sendMessage(chatId, EVENT_SAVING_ERROR, MainKeyboard.getKeyboard());
            log.error("Events saving error", e);
        }
    }

    private void getWeather(UserData userData) {
        Long chatId = userData.getChatId();
        if (userData.getLocation() == null) {
            sendMessage(chatId, LOCATION_IS_EMPTY, MainKeyboard.getKeyboard());
            return;
        }
        try {
            if (userData.getLastWeatherMessage() != null) {
                if (userData.getLastWeatherMessage().isActual()) {
                    sendMessage(chatId, userData.getLastWeatherMessage().toString(), MainKeyboard.getKeyboard());
                    log.info("Send weather for {} from saving data", userData.getName());
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
            sendMessage(chatId, weather.toString(), MainKeyboard.getKeyboard());
            log.info("Send weather for {} from api", userData.getName());
        } catch (ParseException e) {
            sendMessage(chatId, WEATHER_ERROR, MainKeyboard.getKeyboard());
            log.warn("Weather incorrect date", e);
        } catch (SQLException e) {
            sendMessage(chatId, USER_SAVING_ERROR, MainKeyboard.getKeyboard());
            log.warn("Saving error", e);
        } catch (IOException e) {
            sendMessage(chatId, USER_SAVING_ERROR, MainKeyboard.getKeyboard());
            log.warn("Can't get location name from yandex map service", e);
        }
    }

    private void saveLocation(UserData userData, Location location) {
        Long chatId = userData.getChatId();
        LocationBot newLocation = new LocationBot(location.getLongitude(), location.getLatitude());
        userData.setLocation(newLocation);
        try {
            ru.damirovna.telegram.core.model.Location coreLocation = LocationMapper.mapToCoreLocation(newLocation);
            userManager.saveLocation(UserMapper.mapToUser(userData), coreLocation);
            sendMessage(chatId, LOCATION_SAVING_OK + coreLocation.getName(), MainKeyboard.getKeyboard());
            log.info("Save new location: {} for user {}", coreLocation.getName(), userData.getName());
        } catch (SQLException | IOException | ParseException e) {
            sendMessage(chatId, LOCATION_SAVING_ERROR, MainKeyboard.getKeyboard());
            log.warn("New Location saving error", e);
        }
        userDataMap.put(chatId, userData);

    }

    public Date checkDate(String s, SimpleDateFormat format) throws ParseException {
        if (format.toPattern().contains("HH")) {
            s = (s.indexOf(':') == 2) ? s : "0" + s;
        }
        Date newDate = format.parse(s);
        if (format.format(newDate).equals(s)) {
            return newDate;
        } else {
            throw new ParseException(s + " is bad String for pattern " + format.toPattern(), 0);
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
            sendMessage(chatId, TIME_VALUE_MSG + DATE_FORMATTER_GET_TIME.format(newDate), MainKeyboard.getKeyboard());
            sendDailyNotifications(userData);
            log.info("Set new notification time: " + time + " for user " + userData.getName());
        } catch (ParseException e) {
            sendMessage(chatId, DATE_PARSE_ERROR, null);
            log.warn("Invalid notifications time", e);

        } catch (SQLException | IOException e) {
            sendMessage(chatId, USER_SAVING_ERROR, MainKeyboard.getKeyboard());
            log.warn("User saving error", e);
        }

    }

    private void verify(UserData userData, boolean answer) {
        Long chatId = userData.getChatId();
        BotProcess currentProcess = userData.getCurrentProcess();
        if (BotProcess.VERIFY_TIME.equals(currentProcess) && answer) {
            userData.setCurrentProcess(BotProcess.SET_TIME);
            userDataMap.put(chatId, userData);
            sendMessage(chatId, ENTER_TIME_MSG, MainKeyboard.getKeyboard());
            return;
        }
        if (BotProcess.VERIFY_TIME.equals(currentProcess) && (userData.getTimeForMessages() != null && !answer)) {
            sendMessage(chatId, TIME_VALUE_MSG + DATE_FORMATTER_GET_TIME.format(userData.getTimeForMessages()), MainKeyboard.getKeyboard());
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
                    sendMessage(userData.getChatId(), EVENT_SAVING_SUCCESS, MainKeyboard.getKeyboard());
                    log.info("Success new event creation");
                } catch (SQLException | IOException | GeneralSecurityException e) {
                    sendMessage(userData.getChatId(), EVENT_SAVING_ERROR, MainKeyboard.getKeyboard());
                    log.warn("New event saving error", e);
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
        if ((userData.getTimeForMessages() != null) && (userData.getNotification() == null)) {
            sendDailyNotifications(userData);
        }
        return userData;
    }

    private void start(UserData userData) {
        sendMessage(userData.getChatId(), HELLO_MSG, MainKeyboard.getKeyboard());
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
            log.error("Telegram send message Api Error ", e);
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
            log.info("Start scheduled action: send notification for " + userDataMap.get(chatId).getName());
            getWeather(userDataMap.get(chatId));
            getEvents(chatId, 1);
        }
    }
}
