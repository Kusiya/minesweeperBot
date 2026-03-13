package org.minesweeper.vision;

import org.minesweeper.utils.Config;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCapture {
    private Robot robot;
    private Rectangle gameArea;
    private int cellSize;
    private int offsetX;
    private int offsetY;
    private int rows;
    private int cols;
    private Config config;

    public ScreenCapture() throws AWTException {
        this.robot = new Robot();
        this.config = Config.getInstance();

        // Загружаем значения из конфига
        this.offsetX = config.getInt("offsetX", 0);
        this.offsetY = config.getInt("offsetY", 0);
        this.cellSize = config.getInt("cellSize", 30);
        this.rows = config.getInt("rows", 9);
        this.cols = config.getInt("cols", 9);

        updateGameArea();

        System.out.println("🖥️ ScreenCapture инициализирован");
        System.out.println("   offset: (" + offsetX + "," + offsetY + ")");
        System.out.println("   cellSize: " + cellSize);
        System.out.println("   поле: " + rows + "x" + cols);
    }

    private void updateGameArea() {
        int width = cols * cellSize;
        int height = rows * cellSize;
        this.gameArea = new Rectangle(offsetX, offsetY, width, height);

        System.out.println("📐 Область захвата: (" + offsetX + "," + offsetY +
                ") размер " + width + "x" + height);
    }

    public BufferedImage captureGameArea() {
        BufferedImage screenshot = robot.createScreenCapture(gameArea);
        System.out.println("📸 Скриншот сделан: " + screenshot.getWidth() + "x" + screenshot.getHeight());
        return screenshot;
    }

    public BufferedImage getCellImage(BufferedImage fullImage, int row, int col) {
        int x = col * cellSize;
        int y = row * cellSize;

        if (x + cellSize <= fullImage.getWidth() && y + cellSize <= fullImage.getHeight()) {
            return fullImage.getSubimage(x, y, cellSize, cellSize);
        } else {
            System.err.println("❌ Клетка [" + row + "," + col + "] вне границ: x=" + x + ", y=" + y);
            return null;
        }
    }

    // Обновленный метод калибровки
    public void setCalibration(int offsetX, int offsetY, int cellSize, int rows, int cols) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.cellSize = cellSize;
        this.rows = rows;
        this.cols = cols;

        // Сохраняем в конфиг
        config.setInt("offsetX", offsetX);
        config.setInt("offsetY", offsetY);
        config.setInt("cellSize", cellSize);
        config.setInt("rows", rows);
        config.setInt("cols", cols);
        config.save();

        updateGameArea();
    }

    // Геттеры
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getCellSize() { return cellSize; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}