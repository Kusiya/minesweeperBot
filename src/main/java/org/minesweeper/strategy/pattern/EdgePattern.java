package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;

import java.util.*;

/**
 * Паттерны для работы с границами доски.
 */
public class EdgePattern implements Pattern {

    @Override
    public Move detect(Cell[][] board) {
        int rows = board.length;
        int cols = board[0].length;

        // Проверка верхней границы
        for (int j = 0; j < cols; j++) {
            if (isRevealedCell(board, 0, j)) {
                Move move = analyzeEdgeCell(board, 0, j);
                if (move != null) return move;
            }
        }

        // Проверка нижней границы
        for (int j = 0; j < cols; j++) {
            if (isRevealedCell(board, rows - 1, j)) {
                Move move = analyzeEdgeCell(board, rows - 1, j);
                if (move != null) return move;
            }
        }

        // Проверка левой границы
        for (int i = 1; i < rows - 1; i++) {
            if (isRevealedCell(board, i, 0)) {
                Move move = analyzeEdgeCell(board, i, 0);
                if (move != null) return move;
            }
        }

        // Проверка правой границы
        for (int i = 1; i < rows - 1; i++) {
            if (isRevealedCell(board, i, cols - 1)) {
                Move move = analyzeEdgeCell(board, i, cols - 1);
                if (move != null) return move;
            }
        }

        return null;
    }

    /**
     * Анализ клетки на границе
     */
    private Move analyzeEdgeCell(Cell[][] board, int row, int col) {
        Set<Cell> unrevealedNeighbors = getUnrevealedNeighbors(board, row, col);
        Set<Cell> flaggedNeighbors = getFlaggedNeighbors(board, row, col);

        int minesNeeded = board[row][col].getAdjacentMines() - flaggedNeighbors.size();

        // На границе некоторые соседи отсутствуют, что упрощает анализ
        if (unrevealedNeighbors.size() == minesNeeded && minesNeeded > 0) {
            for (Cell cell : unrevealedNeighbors) {
                if (!cell.isFlagged()) {
                    return new Move(cell.getRow(), cell.getCol(), true, 0.95, getName(),
                            "Граничный случай: все доступные соседи - мины");
                }
            }
        }

        if (minesNeeded == 0 && !unrevealedNeighbors.isEmpty()) {
            for (Cell cell : unrevealedNeighbors) {
                if (!cell.isFlagged()) {
                    return new Move(cell.getRow(), cell.getCol(), false, 0.95, getName(),
                            "Граничный случай: все мины отмечены, остальные безопасны");
                }
            }
        }

        return null;
    }

    /**
     * Получение неоткрытых соседей с учетом границ
     */
    private Set<Cell> getUnrevealedNeighbors(Cell[][] board, int row, int col) {
        Set<Cell> neighbors = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = row + i;
                int newCol = col + j;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    Cell cell = board[newRow][newCol];
                    if (!cell.isRevealed()) {
                        neighbors.add(cell);
                    }
                }
            }
        }
        return neighbors;
    }

    /**
     * Получение соседей с флагами
     */
    private Set<Cell> getFlaggedNeighbors(Cell[][] board, int row, int col) {
        Set<Cell> neighbors = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = row + i;
                int newCol = col + j;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    Cell cell = board[newRow][newCol];
                    if (cell.isFlagged()) {
                        neighbors.add(cell);
                    }
                }
            }
        }
        return neighbors;
    }

    /**
     * Проверка, открыта ли клетка
     */
    private boolean isRevealedCell(Cell[][] board, int row, int col) {
        return board[row][col].isRevealed();
    }

    @Override
    public String getName() {
        return "Edge Pattern";
    }

    @Override
    public String getDescription() {
        return "Специализированные правила для клеток на границе доски";
    }
}
