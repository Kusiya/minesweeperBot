package org.minesweeper.execution;

import org.minesweeper.utils.Logger;
import org.minesweeper.utils.Config;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Контроллер для эмуляции действий мыши.
 * Позволяет боту физически кликать по клеткам в реальной игре.
 */
public class MouseController {
    private Robot robot;                    // Робот для эмуляции ввода
    private int cellSize;                    // Размер клетки в пикселях
    private Point boardOffset;               // Смещение доски на экране
    private int clickDelay;                  // Задержка между кликами (мс)
    private Logger logger;                    // Система логирования
    private boolean debugMode;                // Режим отладки (без реальных кликов)

    /**
     * Инициализация с параметрами экрана
     */
    public MouseController(int cellSize, Point boardOffset) throws AWTException {
        this(cellSize, boardOffset, 1000, false);
    }

    /**
     * Полная инициализация с дополнительными параметрами
     */
    public MouseController(int cellSize, Point boardOffset, int clickDelay, boolean debugMode)
            throws AWTException {
        this.robot = new Robot();
        this.cellSize = cellSize;
        this.boardOffset = boardOffset;
        this.clickDelay = clickDelay;
        this.debugMode = debugMode;
        this.logger = Logger.getInstance();

        // Загружаем настройки из конфига
        Config config = Config.getInstance();
        this.clickDelay = config.getClickDelay();

        logger.info("MouseController инициализирован: cellSize=" + cellSize +
                ", offset=" + boardOffset + ", delay=" + clickDelay);
    }

    /**
     * Клик по клетке с координатами (row, col)
     */
    public void clickCell(int row, int col) {
        if (debugMode) {
            logger.debug("[DEBUG] Клик по клетке (" + row + ", " + col + ")");
            return;
        }

        Point screenPos = getScreenCoordinates(row, col);
        moveAndClick(screenPos, InputEvent.BUTTON1_DOWN_MASK);
        logger.debug("Клик по клетке (" + row + ", " + col + ") в " + screenPos);
    }

    /**
     * Правый клик для установки флага
     */
    public void rightClickCell(int row, int col) {
        if (debugMode) {
            logger.debug("[DEBUG] Правый клик по клетке (" + row + ", " + col + ")");
            return;
        }

        Point screenPos = getScreenCoordinates(row, col);
        moveAndClick(screenPos, InputEvent.BUTTON3_DOWN_MASK);
        logger.debug("Правый клик по клетке (" + row + ", " + col + ") в " + screenPos);
    }

    /**
     * Двойной клик для быстрого открытия окружающих клеток
     */
    public void doubleClickCell(int row, int col) {
        if (debugMode) {
            logger.debug("[DEBUG] Двойной клик по клетке (" + row + ", " + col + ")");
            return;
        }

        Point screenPos = getScreenCoordinates(row, col);

        // Первый клик
        moveMouse(screenPos);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        // Небольшая задержка между кликами
        robot.delay(200);

        // Второй клик
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        logger.debug("Двойной клик по клетке (" + row + ", " + col + ") в " + screenPos);
    }

    /**
     * Перемещение мыши с последующим кликом
     */
    private void moveAndClick(Point point, int button) {
        moveMouse(point);
        robot.delay(clickDelay / 2);

        robot.mousePress(button);
        robot.delay(200);
        robot.mouseRelease(button);

        robot.delay(clickDelay);
    }

    /**
     * Перемещение мыши в указанную точку
     */
    private void moveMouse(Point point) {
        // Плавное перемещение (можно добавить анимацию)
        robot.mouseMove(point.x, point.y);
        robot.delay(200);
    }

    /**
     * Преобразование координат клетки в экранные координаты
     */
    private Point getScreenCoordinates(int row, int col) {
        int x = boardOffset.x + col * cellSize + cellSize / 2;
        int y = boardOffset.y + row * cellSize + cellSize / 2;
        return new Point(x, y);
    }

    /**
     * Калибровка: определение границ доски
     */
    public static BoardCalibrationResult calibrate() throws AWTException {
        Robot robot = new Robot();
        Logger logger = Logger.getInstance();

        logger.info("Начало калибровки. Наведите мышь на левый верхний угол доски...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Point topLeft = MouseInfo.getPointerInfo().getLocation();
        logger.info("Левый верхний угол: " + topLeft);

        logger.info("Наведите мышь на правый нижний угол доски...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Point bottomRight = MouseInfo.getPointerInfo().getLocation();
        logger.info("Правый нижний угол: " + bottomRight);

        // Вычисляем размер доски
        int boardWidth = bottomRight.x - topLeft.x;
        int boardHeight = bottomRight.y - topLeft.y;

        logger.info("Размер доски: " + boardWidth + "x" + boardHeight);

        // Запрашиваем количество клеток
        // В реальном приложении можно попросить пользователя ввести

        return new BoardCalibrationResult(topLeft, bottomRight, boardWidth, boardHeight);
    }

    /**
     * Установка режима отладки
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        logger.info("Режим отладки: " + (debugMode ? "включен" : "выключен"));
    }

    /**
     * Результат калибровки
     */
    public static class BoardCalibrationResult {
        public final Point topLeft;
        public final Point bottomRight;
        public final int width;
        public final int height;

        public BoardCalibrationResult(Point topLeft, Point bottomRight, int width, int height) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
            this.width = width;
            this.height = height;
        }

        /**
         * Вычисление размера клетки при известном количестве клеток
         */
        public int calculateCellSize(int rows, int cols) {
            int cellWidth = width / cols;
            int cellHeight = height / rows;
            // Предполагаем квадратные клетки
            return Math.min(cellWidth, cellHeight);
        }

        @Override
        public String toString() {
            return String.format("BoardCalibration{topLeft=%s, bottomRight=%s, size=%dx%d}",
                    topLeft, bottomRight, width, height);
        }
    }
}