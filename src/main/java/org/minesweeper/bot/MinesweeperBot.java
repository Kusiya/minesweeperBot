package org.minesweeper.bot;

import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.Strategy;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.strategy.AdvancedStrategy;
import org.minesweeper.vision.ScreenCapture;
import org.minesweeper.vision.CellRecognizer;
import org.minesweeper.vision.TemplateCellRecognizer;
import org.minesweeper.vision.TemplateLoader;
import org.minesweeper.execution.MouseController;
import org.minesweeper.utils.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Главный класс бота, координирующий все компоненты
 */
public class MinesweeperBot {
    private boolean running;
    private int delayBetweenMoves;

    // Компоненты
    private ScreenCapture screenCapture;
    private CellRecognizer cellRecognizer;
    private Strategy strategy;
    private MouseController mouseController;

    // Статистика
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;

    // Параметры игры
    private int rows;
    private int cols;
    private int totalMines;

    // Калибровочные данные - ДОБАВИТЬ ЭТИ ПОЛЯ
    private int offsetX;
    private int offsetY;
    private int cellSize;

    public MinesweeperBot() throws AWTException {
        this.running = false;
        this.delayBetweenMoves = 200;

        // Инициализация компонентов
        this.screenCapture = new ScreenCapture();
        this.mouseController = new MouseController();

        // Загрузка шаблонов для распознавания
        TemplateLoader templateLoader = new TemplateLoader();
        templateLoader.loadAllTemplates();
        this.cellRecognizer = new TemplateCellRecognizer(templateLoader);

        // По умолчанию используем продвинутую стратегию
        this.strategy = new AdvancedStrategy();

        // Параметры по умолчанию (легкий уровень)
        this.rows = 9;
        this.cols = 9;
        this.totalMines = 10;

        // Калибровочные данные по умолчанию
        this.offsetX = 100;
        this.offsetY = 100;
        this.cellSize = 30;
    }

    /**
     * Запуск бота
     */
    public void start() {
        running = true;
        gamesPlayed++;

        Logger.info("Бот запущен. Стратегия: " + strategy.getName());
        Logger.info("Параметры игры: " + rows + "x" + cols + ", мин: " + totalMines);
        Logger.info("Калибровка: offset=(" + offsetX + "," + offsetY + "), cellSize=" + cellSize);

        try {
            gameLoop();
        } catch (Exception e) {
            Logger.error("Ошибка в игровом цикле: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Основной игровой цикл
     */
    private void gameLoop() throws Exception {
        // Даем время переключиться на игру
        Logger.info("Переключитесь на окно с игрой...");
        Thread.sleep(3000);

        while (running) {
            // 1. Захват экрана
            BufferedImage screenshot = screenCapture.captureGameArea();

            // 2. Распознавание поля - ИСПРАВЛЕНО: добавили параметры калибровки
            int[][] visionData = cellRecognizer.recognizeBoard(
                    screenshot, rows, cols, offsetX, offsetY, cellSize
            );

            // 3. Обновление состояния
            GameState state = new GameState(rows, cols);
            state.setTotalMines(totalMines);
            state.updateFromVision(visionData);

            // 4. Проверка окончания игры
            if (state.isGameOver()) {
                Logger.info("💥 Игра проиграна!");
                gamesLost++;
                break;
            }

            if (state.checkWinCondition()) {
                Logger.info("🏆 Победа!");
                gamesWon++;
                break;
            }

            // 5. Выбор хода
            state.printBoard(); // для отладки
            Move move = strategy.nextMove(state);

            if (move == null) {
                Logger.warn("❌ Нет доступных ходов. Завершение.");
                gamesLost++;
                break;
            }

            Logger.info("➡ Выбран ход: " + move);

            // 6. Выполнение хода
            mouseController.executeMove(move);

            // 7. Пауза между ходами
            Thread.sleep(delayBetweenMoves);
        }

        Logger.info("Игровой цикл завершен");
    }

    /**
     * Остановка бота
     */
    public void stop() {
        running = false;
        Logger.info("Бот остановлен");
    }

    // Настройки
    public void setDelay(int ms) {
        this.delayBetweenMoves = ms;
    }

    public int getDelay() {
        return delayBetweenMoves;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
        Logger.info("Стратегия изменена на: " + strategy.getName());
    }

    public String getStrategyName() {
        return strategy.getName();
    }

    public void setGameParameters(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
    }

    // Калибровка - ИСПРАВЛЕНО: сохраняем значения в поля класса
    public void calibrate(int offsetX, int offsetY, int cellSize) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.cellSize = cellSize;

        mouseController.setOffset(offsetX, offsetY);
        mouseController.setCellSize(cellSize);
        screenCapture.setOffsetX(offsetX);
        screenCapture.setOffsetY(offsetY);
        screenCapture.setCellSize(cellSize);

        Logger.info("Калибровка завершена: offset=(" + offsetX + "," + offsetY + "), size=" + cellSize);
    }

    // Статистика
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public int getGamesLost() { return gamesLost; }

    public double getWinRate() {
        if (gamesPlayed == 0) return 0;
        return (double) gamesWon / gamesPlayed;
    }

    // Геттеры для калибровки
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getCellSize() { return cellSize; }
}