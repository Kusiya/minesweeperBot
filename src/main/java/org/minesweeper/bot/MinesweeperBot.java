package org.minesweeper.bot;

import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.strategy.Strategy;
import org.minesweeper.vision.CellRecognizer;
import org.minesweeper.execution.MouseController;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Упрощенная версия бота для быстрого запуска
 */
public class MinesweeperBot {
    private boolean running;
    private int delayBetweenMoves;

    private CellRecognizer cellRecognizer;
    private Strategy strategy;
    private MouseController mouseController;

    // Параметры игры
    private int rows;
    private int cols;
    private int totalMines;

    // Калибровочные данные
    private int offsetX;
    private int offsetY;
    private int cellSize;

    // Статистика
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;

    public MinesweeperBot() throws AWTException {
        this.running = false;
        this.delayBetweenMoves = 500;

        this.cellRecognizer = new CellRecognizer();
        this.strategy = new BasicStrategy();
        this.mouseController = new MouseController();

        // Параметры по умолчанию (легкий уровень)
        this.rows = 9;
        this.cols = 9;
        this.totalMines = 10;

        // Калибровка по умолчанию
        this.offsetX = 100;
        this.offsetY = 100;
        this.cellSize = 30;

        // Статистика
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
    }

    /**
     * Запуск бота
     */
    public void start() {
        running = true;
        gamesPlayed++;

        System.out.println("=== Бот запущен ===");
        System.out.println("Поле: " + rows + "x" + cols + ", мин: " + totalMines);
        System.out.println("Задержка между ходами: " + delayBetweenMoves + " мс");
        System.out.println("Координаты: offset=(" + offsetX + "," + offsetY + "), cellSize=" + cellSize);
        System.out.println("Стратегия: " + strategy.getName());
        System.out.println("===================");

        try {
            gameLoop();
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Основной игровой цикл
     */
    private void gameLoop() throws Exception {
        // Даем время переключиться на игру
        System.out.println("Переключитесь на окно с игрой...");
        Thread.sleep(3000);

        while (running) {
            // 1. Захват экрана
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(
                    offsetX - 10, offsetY - 10,
                    cols * cellSize + 20, rows * cellSize + 20
            );
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            // 2. Распознавание поля
            int[][] visionData = cellRecognizer.recognizeBoard(
                    screenshot, rows, cols,
                    10, 10, cellSize
            );

            // 3. Обновление состояния
            GameState state = new GameState(rows, cols);
            state.setTotalMines(totalMines);
            state.updateFromVision(visionData);

            // 4. Проверка окончания игры
            if (state.isGameOver()) {
                System.out.println("Игра проиграна!");
                gamesLost++;
                break;
            }

            if (state.checkWinCondition()) {
                System.out.println("Победа!");
                gamesWon++;
                break;
            }

            // 5. Выбор хода
            state.printBoard();
            Move move = strategy.nextMove(state);

            if (move == null) {
                System.out.println("Нет доступных ходов. Завершение.");
                gamesLost++;
                break;
            }

            System.out.println("➡ Ход: " + move);

            // 6. Выполнение хода
            mouseController.setOffset(offsetX, offsetY);
            mouseController.setCellSize(cellSize);
            mouseController.executeMove(move);

            // 7. Пауза
            Thread.sleep(delayBetweenMoves);
        }

        System.out.println("Игра завершена. Статистика: побед " + gamesWon + "/" + gamesPlayed);
        running = false;
    }

    public void stop() {
        running = false;
        System.out.println("Бот остановлен");
    }

    // Методы калибровки
    public void calibrate(int offsetX, int offsetY, int cellSize) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.cellSize = cellSize;
        mouseController.setOffset(offsetX, offsetY);
        mouseController.setCellSize(cellSize);
        System.out.println("Калибровка: (" + offsetX + "," + offsetY + "), размер=" + cellSize);
    }

    // Геттеры и сеттеры
    public void setDelay(int ms) {
        this.delayBetweenMoves = ms;
    }

    public int getDelay() {
        return delayBetweenMoves;
    }

    public void setGameParameters(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
    }

    // Методы для статистики (нужны для BotConsole)
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public double getWinRate() {
        if (gamesPlayed == 0) return 0;
        return (double) gamesWon / gamesPlayed;
    }

    public String getStrategyName() {
        return strategy.getName();
    }
}