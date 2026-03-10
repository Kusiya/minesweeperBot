package org.minesweeper.strategy;

import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;

/**
 * Интерфейс для всех стратегий игры
 */
public interface Strategy {
    /**
     * Вычислить следующий ход
     * @param state текущее состояние игры
     * @return ход или null, если ходов нет
     */
    Move nextMove(GameState state);

    /**
     * @return название стратегии
     */
    String getName();
}