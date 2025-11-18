package ru.damirovna.telegram.bot;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_TIME;

public class BotTest {
    Bot bot = new Bot(System.getenv("BOT_TOKEN"));

    @Test
    public void checkDateGetTimeFormatterTest1() throws ParseException {
        String s = "9:00";
        SimpleDateFormat format = DATE_FORMATTER_GET_TIME;
        Date result = bot.checkDate(s, format);
        assertEquals(format.parse("09:00"), result);
    }

    @Test
    public void checkDateGetTimeFormatterTest2() throws ParseException {
        String s = "09:00";
        SimpleDateFormat format = DATE_FORMATTER_GET_TIME;
        Date result = bot.checkDate(s, format);
        assertEquals(format.parse("09:00"), result);
    }

    @Test
    public void checkDateGetTimeFormatterTest3() {
        String s = "29:00";
        assertThrows(ParseException.class, () -> {
            bot.checkDate(s, DATE_FORMATTER_GET_TIME);
        });
    }


}
