package ru.damirovna.telegram.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

import static ru.damirovna.telegram.bot.constants.BotCommands.*;

public class MainKeyboard {
    public static ReplyKeyboardMarkup getMainKeyboard() {
        var row1 = new KeyboardRow();
        row1.add(GET_EVENTS);
        row1.add(GET_WEATHER);
        var row2 = new KeyboardRow();
        row2.add(SET_TIME);
        row2.add(ADD_NEW_EVENT);
        return ReplyKeyboardMarkup.builder()
                .keyboard(Arrays.asList(row1, row2))
                .resizeKeyboard(false)
                .build();
    }
}
