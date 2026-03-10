package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;

/**
 * Детектор паттерна 1-2-1
 *
 * Пример:
 *   ? ? ?
 *   1 2 1
 *
 * Решение: крайние ? - мины, средний - безопасен
 */
public class OneTwoOnePattern implements PatternDetector {

    @Override
    public String getPatternName() {
        return "1-2-1 паттерн";
    }

    @Override
    public Move detectPattern(GameState state, int row, int col) {
        // Проверяем горизонтальный паттерн
        Move horizontal = detectHorizontalPattern(state, row, col);
        if (horizontal != null) return horizontal;

        // Проверяем вертикальный паттерн
        return detectVerticalPattern(state, row, col);
    }

    private Move detectHorizontalPattern(GameState state, int row, int col) {
        // Проверяем, что в центре цифра 2
        if (!isNumber(state, row, col, 2)) return null;

        // Проверяем соседей слева и справа
        if (!isNumber(state, row, col - 1, 1)) return null;
        if (!isNumber(state, row, col + 1, 1)) return null;

        // Проверяем клетки сверху
        Cell topLeft = state.getCell(row - 1, col - 1);
        Cell topCenter = state.getCell(row - 1, col);
        Cell topRight = state.getCell(row - 1, col + 1);

        // Все три должны быть неизвестны
        if (!topLeft.isOpenable() || !topCenter.isOpenable() || !topRight.isOpenable()) {
            return null;
        }

        // Паттерн найден!
        // Крайние - мины, центр - безопасен
        if (row == 0) return null; // Проверка границы

        // Сначала вернем безопасную клетку (центр)
        return new Move(row - 1, col, false, 0.0,
                "Паттерн 1-2-1: центральная клетка безопасна");
    }

    private Move detectVerticalPattern(GameState state, int row, int col) {
        // Аналогично для вертикали
        if (!isNumber(state, row, col, 2)) return null;
        if (!isNumber(state, row - 1, col, 1)) return null;
        if (!isNumber(state, row + 1, col, 1)) return null;

        Cell leftCenter = state.getCell(row, col - 1);
        if (!leftCenter.isOpenable()) return null;

        return new Move(row, col - 1, false, 0.0,
                "Паттерн 1-2-1 (вертикальный)");
    }

    private boolean isNumber(GameState state, int row, int col, int expected) {
        if (row < 0 || row >= state.getRows() || col < 0 || col >= state.getCols()) {
            return false;
        }
        Cell cell = state.getCell(row, col);
        return cell.isRevealed() && cell.getAdjacentMines() == expected;
    }
}
