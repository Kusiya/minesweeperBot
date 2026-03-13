package org.minesweeper.strategy;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

/**
 * Интерфейс для всех стратегий принятия решений.
 * Позволяет легко добавлять новые стратегии.
 */
public interface Strategy {
    /**
     * Анализ доски и возврат решения
     * @param board текущее состояние игрового поля
     * @param totalMines общее количество мин
     * @return Move или null, если стратегия не может принять решение
     */
    Move analyze(Cell[][] board, int totalMines);

    /**
     * Название стратегии для логирования
     */
    String getName();

    /**
     * Приоритет стратегии (чем выше, тем раньше применяется)
     */
    int getPriority();

    /**
     * Описание стратегии
     */
    default String getDescription() {
        return "Нет описания";
    }

    /**
     * Сброс внутреннего состояния стратегии
     */
    default void reset() {
        // По умолчанию ничего не делаем
    }
}