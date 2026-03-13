package org.minesweeper.bot;

import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.Strategy;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.vision.ScreenCapture;
import org.minesweeper.execution.MouseController;
import org.minesweeper.utils.Logger;
import org.minesweeper.vision.TemplateCellRecognizer;
import org.minesweeper.vision.TemplateLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    private final TemplateCellRecognizer cellRecognizer;
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
        boolean gameFinished = false;

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
                    running.set(false);
                    break;
                }

                if (state.checkWinCondition()) {
                    Logger.info("🎉 ПОБЕДА! Все мины найдены.");
                    gamesWon++;
                    showFinalBoard(state);
                    running.set(false);
                    break;
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

                // 6. Выбор хода (закрытые клетки)
                Move move = strategy.nextMove(state);

                if (move == null) {
                    Logger.warn("Нет безопасных ходов. Пробуем случайный...");
                    move = getRandomMove(state);
                }

                if (move != null) {
                    // ЖЕСТКАЯ ПРОВЕРКА - НИКАКИХ ОТКРЫТЫХ КЛЕТОК!
                    org.minesweeper.core.Cell targetCell = state.getCell(move.getRow(), move.getCol());

                    if (targetCell.isRevealed()) {
                        Logger.error("❌❌❌ КРИТИЧЕСКАЯ ОШИБКА: Стратегия вернула УЖЕ ОТКРЫТУЮ клетку!");
                        Logger.error("   Клетка [" + move.getRow() + "," + move.getCol() + "] уже открыта со значением " + targetCell.getAdjacentMines());

                        // Находим любую НЕОТКРЫТУЮ клетку
                        java.util.List<org.minesweeper.core.Cell> unknown = new ArrayList<>();
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < cols; j++) {
                                org.minesweeper.core.Cell cell = state.getCell(i, j);
                                if (!cell.isRevealed() && !cell.isFlagged()) {
                                    unknown.add(cell);
                                }
                            }
                        }

                        if (!unknown.isEmpty()) {
                            java.util.Random rand = new java.util.Random();
                            org.minesweeper.core.Cell newCell = unknown.get(rand.nextInt(unknown.size()));
                            move = new Move(newCell.getRow(), newCell.getCol(), false, 0.9,
                                    "Аварийный выбор после ошибки");
                            Logger.info("✅ Выбрана аварийная клетка: [" + newCell.getRow() + "," + newCell.getCol() + "]");
                        } else {
                            Logger.error("❌ Нет неизвестных клеток! Игра зависла.");
                            running.set(false);
                            break;
                        }
                    }

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
            Logger.info("🏁 Игра завершена. Возврат в меню...");

            // Небольшая пауза перед возвратом в меню
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Проверка, что ход ведет на НЕОТКРЫТУЮ клетку
     */
    private boolean isValidMove(GameState state, Move move) {
        if (move == null) return false;

        int row = move.getRow();
        int col = move.getCol();

        // Проверяем границы
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            Logger.warn("Ход за пределами поля: (" + row + "," + col + ")");
            return false;
        }

        org.minesweeper.core.Cell cell = state.getCell(row, col);

        // КЛЮЧЕВОЕ: не открываем уже открытые клетки
        if (cell.isRevealed() && !move.isFlag()) {
            Logger.debug("Пропускаем уже открытую клетку: (" + row + "," + col + ")");
            return false;
        }

        // Не ставим флаг на открытую клетку
        if (cell.isRevealed() && move.isFlag()) {
            Logger.debug("Нельзя поставить флаг на открытую клетку");
            return false;
        }

        return true;
    }

    /**
     * Умный выбор хода - только новые клетки
     */
    private Move getIntelligentMove(GameState state) {
        Move move = strategy.nextMove(state);

        // Если стратегия вернула уже открытую клетку - ищем другой ход
        if (move != null && !isValidMove(state, move)) {
            Logger.debug("Стратегия вернула уже открытую клетку, ищем другой ход...");

            // Пробуем найти другой ход через базовую стратегию
            BasicStrategy basic = new BasicStrategy();
            move = basic.nextMove(state);

            // Если все равно открытая - ищем случайный среди НОВЫХ
            if (move != null && !isValidMove(state, move)) {
                move = getRandomMove(state);
            }
        }

        return move;
    }

    /**
     * Случайный ход ТОЛЬКО по НОВЫМ клеткам
     */
    private Move getRandomMove(GameState state) {
        java.util.List<org.minesweeper.core.Cell> unknown = state.getUnknownCells();

        // Фильтруем только НЕОТКРЫТЫЕ клетки
        java.util.List<org.minesweeper.core.Cell> available = new ArrayList<>();
        for (org.minesweeper.core.Cell cell : unknown) {
            if (!cell.isRevealed() && !cell.isFlagged()) {
                available.add(cell);
            }
        }

        if (available.isEmpty()) {
            Logger.warn("Нет неизвестных клеток!");
            return null;
        }

        java.util.Random rand = new java.util.Random();
        org.minesweeper.core.Cell cell = available.get(rand.nextInt(available.size()));

        return new Move(cell.getRow(), cell.getCol(), false, 0.7,
                "Случайный ход по новой клетке");
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

    public void calibrate(int offsetX, int offsetY, int cellSize, int rows, int cols) {
        screenCapture.setCalibration(offsetX, offsetY, cellSize, rows, cols);
        mouseController.setOffset(offsetX, offsetY);
        mouseController.setCellSize(cellSize);

        // Обновляем параметры игры
        this.rows = rows;
        this.cols = cols;
        // totalMines оставляем как есть или можно тоже спросить

        Logger.info("📐 Калибровка: offset=(" + offsetX + "," + offsetY +
                "), cellSize=" + cellSize + ", поле=" + rows + "x" + cols);
    }

    public boolean isGameFinished() {
        return gamesWon + gamesLost > gamesPlayed; // или храните отдельный флаг
    }

    // Добавить флаг экстренной остановки
    private final AtomicBoolean emergencyStop = new AtomicBoolean(false);

    // Добавить метод экстренной остановки
    public void emergencyStop() {
        Logger.warn("🚨 ЭКСТРЕННАЯ ОСТАНОВКА!");
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