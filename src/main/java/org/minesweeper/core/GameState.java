package org.minesweeper.core;

import org.minesweeper.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.minesweeper.vision.TemplateCellRecognizer.FLAG;
import static org.minesweeper.vision.TemplateCellRecognizer.MINE;

/**
 * Хранит текущее состояние игрового поля
 */
public class GameState {
    private final int rows;
    private final int cols;
    private final Cell[][] board;
    private int totalMines;
    private int revealedCount;
    private int flaggedCount;
    private boolean gameOver;
    private boolean gameWon;

    public GameState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new Cell[rows][cols];
        this.revealedCount = 0;
        this.flaggedCount = 0;
        this.gameOver = false;
        this.gameWon = false;

        // Инициализируем клетки
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * Обновляет состояние на основе распознанных данных
     * @param visionData матрица значений от vision модуля:
     *                   -1 - мина, 0-8 - цифры, -2 - закрыто, -3 - флаг
     */
    public void updateFromVision(int[][] visionData) {
        // Сбрасываем флаги
        boolean mineFound = false;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = visionData[i][j];
                Cell cell = board[i][j];

                if (value == MINE) {
                    // Мина обнаружена!
                    cell.setRevealed(true);
                    cell.setMine(true);
                    mineFound = true;
                    System.out.println("💥💥💥 МИНА ОБНАРУЖЕНА в клетке [" + i + "," + j + "]");
                }
                else if (value >= 0 && value <= 8) {
                    cell.setRevealed(true);
                    cell.setMine(false);
                    cell.setAdjacentMines(value);
                }
                else if (value == FLAG) {
                    cell.setFlagged(true);
                }
                // UNKNOWN ничего не меняем
            }
        }

        // Если найдена мина - игра проиграна
        if (mineFound) {
            gameOver = true;
            System.out.println("💥💥💥 ИГРА ПРОИГРАНА! Обнаружена мина.");
        }
    }

    /**
     * Получить всех соседей клетки
     */
    public List<Cell> getNeighbors(int row, int col) {
        List<Cell> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                if (isValidPosition(newRow, newCol)) {
                    neighbors.add(board[newRow][newCol]);
                }
            }
        }
        return neighbors;
    }

    /**
     * Подсчитать количество флагов вокруг клетки
     */
    public int countFlagsAround(int row, int col) {
        int count = 0;
        for (Cell cell : getNeighbors(row, col)) {
            if (cell.isFlagged()) count++;
        }
        return count;
    }

    /**
     * Проверить, есть ли мины на поле
     */
    public boolean hasMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isMine()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Подсчитать количество неизвестных (закрытых) клеток вокруг
     */
    public int countUnknownAround(int row, int col) {
        int count = 0;
        for (Cell cell : getNeighbors(row, col)) {
            if (!cell.isRevealed() && !cell.isFlagged()) count++;
        }
        return count;
    }

    /**
     * Получить все неизвестные клетки на поле
     */
    public List<Cell> getUnknownCells() {
        List<Cell> unknown = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                    unknown.add(board[i][j]);
                }
            }
        }
        return unknown;
    }

    /**
     * Проверить, выиграна ли игра
     */
    public boolean checkWinCondition() {
        if (!gameOver && revealedCount == rows * cols - totalMines) {
            gameWon = true;
        }
        return gameWon;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // Геттеры
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getCell(int row, int col) { return board[row][col]; }
    public int getTotalMines() { return totalMines; }
    public int getRevealedCount() { return revealedCount; }
    public int getFlaggedCount() { return flaggedCount; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }

    public void setTotalMines(int mines) { this.totalMines = mines; }

    /**
     * Распечатать поле в консоль (для отладки)
     */
    public void printBoard() {
        System.out.println("\nТекущее поле:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}
