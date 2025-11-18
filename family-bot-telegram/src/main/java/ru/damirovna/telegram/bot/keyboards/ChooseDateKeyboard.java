package ru.damirovna.telegram.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.Calendar;

import static ru.damirovna.telegram.bot.constants.BotCommands.*;
import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_GET_DAY;

public class ChooseDateKeyboard {
    public static ReplyKeyboardMarkup getKeyboard() {
        Calendar calendar = Calendar.getInstance();
        var row1 = new KeyboardRow();
        row1.add(TODAY + DATE_FORMATTER_GET_DAY.format(calendar.getTime()));
        calendar.add(Calendar.DATE, 1);
        row1.add(TOMORROW + DATE_FORMATTER_GET_DAY.format(calendar.getTime()));
        var row2 = new KeyboardRow();
        calendar.add(Calendar.DATE, 1);
        row2.add(AFTER_TOMORROW + DATE_FORMATTER_GET_DAY.format(calendar.getTime()));
        row2.add(ENTER_DATE);
        return ReplyKeyboardMarkup.builder()
                .keyboard(Arrays.asList(row1, row2))
                .resizeKeyboard(false)
                .build();
    }
}
