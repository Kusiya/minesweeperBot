package org.minesweeper.vision;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Захватывает изображение игрового поля с экрана
 */
public class ScreenCapture {
    private Robot robot;
    private Rectangle gameArea;      // область игрового поля
    private int cellSize;             // размер клетки в пикселях
    private int offsetX;              // отступ слева
    private int offsetY;              // отступ сверху

    public ScreenCapture() throws AWTException {
        this.robot = new Robot();

        // Значения по умолчанию (нужно будет откалибровать под вашу игру)
        this.gameArea = new Rectangle(100, 100, 400, 400);
        this.cellSize = 30;
        this.offsetX = 100;
        this.offsetY = 100;
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
    public void calibrate(int expectedRows, int expectedCols) throws AWTException {
        System.out.println("Калибровка... Наведите мышь на левый верхний угол поля и нажмите Enter");
        // Здесь можно добавить ожидание нажатия клавиши и получение координат мыши
        // Для простоты пока оставим заглушку
    }

    // Геттеры и сеттеры для калибровки
    public void setGameArea(Rectangle area) { this.gameArea = area; }
    public void setCellSize(int size) { this.cellSize = size; }
    public void setOffsetX(int x) { this.offsetX = x; }
    public void setOffsetY(int y) { this.offsetY = y; }
}
