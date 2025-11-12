package ru.damirovna.telegram.bot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static ru.damirovna.telegram.bot.constants.BotCommands.NO;
import static ru.damirovna.telegram.bot.constants.BotCommands.YES;

public class VerifyKeyboard {
    public static ReplyKeyboardMarkup getKeyboard() {
        var row1 = new KeyboardRow();
        row1.add(YES);
        row1.add(NO);
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .resizeKeyboard(false)
                .build();
    }
}
