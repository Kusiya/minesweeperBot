package org.minesweeper.vision;

import org.minesweeper.utils.Logger;
import org.minesweeper.utils.Config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Захват экрана для получения изображения игрового поля.
 */
public class ScreenCapture {
    private Robot robot;
    private Rectangle screenRect;
    private Logger logger;
    private Config config;

    public ScreenCapture() throws AWTException {
        this.robot = new Robot();
        this.logger = Logger.getInstance();
        this.config = Config.getInstance();
        this.screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        logger.info("ScreenCapture инициализирован, размер экрана: " + screenRect.width + "x" + screenRect.height);
    }

    /**
     * Захват всего экрана
     */
    public BufferedImage captureScreen() {
        BufferedImage screenshot = robot.createScreenCapture(screenRect);
        logger.debug("Сделан скриншот экрана");
        return screenshot;
    }

    /**
     * Захват области экрана
     */
    public BufferedImage captureArea(Rectangle area) {
        BufferedImage screenshot = robot.createScreenCapture(area);
        logger.debug("Сделан скриншот области: " + area);
        return screenshot;
    }

    /**
     * Захват области игрового поля
     */
    public BufferedImage captureBoardArea(Rectangle boardBounds) {
        return captureArea(boardBounds);
    }

    /**
     * Захват конкретной клетки
     */
    public BufferedImage captureCell(int row, int col, int cellSize, Point boardOffset) {
        int x = boardOffset.x + col * cellSize;
        int y = boardOffset.y + row * cellSize;
        Rectangle cellRect = new Rectangle(x, y, cellSize, cellSize);

        return captureArea(cellRect);
    }

    /**
     * Захват всех клеток доски
     */
    public BufferedImage[][] captureAllCells(int rows, int cols, int cellSize, Point boardOffset) {
        BufferedImage[][] cells = new BufferedImage[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = captureCell(i, j, cellSize, boardOffset);
            }
        }

        logger.debug("Захвачено " + (rows * cols) + " клеток");
        return cells;
    }

    /**
     * Сохранение скриншота в файл
     */
    public void saveScreenshot(BufferedImage image, String filename) {
        try {
            File outputFile = new File("screenshots/" + filename);
            outputFile.getParentFile().mkdirs();
            ImageIO.write(image, "png", outputFile);
            logger.info("Скриншот сохранен: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Ошибка сохранения скриншота", e);
        }
    }

    /**
     * Поиск игрового поля на экране
     */
    public Rectangle findBoardArea() {
        logger.info("Поиск игрового поля на экране...");

        BufferedImage screen = captureScreen();
        int width = screen.getWidth();
        int height = screen.getHeight();

        // Поиск характерных цветов сапера
        int boardTop = -1, boardBottom = -1, boardLeft = -1, boardRight = -1;

        // Проходим по строкам в поиске границ
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = screen.getRGB(x, y);
                Color color = new Color(rgb);

                // Проверяем типичные цвета сапера
                if (isBoardColor(color)) {
                    if (boardTop == -1) boardTop = y;
                    boardBottom = y;
                    if (boardLeft == -1 || x < boardLeft) boardLeft = x;
                    if (x > boardRight) boardRight = x;
                }
            }
        }

        if (boardTop != -1 && boardBottom != -1 && boardLeft != -1 && boardRight != -1) {
            Rectangle boardArea = new Rectangle(
                    boardLeft, boardTop,
                    boardRight - boardLeft + 1,
                    boardBottom - boardTop + 1
            );
            logger.info("Найдена область доски: " + boardArea);
            return boardArea;
        }

        logger.warning("Не удалось найти область доски");
        return null;
    }

    /**
     * Проверка, является ли цвет цветом доски сапера
     */
    private boolean isBoardColor(Color color) {
        // Типичные цвета для сапера (можно настроить)
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // Серые тона
        if (Math.abs(r - g) < 20 && Math.abs(g - b) < 20) {
            return true;
        }

        return false;
    }

    /**
     * Определение размера клетки
     */
    public int detectCellSize(Rectangle boardArea, int expectedRows, int expectedCols) {
        int width = boardArea.width;
        int height = boardArea.height;

        int cellWidth = width / expectedCols;
        int cellHeight = height / expectedRows;

        // Предполагаем квадратные клетки
        int cellSize = Math.min(cellWidth, cellHeight);

        logger.info("Определен размер клетки: " + cellSize + "px");
        return cellSize;
    }

    /**
     * Определение границ доски с помощью шаблонов
     */
    public BoardCalibrationResult calibrateWithTemplates() {
        logger.info("Калибровка с использованием шаблонов...");

        // Здесь можно реализовать более сложную калибровку
        // с использованием поиска шаблонов углов доски

        return null;
    }

    /**
     * Класс для результатов калибровки
     */
    public static class BoardCalibrationResult {
        public final Rectangle boardArea;
        public final int cellSize;
        public final int rows;
        public final int cols;

        public BoardCalibrationResult(Rectangle boardArea, int cellSize, int rows, int cols) {
            this.boardArea = boardArea;
            this.cellSize = cellSize;
            this.rows = rows;
            this.cols = cols;
        }

        public Point getBoardOffset() {
            return new Point(boardArea.x, boardArea.y);
        }

        @Override
        public String toString() {
            return String.format("BoardCalibration{area=%s, cellSize=%d, grid=%dx%d}",
                    boardArea, cellSize, rows, cols);
        }
    }
}