package org.minesweeper.core;

import org.minesweeper.bot.MinesweeperBot;

import java.util.Arrays;

/**
 * Состояние игры в конкретный момент.
 * Используется для сохранения/восстановления состояния и анализа.
 */
public class GameState {
    private final Cell[][] board;            // Копия игрового поля
    private final int rows;                   // Размеры доски
    private final int cols;
    private final int remainingMines;         // Оставшиеся мины
    private final boolean gameActive;          // Флаг активности игры
    private final boolean gameWon;             // Флаг победы
    private final int moveCount;               // Количество сделанных ходов
    private final long timestamp;              // Временная метка

    /**
     * Приватный конструктор для создания состояния
     */
    private GameState(Cell[][] board, int remainingMines, boolean gameActive,
                      boolean gameWon, int moveCount) {
        this.rows = board.length;
        this.cols = board[0].length;
        this.board = new Cell[rows][cols];

        // Глубокое копирование доски
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.board[i][j] = new Cell(board[i][j]);
            }
        }

        this.remainingMines = remainingMines;
        this.gameActive = gameActive;
        this.gameWon = gameWon;
        this.moveCount = moveCount;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Создание снимка текущего состояния
     */
    public static GameState snapshotFrom(MinesweeperBot bot) {
        // Используем рефлексию для доступа к protected полям
        // В реальном проекте лучше добавить геттеры в MinesweeperBot
        return new GameState(
                bot.board,  // Предполагаем, что поле доступно
                bot.totalMines - bot.getFlaggedCount(),
                bot.isGameActive(),
                bot.isGameWon(),
                0  // moveCount нужно добавить в MinesweeperBot
        );
    }

    /**
     * Восстановление состояния
     */
    public void restoreTo(MinesweeperBot bot) {
        // Копируем сохраненное состояние обратно в бота
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bot.board[i][j] = new Cell(this.board[i][j]);
            }
        }
        // Восстанавливаем другие параметры через рефлексию или геттеры/сеттеры
    }

    /**
     * Получение клетки по координатам
     */
    public Cell getCell(int row, int col) {
        return new Cell(board[row][col]); // Возвращаем копию для безопасности
    }

    /**
     * Проверка, является ли состояние терминальным
     */
    public boolean isTerminal() {
        return !gameActive || gameWon;
    }

    /**
     * Получение всех неоткрытых клеток
     */
    public Cell[][] getUnrevealedCells() {
        Cell[][] unrevealed = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed()) {
                    unrevealed[i][j] = new Cell(board[i][j]);
                }
            }
        }
        return unrevealed;
    }

    /**
     * Сравнение двух состояний
     */
    public boolean equals(GameState other) {
        if (other == null) return false;
        if (this.rows != other.rows || this.cols != other.cols) return false;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!this.board[i][j].equals(other.board[i][j])) {
                    return false;
                }
            }
        }

        return this.remainingMines == other.remainingMines &&
                this.gameActive == other.gameActive &&
                this.gameWon == other.gameWon;
    }

    // Геттеры
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getRemainingMines() { return remainingMines; }
    public boolean isGameActive() { return gameActive; }
    public boolean isGameWon() { return gameWon; }
    public int getMoveCount() { return moveCount; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("GameState{remainingMines=%d, active=%b, won=%b, moves=%d, time=%d}",
                remainingMines, gameActive, gameWon, moveCount, timestamp);
    }
}