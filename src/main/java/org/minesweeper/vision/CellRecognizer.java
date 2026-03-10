package org.minesweeper.vision;

import java.awt.image.BufferedImage;

/**
 * Упрощенный распознаватель, использующий анализ цветов
 */
public class CellRecognizer {

    /**
     * Распознать одну клетку по цвету
     * @return -1 мина, 0-8 цифры, -2 закрыто, -3 флаг
     */
    public int recognizeCell(BufferedImage cellImage) {
        try {
            // Берем цвет в центре клетки
            int centerX = cellImage.getWidth() / 2;
            int centerY = cellImage.getHeight() / 2;
            int centerColor = cellImage.getRGB(centerX, centerY);

            // Получаем RGB компоненты
            int red = (centerColor >> 16) & 0xFF;
            int green = (centerColor >> 8) & 0xFF;
            int blue = centerColor & 0xFF;

            // Лог для отладки
            // System.out.printf("RGB: (%d, %d, %d)%n", red, green, blue);

            // Цвета для стандартного сапёра в Windows
            // Закрытая клетка (серая)
            if (red > 180 && green > 180 && blue > 180) {
                if (Math.abs(red - green) < 20 && Math.abs(green - blue) < 20) {
                    return -2; // закрыто
                }
            }

            // Пустая открытая (белая)
            if (red > 240 && green > 240 && blue > 240) {
                return 0;
            }

            // Синий - цифра 1
            if (red < 100 && green < 150 && blue > 200) {
                return 1;
            }

            // Зеленый - цифра 2
            if (red < 100 && green > 150 && blue < 100) {
                return 2;
            }

            // Красный - цифра 3
            if (red > 200 && green < 100 && blue < 100) {
                return 3;
            }

            // Темно-синий - цифра 4
            if (red < 100 && green < 100 && blue > 150 && blue < 200) {
                return 4;
            }

            // Темно-красный - цифра 5
            if (red > 150 && red < 200 && green < 80 && blue < 80) {
                return 5;
            }

            // Бирюзовый - цифра 6
            if (red < 100 && green > 150 && blue > 150) {
                return 6;
            }

            // Черный - цифра 7
            if (red < 50 && green < 50 && blue < 50) {
                return 7;
            }

            // Темно-серый - цифра 8
            if (red > 100 && red < 150 && green > 100 && green < 150 && blue > 100 && blue < 150) {
                if (Math.abs(red - green) < 20 && Math.abs(green - blue) < 20) {
                    return 8;
                }
            }

            // Флаг (красный)
            if (red > 200 && green < 100 && blue < 100) {
                // Проверяем, не цифра ли это 3
                if (red > 240) {
                    return -3; // флаг
                }
            }

        } catch (Exception e) {
            // Игнорируем ошибки распознавания
        }

        return -2; // неизвестно
    }

    /**
     * Распознать всё поле
     */
    public int[][] recognizeBoard(BufferedImage fullImage, int rows, int cols,
                                  int offsetX, int offsetY, int cellSize) {
        int[][] board = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                try {
                    int x = offsetX + col * cellSize;
                    int y = offsetY + row * cellSize;

                    // Проверяем, не выходит ли за границы
                    if (x + cellSize <= fullImage.getWidth() && y + cellSize <= fullImage.getHeight()) {
                        // Вырезаем клетку
                        BufferedImage cellImage = fullImage.getSubimage(
                                x, y, cellSize, cellSize
                        );

                        // Распознаем
                        board[row][col] = recognizeCell(cellImage);
                    } else {
                        board[row][col] = -2;
                    }
                } catch (Exception e) {
                    board[row][col] = -2;
                }
            }
        }

        return board;
    }
}

