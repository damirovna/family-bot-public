package ru.damirovna.telegram.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

import static ru.damirovna.telegram.bot.constants.BotCommands.*;

public class EditEventKeyboard {
    public static ReplyKeyboardMarkup getKeyboard() {
        var row1 = new KeyboardRow();
        var row2 = new KeyboardRow();
        var row3 = new KeyboardRow();
        var row4 = new KeyboardRow();
        row1.add(EDIT_EVENT_SUMMARY);
        row1.add(EDIT_EVENT_DATE);
        row2.add(EDIT_EVENT_START);
        row2.add(EDIT_EVENT_END);
        row3.add(EDIT_EVENT_LOCATION);
        row3.add(EDIT_EVENT_IN_GOOGLE_CALENDAR);
        row4.add(CANCEL_NEW_EVENT_CREATION);
        return ReplyKeyboardMarkup.builder()
                .keyboard(Arrays.asList(row1, row2, row3, row4))
                .resizeKeyboard(false)
                .build();
    }

}
