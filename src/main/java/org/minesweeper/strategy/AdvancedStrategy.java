package org.minesweeper.strategy;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;
import org.minesweeper.utils.Logger;

import java.util.*;

/**
 * Продвинутая стратегия с вероятностным анализом.
 */
public class AdvancedStrategy implements Strategy {
    private Logger logger;
    private Map<String, Double> probabilityCache;

    public AdvancedStrategy() {
        this.logger = Logger.getInstance();
        this.probabilityCache = new HashMap<>();
    }

    @Override
    public Move analyze(Cell[][] board, int totalMines) {
        logger.debug("AdvancedStrategy: вероятностный анализ");

        probabilityCache.clear();

        int rows = board.length;
        int cols = board[0].length;

        // Собираем все неоткрытые клетки
        List<Cell> unrevealedCells = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                    unrevealedCells.add(board[i][j]);
                }
            }
        }

        if (unrevealedCells.isEmpty()) {
            return null;
        }

        // Вычисляем вероятности
        Map<Cell, Double> probabilities = calculateProbabilities(board, unrevealedCells, totalMines);

        // Находим клетку с наименьшей вероятностью мины
        Cell safest = findSafestCell(probabilities);

        if (safest != null) {
            double probability = probabilities.get(safest);
            logger.debug(String.format("AdvancedStrategy: safest клетка (%d,%d) с вероятностью %.2f",
                    safest.getRow(), safest.getCol(), probability));

            // Если вероятность очень низкая, открываем клетку
            if (probability < 0.3) {
                return new Move(safest.getRow(), safest.getCol(), false, 1.0 - probability, getName(),
                        String.format("Низкая вероятность мины (%.2f%%)", probability * 100));
            }

            // Если вероятность очень высокая, ставим флаг
            if (probability > 0.7) {
                return new Move(safest.getRow(), safest.getCol(), true, probability, getName(),
                        String.format("Высокая вероятность мины (%.2f%%)", probability * 100));
            }
        }

        return null;
    }

    /**
     * Расчет вероятностей для всех неоткрытых клеток
     */
    private Map<Cell, Double> calculateProbabilities(Cell[][] board,
                                                     List<Cell> unrevealedCells, int totalMines) {
        Map<Cell, Double> probabilities = new HashMap<>();

        int flaggedCount = countFlagged(board);
        int remainingMines = totalMines - flaggedCount;

        for (Cell cell : unrevealedCells) {
            double probability = calculateCellProbability(board, cell, remainingMines);
            probabilities.put(cell, probability);
        }

        return probabilities;
    }

    /**
     * Расчет вероятности для конкретной клетки
     */
    private double calculateCellProbability(Cell[][] board, Cell cell, int remainingMines) {
        List<Cell> adjacentRevealed = getAdjacentRevealedCells(board, cell);

        if (adjacentRevealed.isEmpty()) {
            // Нет информации от соседей - используем глобальную вероятность
            int unrevealedCount = countUnrevealed(board);
            return (double) remainingMines / unrevealedCount;
        }

        double totalProbability = 0;
        int contributingNeighbors = 0;

        for (Cell revealed : adjacentRevealed) {
            Set<Cell> unrevealedNeighbors = getUnrevealedNeighbors(board, revealed);
            Set<Cell> flaggedNeighbors = getFlaggedNeighbors(board, revealed);

            if (unrevealedNeighbors.contains(cell)) {
                int minesNeeded = revealed.getAdjacentMines() - flaggedNeighbors.size();

                if (minesNeeded > 0) {
                    // Равномерно распределяем вероятность между неоткрытыми соседями
                    double neighborProbability = (double) minesNeeded / unrevealedNeighbors.size();
                    totalProbability += neighborProbability;
                    contributingNeighbors++;
                } else if (minesNeeded == 0) {
                    // Если вокруг клетки не должно быть мин, то эта клетка безопасна
                    return 0.0;
                }
            }
        }

        if (contributingNeighbors > 0) {
            return totalProbability / contributingNeighbors;
        }

        // Если нет информации, используем глобальную вероятность
        int unrevealedCount = countUnrevealed(board);
        return (double) remainingMines / unrevealedCount;
    }

    /**
     * Поиск safest клетки (с наименьшей вероятностью мины)
     */
    private Cell findSafestCell(Map<Cell, Double> probabilities) {
        Cell safest = null;
        double minProbability = 1.0;

        for (Map.Entry<Cell, Double> entry : probabilities.entrySet()) {
            if (entry.getValue() < minProbability) {
                minProbability = entry.getValue();
                safest = entry.getKey();
            }
        }

        return safest;
    }

    /**
     * Подсчет количества флагов
     */
    private int countFlagged(Cell[][] board) {
        int count = 0;
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (cell.isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Подсчет количества неоткрытых клеток
     */
    private int countUnrevealed(Cell[][] board) {
        int count = 0;
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (!cell.isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Получение открытых соседей клетки
     */
    private List<Cell> getAdjacentRevealedCells(Cell[][] board, Cell cell) {
        List<Cell> revealed = new ArrayList<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = cell.getRow() + i;
                int newCol = cell.getCol() + j;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    Cell neighbor = board[newRow][newCol];
                    if (neighbor.isRevealed()) {
                        revealed.add(neighbor);
                    }
                }
            }
        }
        return revealed;
    }

    /**
     * Получение неоткрытых соседей клетки
     */
    private Set<Cell> getUnrevealedNeighbors(Cell[][] board, Cell cell) {
        Set<Cell> neighbors = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = cell.getRow() + i;
                int newCol = cell.getCol() + j;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    Cell neighbor = board[newRow][newCol];
                    if (!neighbor.isRevealed()) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

    /**
     * Получение соседей с флагами
     */
    private Set<Cell> getFlaggedNeighbors(Cell[][] board, Cell cell) {
        Set<Cell> neighbors = new HashSet<>();
        int rows = board.length;
        int cols = board[0].length;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int newRow = cell.getRow() + i;
                int newCol = cell.getCol() + j;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    Cell neighbor = board[newRow][newCol];
                    if (neighbor.isFlagged()) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

    @Override
    public String getName() {
        return "Advanced Strategy";
    }

    @Override
    public int getPriority() {
        return 50; // Средний приоритет
    }

    @Override
    public String getDescription() {
        return "Использует вероятностный анализ для оценки риска каждой клетки";
    }

    @Override
    public void reset() {
        probabilityCache.clear();
    }
}