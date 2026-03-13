package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

import java.util.*;

/**
 * Реализация паттерна 1-2-2-1.
 */
public class OneTwoTwoOnePattern implements Pattern {

    @Override
    public Move detect(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        // Поиск горизонтального паттерна
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols - 3; j++) {
                if (checkHorizontal(board, i, j)) {
                    return handleHorizontal(board, i, j);
                }
            }
        }

        // Поиск вертикального паттерна
        for (int i = 0; i < rows - 3; i++) {
            for (int j = 0; j < cols; j++) {
                if (checkVertical(board, i, j)) {
                    return handleVertical(board, i, j);
                }
            }
        }

        return null;
    }

    /**
     * Проверка горизонтального паттерна
     */
    private boolean checkHorizontal(Cell[][] board, int row, int col) {
        return isRevealedCell(board, row, col) &&
                getCellNumber(board, row, col) == 1 &&
                isRevealedCell(board, row, col + 1) &&
                getCellNumber(board, row, col + 1) == 2 &&
                isRevealedCell(board, row, col + 2) &&
                getCellNumber(board, row, col + 2) == 2 &&
                isRevealedCell(board, row, col + 3) &&
                getCellNumber(board, row, col + 3) == 1;
    }

    /**
     * Проверка вертикального паттерна
     */
    private boolean checkVertical(Cell[][] board, int row, int col) {
        return isRevealedCell(board, row, col) &&
                getCellNumber(board, row, col) == 1 &&
                isRevealedCell(board, row + 1, col) &&
                getCellNumber(board, row + 1, col) == 2 &&
                isRevealedCell(board, row + 2, col) &&
                getCellNumber(board, row + 2, col) == 2 &&
                isRevealedCell(board, row + 3, col) &&
                getCellNumber(board, row + 3, col) == 1;
    }

    /**
     * Обработка горизонтального паттерна
     */
    private Move handleHorizontal(Cell[][] board, int row, int col) {
        Set<Cell> mines = new HashSet<>();
        Set<Cell> safe = new HashSet<>();

        for (int i = -1; i <= 1; i += 2) {
            int newRow = row + i;

            // Над первой единицей - мина
            if (isValidCell(board, newRow, col)) {
                Cell cell = board[newRow][col];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Над последней единицей - мина
            if (isValidCell(board, newRow, col + 3)) {
                Cell cell = board[newRow][col + 3];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Над второй двойкой - мина
            if (isValidCell(board, newRow, col + 2)) {
                Cell cell = board[newRow][col + 2];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Над первой двойкой - безопасно
            if (isValidCell(board, newRow, col + 1)) {
                Cell cell = board[newRow][col + 1];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    safe.add(cell);
                }
            }
        }

        if (!safe.isEmpty()) {
            Cell safeCell = safe.iterator().next();
            return new Move(safeCell.getRow(), safeCell.getCol(), false, 0.9, getName(),
                    "Паттерн 1-2-2-1: клетка над первой двойкой безопасна");
        }

        if (!mines.isEmpty()) {
            Cell mineCell = mines.iterator().next();
            return new Move(mineCell.getRow(), mineCell.getCol(), true, 0.9, getName(),
                    "Паттерн 1-2-2-1: найдена мина");
        }

        return null;
    }

    /**
     * Обработка вертикального паттерна
     */
    private Move handleVertical(Cell[][] board, int row, int col) {
        Set<Cell> mines = new HashSet<>();
        Set<Cell> safe = new HashSet<>();

        for (int i = -1; i <= 1; i += 2) {
            int newCol = col + i;

            // Слева от первой единицы - мина
            if (isValidCell(board, row, newCol)) {
                Cell cell = board[row][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Слева от последней единицы - мина
            if (isValidCell(board, row + 3, newCol)) {
                Cell cell = board[row + 3][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Слева от второй двойки - мина
            if (isValidCell(board, row + 2, newCol)) {
                Cell cell = board[row + 2][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Слева от первой двойки - безопасно
            if (isValidCell(board, row + 1, newCol)) {
                Cell cell = board[row + 1][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    safe.add(cell);
                }
            }
        }

        if (!safe.isEmpty()) {
            Cell safeCell = safe.iterator().next();
            return new Move(safeCell.getRow(), safeCell.getCol(), false, 0.9, getName(),
                    "Паттерн 1-2-2-1 (вертикальный): клетка слева от первой двойки безопасна");
        }

        if (!mines.isEmpty()) {
            Cell mineCell = mines.iterator().next();
            return new Move(mineCell.getRow(), mineCell.getCol(), true, 0.9, getName(),
                    "Паттерн 1-2-2-1 (вертикальный): найдена мина");
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
        return "1-2-2-1 Pattern";
    }

    @Override
    public String getDescription() {
        return "Определяет конфигурацию 1-2-2-1, где определенные клетки являются минами";
    }
}