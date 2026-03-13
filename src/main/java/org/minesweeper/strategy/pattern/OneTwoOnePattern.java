package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

import java.util.*;

/**
 * Реализация паттерна 1-2-1.
 * Определяет мины и безопасные клетки в конфигурации 1-2-1.
 */
public class OneTwoOnePattern implements Pattern {

    @Override
    public Move detect(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        // Поиск горизонтального паттерна
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols - 2; j++) {
                Move move = detectHorizontal(board, i, j);
                if (move != null) return move;
            }
        }

        // Поиск вертикального паттерна
        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < cols; j++) {
                Move move = detectVertical(board, i, j);
                if (move != null) return move;
            }
        }

        return null;
    }

    /**
     * Поиск горизонтального паттерна 1-2-1
     */
    private Move detectHorizontal(Cell[][] board, int row, int col) {
        if (!isRevealedCell(board, row, col) || !isRevealedCell(board, row, col + 2)) {
            return null;
        }

        int leftNum = getCellNumber(board, row, col);
        int rightNum = getCellNumber(board, row, col + 2);

        if (leftNum == 1 && rightNum == 1 && isRevealedCell(board, row, col + 1)) {
            int middleNum = getCellNumber(board, row, col + 1);
            if (middleNum == 2) {
                return handleHorizontal(board, row, col, col + 1, col + 2);
            }
        }

        return null;
    }

    /**
     * Поиск вертикального паттерна 1-2-1
     */
    private Move detectVertical(Cell[][] board, int row, int col) {
        if (!isRevealedCell(board, row, col) || !isRevealedCell(board, row + 2, col)) {
            return null;
        }

        int topNum = getCellNumber(board, row, col);
        int bottomNum = getCellNumber(board, row + 2, col);

        if (topNum == 1 && bottomNum == 1 && isRevealedCell(board, row + 1, col)) {
            int middleNum = getCellNumber(board, row + 1, col);
            if (middleNum == 2) {
                return handleVertical(board, row, col, row + 1, row + 2);
            }
        }

        return null;
    }

    /**
     * Обработка горизонтального паттерна
     */
    private Move handleHorizontal(Cell[][] board, int row, int col1, int col2, int col3) {
        Set<Cell> mines = new HashSet<>();
        Set<Cell> safe = new HashSet<>();

        // Проверяем клетки сверху и снизу
        for (int i = -1; i <= 1; i += 2) {
            int newRow = row + i;

            // Клетки над/под крайними единицами - мины
            if (isValidCell(board, newRow, col1)) {
                Cell cell = board[newRow][col1];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            if (isValidCell(board, newRow, col3)) {
                Cell cell = board[newRow][col3];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Клетки над/под центром - безопасны
            if (isValidCell(board, newRow, col2)) {
                Cell cell = board[newRow][col2];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    safe.add(cell);
                }
            }
        }

        // Сначала открываем безопасные клетки
        if (!safe.isEmpty()) {
            Cell safeCell = safe.iterator().next();
            return new Move(safeCell.getRow(), safeCell.getCol(), false, 0.95, getName(),
                    "Паттерн 1-2-1: клетка над/под центром безопасна");
        }

        // Затем ставим флаги на мины
        if (!mines.isEmpty()) {
            Cell mineCell = mines.iterator().next();
            return new Move(mineCell.getRow(), mineCell.getCol(), true, 0.95, getName(),
                    "Паттерн 1-2-1: клетка над/под крайней единицей - мина");
        }

        return null;
    }

    /**
     * Обработка вертикального паттерна
     */
    private Move handleVertical(Cell[][] board, int row1, int col, int row2, int row3) {
        Set<Cell> mines = new HashSet<>();
        Set<Cell> safe = new HashSet<>();

        // Проверяем клетки слева и справа
        for (int i = -1; i <= 1; i += 2) {
            int newCol = col + i;

            // Клетки слева/справа от крайних единиц - мины
            if (isValidCell(board, row1, newCol)) {
                Cell cell = board[row1][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            if (isValidCell(board, row3, newCol)) {
                Cell cell = board[row3][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Клетки слева/справа от центра - безопасны
            if (isValidCell(board, row2, newCol)) {
                Cell cell = board[row2][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    safe.add(cell);
                }
            }
        }

        if (!safe.isEmpty()) {
            Cell safeCell = safe.iterator().next();
            return new Move(safeCell.getRow(), safeCell.getCol(), false, 0.95, getName(),
                    "Паттерн 1-2-1 (вертикальный): клетка слева/справа от центра безопасна");
        }

        if (!mines.isEmpty()) {
            Cell mineCell = mines.iterator().next();
            return new Move(mineCell.getRow(), mineCell.getCol(), true, 0.95, getName(),
                    "Паттерн 1-2-1 (вертикальный): клетка слева/справа от крайней единицы - мина");
        }

        return null;
    }

    /**
     * Вспомогательные методы
     */
    private boolean isRevealedCell(Cell[][] board, int row, int col) {
        return isValidCell(board, row, col) && board[row][col].isRevealed();
    }

    private int getCellNumber(Cell[][] board, int row, int col) {
        return board[row][col].getAdjacentMines();
    }

    private boolean isValidCell(Cell[][] board, int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    @Override
    public String getName() {
        return "1-2-1 Pattern";
    }

    @Override
    public String getDescription() {
        return "Определяет конфигурацию 1-2-1, где клетки над/под крайними единицами - мины, " +
                "а над/под центром - безопасны";
    }
}