package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

import java.util.*;

/**
 * Реализация паттерна 2-2.
 * Две рядом стоящие двойки указывают на мины над/под ними.
 */
public class TwoTwoPattern implements Pattern {

    @Override
    public Move detect(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        // Поиск горизонтального паттерна
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols - 1; j++) {
                Move move = detectHorizontal(board, i, j);
                if (move != null) return move;
            }
        }

        // Поиск вертикального паттерна
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols; j++) {
                Move move = detectVertical(board, i, j);
                if (move != null) return move;
            }
        }

        return null;
    }

    /**
     * Поиск горизонтального паттерна 2-2
     */
    private Move detectHorizontal(Cell[][] board, int row, int col) {
        if (!isRevealedCell(board, row, col) || !isRevealedCell(board, row, col + 1)) {
            return null;
        }

        int leftNum = getCellNumber(board, row, col);
        int rightNum = getCellNumber(board, row, col + 1);

        if (leftNum == 2 && rightNum == 2) {
            return handleHorizontal(board, row, col);
        }

        return null;
    }

    /**
     * Поиск вертикального паттерна 2-2
     */
    private Move detectVertical(Cell[][] board, int row, int col) {
        if (!isRevealedCell(board, row, col) || !isRevealedCell(board, row + 1, col)) {
            return null;
        }

        int topNum = getCellNumber(board, row, col);
        int bottomNum = getCellNumber(board, row + 1, col);

        if (topNum == 2 && bottomNum == 2) {
            return handleVertical(board, row, col);
        }

        return null;
    }

    /**
     * Обработка горизонтального паттерна
     */
    private Move handleHorizontal(Cell[][] board, int row, int col) {
        Set<Cell> mines = new HashSet<>();

        // Клетки над и под обеими двойками - мины
        for (int i = -1; i <= 1; i += 2) {
            int newRow = row + i;

            // Над/под левой двойкой
            if (isValidCell(board, newRow, col)) {
                Cell cell = board[newRow][col];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Над/под правой двойкой
            if (isValidCell(board, newRow, col + 1)) {
                Cell cell = board[newRow][col + 1];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }
        }

        if (!mines.isEmpty()) {
            Cell mineCell = mines.iterator().next();
            return new Move(mineCell.getRow(), mineCell.getCol(), true, 0.9, getName(),
                    "Паттерн 2-2: клетки над/под двойками - мины");
        }

        return null;
    }

    /**
     * Обработка вертикального паттерна
     */
    private Move handleVertical(Cell[][] board, int row, int col) {
        Set<Cell> mines = new HashSet<>();

        // Клетки слева и справа от обеих двоек - мины
        for (int i = -1; i <= 1; i += 2) {
            int newCol = col + i;

            // Слева/справа от верхней двойки
            if (isValidCell(board, row, newCol)) {
                Cell cell = board[row][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }

            // Слева/справа от нижней двойки
            if (isValidCell(board, row + 1, newCol)) {
                Cell cell = board[row + 1][newCol];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    mines.add(cell);
                }
            }
        }

        if (!mines.isEmpty()) {
            Cell mineCell = mines.iterator().next();
            return new Move(mineCell.getRow(), mineCell.getCol(), true, 0.9, getName(),
                    "Паттерн 2-2 (вертикальный): клетки слева/справа от двоек - мины");
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
        return "2-2 Pattern";
    }

    @Override
    public String getDescription() {
        return "Две рядом стоящие двойки указывают на мины над/под ними";
    }
}
