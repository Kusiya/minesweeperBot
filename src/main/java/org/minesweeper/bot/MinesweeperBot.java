package org.minesweeper.bot;

import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.Strategy;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.vision.ScreenCapture;
import org.minesweeper.vision.CellRecognizer;
import org.minesweeper.execution.MouseController;
import org.minesweeper.utils.Logger;
import org.minesweeper.vision.TemplateCellRecognizer;
import org.minesweeper.vision.TemplateLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Главный класс бота с исправленным управлением
 */
public class MinesweeperBot {
    // Управление состоянием
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private Thread botThread;

    // Параметры
    private int delayBetweenMoves = 2000;
    private int rows = 9;
    private int cols = 9;
    private int totalMines = 10;

    // Компоненты
    private final ScreenCapture screenCapture;
    private final CellRecognizer cellRecognizer;
    private Strategy strategy;
    private final MouseController mouseController;

    // Статистика
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int gamesLost = 0;

    // Константы для защиты от зависания
    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final int MAX_MOVES_WITHOUT_CHANGE = 10;
    private static final long MOVE_TIMEOUT_MS = 3000;

    public MinesweeperBot() throws AWTException {
        this.screenCapture = new ScreenCapture();
        this.mouseController = new MouseController();
        TemplateLoader templateLoader = new TemplateLoader();
        templateLoader.loadAllTemplates();
        this.cellRecognizer = new TemplateCellRecognizer(templateLoader);
        this.strategy = new BasicStrategy(); // начнем с базовой

        Logger.info("Бот инициализирован");
    }

    /**
     * Запуск бота
     */
    public void start() {
        if (running.get()) {
            Logger.warn("Бот уже запущен");
            return;
        }

        running.set(true);
        paused.set(false);
        gamesPlayed++;

        Logger.info("=================================");
        Logger.info("🚀 ЗАПУСК БОТА");
        Logger.info("Стратегия: " + strategy.getName());
        Logger.info("Поле: " + rows + "x" + cols + ", мин: " + totalMines);
        Logger.info("=================================");

        botThread = new Thread(this::gameLoop, "MinesweeperBotThread");
        botThread.setDaemon(true);
        botThread.start();
    }

    /**
     * Остановка бота
     */
    public void stop() {
        Logger.info("🛑 Останавливаем бота...");
        running.set(false);
        paused.set(false);

        if (botThread != null) {
            botThread.interrupt();
            try {
                botThread.join(1000); // ждем максимум 1 секунду
            } catch (InterruptedException e) {
                Logger.error("Ошибка при остановке потока");
            }
        }

        Logger.info("✅ Бот остановлен");
    }

    /**
     * Пауза/возобновление
     */
    public void togglePause() {
        if (!running.get()) {
            Logger.warn("Бот не запущен");
            return;
        }

        boolean newPauseState = !paused.get();
        paused.set(newPauseState);
        Logger.info(newPauseState ? "⏸️ Бот на паузе" : "▶️ Бот возобновил работу");
    }

    /**
     * Основной игровой цикл с защитой от зависания
     */
    private void gameLoop() {
        int consecutiveFailures = 0;
        int movesWithoutChange = 0;
        String lastBoardHash = "";
        boolean gameFinished = false; // <-- ДОБАВИТЬ

        try {
            while (running.get() && !Thread.currentThread().isInterrupted() && !gameFinished) {
                // Проверка паузы
                while (paused.get() && running.get() && !gameFinished) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (!running.get() || gameFinished) break;

                // 1. Захват экрана с таймаутом
                BufferedImage screenshot = captureScreenWithRetry();
                if (screenshot == null) {
                    consecutiveFailures++;
                    Logger.error("Не удалось захватить экран (попытка " + consecutiveFailures + ")");

                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        Logger.error("Слишком много ошибок захвата. Завершаем игру.");
                        break;
                    }
                    continue;
                }

                // 2. Распознавание поля
                int[][] visionData = recognizeBoard(screenshot);
                if (visionData == null) {
                    consecutiveFailures++;
                    Logger.error("Не удалось распознать поле");

                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        Logger.error("Слишком много ошибок распознавания. Завершаем игру.");
                        break;
                    }
                    continue;
                }

                // Сброс счетчика ошибок при успехе
                consecutiveFailures = 0;

                // 3. Создание состояния игры
                GameState state = new GameState(rows, cols);
                state.setTotalMines(totalMines);
                state.updateFromVision(visionData);

                // 4. ПРОВЕРКА ОКОНЧАНИЯ ИГРЫ - ВАЖНО!
                if (state.isGameOver()) {
                    Logger.info("💥 ИГРА ПРОИГРАНА! Наступили на мину.");
                    gamesLost++;
                    showFinalBoard(state);
                    gameFinished = true; // <-- УСТАНОВИТЬ ФЛАГ
                    break; // <-- ВЫЙТИ ИЗ ЦИКЛА
                }

                if (state.checkWinCondition()) {
                    Logger.info("🎉 ПОБЕДА! Все мины найдены.");
                    gamesWon++;
                    showFinalBoard(state);
                    gameFinished = true; // <-- УСТАНОВИТЬ ФЛАГ
                    break; // <-- ВЫЙТИ ИЗ ЦИКЛА
                }

                // 5. Проверка прогресса
                String currentHash = getBoardHash(state);
                if (currentHash.equals(lastBoardHash)) {
                    movesWithoutChange++;
                    Logger.debug("Состояние не меняется: " + movesWithoutChange + "/" + MAX_MOVES_WITHOUT_CHANGE);

                    if (movesWithoutChange >= MAX_MOVES_WITHOUT_CHANGE) {
                        Logger.warn("Нет прогресса. Делаем случайный ход...");
                        makeRandomMove(state);
                        movesWithoutChange = 0;
                        try {
                            Thread.sleep(delayBetweenMoves);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }
                } else {
                    movesWithoutChange = 0;
                    lastBoardHash = currentHash;
                }

                // 6. Выбор хода
                Move move = strategy.nextMove(state);

                if (move == null) {
                    Logger.warn("Стратегия не нашла ход. Делаем случайный.");
                    makeRandomMove(state);
                } else {
                    Logger.info("🎯 Ход: " + move);
                    mouseController.executeMove(move);
                }

                // 7. Пауза между ходами
                try {
                    Thread.sleep(delayBetweenMoves);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

        } catch (Exception e) {
            Logger.error("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            running.set(false);
            printStatistics();
            // Небольшая пауза перед возвратом в меню
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Захват экрана с повторными попытками
     */
    private BufferedImage captureScreenWithRetry() {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < MOVE_TIMEOUT_MS) {
            try {
                return screenCapture.captureGameArea();
            } catch (Exception e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Распознавание поля
     */
    private int[][] recognizeBoard(BufferedImage screenshot) {
        try {
            return cellRecognizer.recognizeBoard(screenshot, rows, cols,
                    screenCapture.getOffsetX(), screenCapture.getOffsetY(),
                    screenCapture.getCellSize());
        } catch (Exception e) {
            Logger.error("Ошибка распознавания: " + e.getMessage());
            return null;
        }
    }

    /**
     * Сделать случайный ход
     */
    private void makeRandomMove(GameState state) {
        java.util.List<org.minesweeper.core.Cell> unknown = state.getUnknownCells();
        if (unknown.isEmpty()) {
            Logger.warn("Нет неизвестных клеток!");
            return;
        }

        java.util.Random rand = new java.util.Random();
        org.minesweeper.core.Cell cell = unknown.get(rand.nextInt(unknown.size()));
        Move randomMove = new Move(cell.getRow(), cell.getCol(), false, 0.7, "Случайный ход");

        Logger.warn("🎲 Случайный ход: " + randomMove);
        mouseController.executeMove(randomMove);
    }

    /**
     * Получить хэш текущего состояния поля
     */
    private String getBoardHash(GameState state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                org.minesweeper.core.Cell cell = state.getCell(i, j);
                if (cell.isRevealed()) {
                    sb.append(cell.getAdjacentMines());
                } else if (cell.isFlagged()) {
                    sb.append("F");
                } else {
                    sb.append("?");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Показать финальное состояние поля
     */
    private void showFinalBoard(GameState state) {
        Logger.info("\n📋 Финальное состояние поля:");
        state.printBoard();
    }

    /**
     * Вывод статистики
     */
    private void printStatistics() {
        Logger.info("=================================");
        Logger.info("📊 СТАТИСТИКА");
        Logger.info("Сыграно игр: " + gamesPlayed);
        Logger.info("Побед: " + gamesWon);
        Logger.info("Поражений: " + gamesLost);
        Logger.info("Процент побед: " + String.format("%.1f%%", getWinRate() * 100));
        Logger.info("=================================");
    }

    // Геттеры и сеттеры
    public boolean isRunning() { return running.get(); }
    public boolean isPaused() { return paused.get(); }

    public void setDelay(int ms) {
        if (ms >= 50) this.delayBetweenMoves = ms;
    }
    public int getDelay() { return delayBetweenMoves; }

    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public int getGamesLost() { return gamesLost; }

    public double getWinRate() {
        if (gamesPlayed == 0) return 0;
        return (double) gamesWon / gamesPlayed;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
        Logger.info("Стратегия изменена на: " + strategy.getName());
    }

    public void setGameParameters(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = mines;
    }

    public void calibrate(int offsetX, int offsetY, int cellSize) {
        screenCapture.setOffsetX(offsetX);
        screenCapture.setOffsetY(offsetY);
        screenCapture.setCellSize(cellSize);
        mouseController.setOffset(offsetX, offsetY);
        mouseController.setCellSize(cellSize);
        Logger.info("📐 Калибровка: offset=(" + offsetX + "," + offsetY + "), cellSize=" + cellSize);
    }

    public boolean isGameFinished() {
        return gamesWon + gamesLost > gamesPlayed; // или храните отдельный флаг
    }

    // Добавить флаг экстренной остановки
    private final AtomicBoolean emergencyStop = new AtomicBoolean(false);

    // В метод gameLoop() добавить проверку клавиши ESC
    private void checkEmergencyStop() {
        try {
            // Проверяем, нажата ли клавиша ESC
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != null) {
                // Можно проверить через Robot, но проще добавить слушатель
            }
        } catch (Exception e) {
            // Игнорируем
        }
    }

    // Добавить метод экстренной остановки
    public void emergencyStop() {
        Logger.warn("ЭКСТРЕННАЯ ОСТАНОВКА!");
        emergencyStop.set(true);
        running.set(false);
        paused.set(false);

        // Освобождаем мышь - перемещаем в угол экрана
        try {
            Robot tempRobot = new Robot();
            tempRobot.mouseMove(0, 0);
        } catch (AWTException e) {
            // Игнорируем
        }

        if (botThread != null) {
            botThread.interrupt();
        }
    }
}