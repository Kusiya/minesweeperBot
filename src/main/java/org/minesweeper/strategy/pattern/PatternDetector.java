package org.minesweeper.strategy.pattern;

import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;

/**
 * Интерфейс для детекторов паттернов
 */
public interface PatternDetector {
    /**
     * Проверить, есть ли паттерн в данной позиции
     * @return ход, соответствующий паттерну, или null
     */
    Move detectPattern(GameState state, int row, int col);

    /**
     * @return название паттерна
     */
    String getPatternName();
}
