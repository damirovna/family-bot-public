package ru.damirovna.telegram.common;

public enum DayOfWeek {
    SUNDAY("Воскресенье"), MONDAY("Понедельник"), TUESDAY("Вторник"), WEDNESDAY("Среда"), THURSDAY("Четверг"), FRIDAY("Пятница"), SATURDAY("Суббота");

    private final String nameOfWeek;

    DayOfWeek(String nameOfWeek) {
        this.nameOfWeek = nameOfWeek;
    }

    public String getNameOfWeek() {
        return nameOfWeek;
    }
}
