package org.minesweeper.vision;

/**
 * Перечисление возможных состояний клетки после распознавания.
 */
public enum CellState {
    UNKNOWN("Неизвестно", '?'),
    CLOSED("Закрыта", '?'),
    EMPTY("Пусто", '.'),
    FLAG("Флаг", 'F'),
    MINE("Мина", '*'),
    NUMBER_0("0", '0'),
    NUMBER_1("1", '1'),
    NUMBER_2("2", '2'),
    NUMBER_3("3", '3'),
    NUMBER_4("4", '4'),
    NUMBER_5("5", '5'),
    NUMBER_6("6", '6'),
    NUMBER_7("7", '7'),
    NUMBER_8("8", '8');

    private final String description;
    private final char symbol;

    CellState(String description, char symbol) {
        this.description = description;
        this.symbol = symbol;
    }

    /**
     * Получение описания
     */
    public String getDescription() {
        return description;
    }

    /**
     * Получение символа для отображения
     */
    public char getSymbol() {
        return symbol;
    }

    /**
     * Проверка, является ли состояние цифрой
     */
    public boolean isNumber() {
        return this.ordinal() >= NUMBER_0.ordinal() && this.ordinal() <= NUMBER_8.ordinal();
    }

    /**
     * Получение числового значения (для цифр)
     */
    public int getNumber() {
        if (!isNumber()) {
            throw new IllegalStateException("Состояние не является цифрой: " + this);
        }
        return this.ordinal() - NUMBER_0.ordinal();
    }

    /**
     * Создание состояния цифры из числа
     */
    public static CellState fromNumber(int number) {
        if (number < 0 || number > 8) {
            throw new IllegalArgumentException("Номер должен быть от 0 до 8: " + number);
        }
        return values()[NUMBER_0.ordinal() + number];
    }

    /**
     * Проверка, является ли состояние открытой клеткой
     */
    public boolean isRevealed() {
        return this == EMPTY || isNumber() || this == MINE;
    }

    /**
     * Проверка, является ли состояние безопасным для открытия
     */
    public boolean isSafe() {
        return this == EMPTY || isNumber();
    }

    /**
     * Преобразование в строку
     */
    @Override
    public String toString() {
        return String.valueOf(symbol);
    }
}
