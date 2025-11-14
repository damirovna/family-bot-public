package ru.damirovna.telegram.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

import static ru.damirovna.telegram.bot.constants.BotCommands.*;

public class EditEventKeyboard {
    public static ReplyKeyboardMarkup getKeyboard() {
        var row1 = new KeyboardRow();
        row1.add(EDIT_EVENT_SUMMARY);
        var row2 = new KeyboardRow();
        row2.add(EDIT_EVENT_START);
        var row3 = new KeyboardRow();
        row3.add(EDIT_EVENT_END);
        var row4 = new KeyboardRow();
        row4.add(EDIT_EVENT_LOCATION);
        var row5 = new KeyboardRow();
        row5.add(EDIT_EVENT_IN_GOOGLE_CALENDAR);
        return ReplyKeyboardMarkup.builder()
                .keyboard(Arrays.asList(row1, row2, row3, row4, row5))
                .resizeKeyboard(false)
                .build();
    }
}
