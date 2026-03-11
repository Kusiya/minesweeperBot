package org.minesweeper.strategy;

import org.minesweeper.core.Cell;
import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;

/**
 * Базовая стратегия, использующая простые правила
 */
public class BasicStrategy implements Strategy {

    @Override
    public String getName() {
        return "Базовая стратегия";
    }

    @Override
    public Move nextMove(GameState state) {
        // Ищем безопасные клетки для открытия
        Move safeMove = findSafeCellToOpen(state);
        if (safeMove != null) {
            return safeMove;
        }

        // Ищем клетки для флагов
        Move flagMove = findCellToFlag(state);
        if (flagMove != null) {
            return flagMove;
        }
        return null; // нет очевидных ходов
    }

    /**
     * Найти безопасную клетку для открытия
     */
    private Move findSafeCellToOpen(GameState state) {
        for (int i = 0; i < state.getRows(); i++) {
            for (int j = 0; j < state.getCols(); j++) {
                Cell cell = state.getCell(i, j);

                if (!cell.isRevealed()) continue;

                int number = cell.getAdjacentMines();
                if (number <= 0) continue;

                int flags = state.countFlagsAround(i, j);
                int unknown = state.countUnknownAround(i, j);

                // Правило: все мины уже отмечены флагами
                if (number == flags && unknown > 0) {
                    // Находим первую неизвестную клетку вокруг
                    for (Cell neighbor : state.getNeighbors(i, j)) {
                        if (neighbor.isOpenable()) {
                            return new Move(neighbor.getRow(), neighbor.getCol(), false,
                                    0.0, "Все мины вокруг (" + i + "," + j + ") отмечены");
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Найти клетку, где нужно поставить флаг
     */
    private Move findCellToFlag(GameState state) {
        for (int i = 0; i < state.getRows(); i++) {
            for (int j = 0; j < state.getCols(); j++) {
                Cell cell = state.getCell(i, j);

                if (!cell.isRevealed()) continue;

                int number = cell.getAdjacentMines();
                if (number <= 0) continue;

                int flags = state.countFlagsAround(i, j);
                int unknown = state.countUnknownAround(i, j);

                // Правило: все неизвестные клетки должны быть минами
                if (unknown == number - flags && unknown > 0) {
                    for (Cell neighbor : state.getNeighbors(i, j)) {
                        if (neighbor.isOpenable()) {
                            return new Move(neighbor.getRow(), neighbor.getCol(), true,
                                    0.0, "Должна быть мина (клетка " + i + "," + j + ")");
                        }
                    }
                }
            }
        }
        return null;
    }
}
