package org.minesweeper.vision;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Распознаёт содержимое клетки по изображению
 */
public class CellRecognizer {
    private TemplateLoader templateLoader;
    private double matchThreshold = 30.0; // порог совпадения

    public CellRecognizer(TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
    }

    /**
     * Распознать одну клетку
     * @return -1 мина, 0-8 цифры, -2 закрыто, -3 флаг
     */
    public int recognizeCell(BufferedImage cellImage) {
        int bestMatch = -2; // по умолчанию - закрыто
        double bestDifference = Double.MAX_VALUE;

        // Сравниваем со всеми шаблонами
        for (Map.Entry<Integer, BufferedImage> entry : templateLoader.getAllTemplates().entrySet()) {
            int value = entry.getKey();
            BufferedImage template = entry.getValue();

            double difference = compareImages(cellImage, template);

            if (difference < bestDifference) {
                bestDifference = difference;
                bestMatch = value;
            }
        }

        // Если разница слишком велика, считаем что не распознали
        if (bestDifference > matchThreshold) {
            return -2; // unknown
        }

        return bestMatch;
    }

    /**
     * Сравнить два изображения
     * @return средняя разница в цвете (чем меньше, тем больше похожи)
     */
    private double compareImages(BufferedImage img1, BufferedImage img2) {
        // Приводим к одинаковому размеру
        BufferedImage scaled1 = scaleImage(img1, 20, 20);
        BufferedImage scaled2 = scaleImage(img2, 20, 20);

        double totalDiff = 0;
        int pixels = 0;

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                int rgb1 = scaled1.getRGB(x, y);
                int rgb2 = scaled2.getRGB(x, y);

                // Переводим в градации серого
                int gray1 = getGrayValue(rgb1);
                int gray2 = getGrayValue(rgb2);

                totalDiff += Math.abs(gray1 - gray2);
                pixels++;
            }
        }

        return totalDiff / pixels;
    }

    /**
     * Масштабировать изображение
     */
    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaled.getGraphics().drawImage(original, 0, 0, width, height, null);
        return scaled;
    }

    /**
     * Получить значение яркости из RGB
     */
    private int getGrayValue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }

    /**
     * Распознать всё поле целиком
     * @return матрица значений
     */
    public int[][] recognizeBoard(BufferedImage fullImage, int rows, int cols) {
        int[][] board = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Здесь нужно получить изображение каждой клетки
                // Для простоты пока заполняем случайными значениями
                board[i][j] = -2; // все закрыто
            }
        }

        return board;
    }
}

