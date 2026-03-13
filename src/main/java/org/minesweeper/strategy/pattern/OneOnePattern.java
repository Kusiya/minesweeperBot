package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

import java.util.*;

/**
 * Реализация паттерна 1-1.
 * Две рядом стоящие единицы с общей границей.
 */
public class OneOnePattern implements Pattern {

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
     * Поиск горизонтального паттерна 1-1
     */
    private Move detectHorizontal(Cell[][] board, int row, int col) {
        if (!isRevealedCell(board, row, col) || !isRevealedCell(board, row, col + 1)) {
            return null;
        }

        int leftNum = getCellNumber(board, row, col);
        int rightNum = getCellNumber(board, row, col + 1);

        if (leftNum == 1 && rightNum == 1) {
            return handleHorizontal(board, row, col);
        }

        return null;
    }

    /**
     * Поиск вертикального паттерна 1-1
     */
    private Move detectVertical(Cell[][] board, int row, int col) {
        if (!isRevealedCell(board, row, col) || !isRevealedCell(board, row + 1, col)) {
            return null;
        }

        int topNum = getCellNumber(board, row, col);
        int bottomNum = getCellNumber(board, row + 1, col);

        if (topNum == 1 && bottomNum == 1) {
            return handleVertical(board, row, col);
        }

        return null;
    }

    /**
     * Обработка горизонтального паттерна
     */
    private Move handleHorizontal(Cell[][] board, int row, int col) {
        // Проверяем общие неоткрытые клетки
        Set<Cell> commonNeighbors = getCommonHorizontalNeighbors(board, row, col);

        if (commonNeighbors.size() == 1) {
            // Если есть одна общая клетка - это мина
            Cell mine = commonNeighbors.iterator().next();
            return new Move(mine.getRow(), mine.getCol(), true, 0.9, getName(),
                    "Паттерн 1-1: общая клетка - мина");
        }

        return null;
    }

    /**
     * Обработка вертикального паттерна
     */
    private Move handleVertical(Cell[][] board, int row, int col) {
        // Проверяем общие неоткрытые клетки
        Set<Cell> commonNeighbors = getCommonVerticalNeighbors(board, row, col);

        if (commonNeighbors.size() == 1) {
            // Если есть одна общая клетка - это мина
            Cell mine = commonNeighbors.iterator().next();
            return new Move(mine.getRow(), mine.getCol(), true, 0.9, getName(),
                    "Паттерн 1-1 (вертикальный): общая клетка - мина");
        }

        return null;
    }

    /**
     * Получение общих соседей для горизонтального паттерна
     */
    private Set<Cell> getCommonHorizontalNeighbors(Cell[][] board, int row, int col) {
        Set<Cell> leftNeighbors = getUnrevealedNeighbors(board, row, col);
        Set<Cell> rightNeighbors = getUnrevealedNeighbors(board, row, col + 1);

        leftNeighbors.retainAll(rightNeighbors);
        return leftNeighbors;
    }

    /**
     * Получение общих соседей для вертикального паттерна
     */
    private Set<Cell> getCommonVerticalNeighbors(Cell[][] board, int row, int col) {
        Set<Cell> topNeighbors = getUnrevealedNeighbors(board, row, col);
        Set<Cell> bottomNeighbors = getUnrevealedNeighbors(board, row + 1, col);

        topNeighbors.retainAll(bottomNeighbors);
        return topNeighbors;
    }

    /**
     * Получение неоткрытых соседей клетки
     */
    private Set<Cell> getUnrevealedNeighbors(Cell[][] board, int row, int col) {
        Set<Cell> neighbors = new HashSet<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = row + i;
                int newCol = col + j;

                if (isValidCell(board, newRow, newCol)) {
                    Cell cell = board[newRow][newCol];
                    if (!cell.isRevealed() && !cell.isFlagged()) {
                        neighbors.add(cell);
                    }
                }
            }
        }

        return neighbors;
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
        return "1-1 Pattern";
    }

    @Override
    public String getDescription() {
        return "Две рядом стоящие единицы указывают на общую мину";
    }
}
