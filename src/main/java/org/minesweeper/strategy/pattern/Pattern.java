package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

/**
 * Интерфейс для всех детекторов паттернов.
 */
public interface Pattern {
    /**
     * Поиск паттерна на доске
     * @param board игровое поле
     * @return Move или null, если паттерн не найден
     */
    Move detect(Cell[][] board);

    /**
     * Название паттерна
     */
    String getName();

    /**
     * Описание паттерна
     */
    String getDescription();
}