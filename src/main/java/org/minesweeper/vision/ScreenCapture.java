package org.minesweeper.vision;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Захватывает изображение игрового поля с экрана
 */
public class ScreenCapture {
    private final Robot robot;
    private Rectangle gameArea;      // область игрового поля
    private int cellSize;             // размер клетки в пикселях
    private int offsetX;              // отступ слева
    private int offsetY;              // отступ сверху
    private int rows;                 // количество строк (для обновления gameArea)
    private int cols;                 // количество столбцов (для обновления gameArea)

    public ScreenCapture() throws AWTException {
        this.robot = new Robot();

        // Значения по умолчанию (нужно будет откалибровать под вашу игру)
        this.cellSize = 30;
        this.offsetX = 100;
        this.offsetY = 100;
        this.rows = 9;
        this.cols = 9;
        updateGameArea();
    }

    /**
     * Обновить область захвата на основе текущих параметров
     */
    private void updateGameArea() {
        this.gameArea = new Rectangle(offsetX, offsetY, cols * cellSize, rows * cellSize);
    }

    /**
     * Захватить весь экран
     */
    public BufferedImage captureFullScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        return robot.createScreenCapture(screenRect);
    }

    /**
     * Захватить только область игры
     */
    public BufferedImage captureGameArea() {
        return robot.createScreenCapture(gameArea);
    }

    /**
     * Получить изображение конкретной клетки
     */
    public BufferedImage getCellImage(BufferedImage fullImage, int row, int col) {
        int x = offsetX + col * cellSize;
        int y = offsetY + row * cellSize;
        return fullImage.getSubimage(x, y, cellSize, cellSize);
    }

    /**
     * Калибровка: определить размер клетки и координаты
     */
    public void calibrate(int expectedRows, int expectedCols) {
        this.rows = expectedRows;
        this.cols = expectedCols;
        updateGameArea();
        System.out.println("Калибровка завершена. Размер поля: " + rows + "x" + cols);
    }

    // ============ ГЕТТЕРЫ (нужны для MinesweeperBot) ============

    /**
     * Получить отступ по X
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Получить отступ по Y
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Получить размер клетки
     */
    public int getCellSize() {
        return cellSize;
    }

    /**
     * Получить количество строк
     */
    public int getRows() {
        return rows;
    }

    /**
     * Получить количество столбцов
     */
    public int getCols() {
        return cols;
    }

    /**
     * Получить область захвата
     */
    public Rectangle getGameArea() {
        return gameArea;
    }

    // ============ СЕТТЕРЫ ============

    /**
     * Установить область игры
     */
    public void setGameArea(Rectangle area) {
        this.gameArea = area;
        // Обновляем offset и размеры из области
        this.offsetX = area.x;
        this.offsetY = area.y;
        if (cols > 0) {
            this.cellSize = area.width / cols;
        }
    }

    /**
     * Установить размер клетки
     */
    public void setCellSize(int size) {
        this.cellSize = size;
        updateGameArea();
    }

    /**
     * Установить отступ по X
     */
    public void setOffsetX(int x) {
        this.offsetX = x;
        updateGameArea();
    }

    /**
     * Установить отступ по Y
     */
    public void setOffsetY(int y) {
        this.offsetY = y;
        updateGameArea();
    }

    /**
     * Установить количество строк
     */
    public void setRows(int rows) {
        this.rows = rows;
        updateGameArea();
    }

    /**
     * Установить количество столбцов
     */
    public void setCols(int cols) {
        this.cols = cols;
        updateGameArea();
    }

    /**
     * Полная калибровка с указанием всех параметров
     */
    public void setCalibration(int offsetX, int offsetY, int cellSize, int rows, int cols) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.cellSize = cellSize;
        this.rows = rows;
        this.cols = cols;
        updateGameArea();
    }

    @Override
    public String toString() {
        return String.format("ScreenCapture{offset=(%d,%d), cellSize=%d, field=%dx%d, area=%s}",
                offsetX, offsetY, cellSize, rows, cols, gameArea);
    }
}