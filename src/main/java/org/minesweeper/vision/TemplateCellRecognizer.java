package org.minesweeper.vision;

import java.awt.image.BufferedImage;

public class TemplateCellRecognizer implements CellRecognizer {
    private final TemplateLoader templateLoader;
    private final double matchThreshold = 30.0;

    public TemplateCellRecognizer(TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
    }

    @Override
    public int recognizeCell(BufferedImage cellImage) {
        if (templateLoader.getTemplateCount() == 0) {
            System.err.println("❌ Нет загруженных шаблонов!");
            return -2;
        }

        int bestMatch = -2;
        double bestDifference = Double.MAX_VALUE;

        BufferedImage smallCell = scaleImage(cellImage, 20, 20);

        for (var entry : templateLoader.getAllTemplates().entrySet()) {
            int value = entry.getKey();
            BufferedImage template = entry.getValue();

            BufferedImage smallTemplate = scaleImage(template, 20, 20);
            double difference = compareImages(smallCell, smallTemplate);

            if (difference < bestDifference) {
                bestDifference = difference;
                bestMatch = value;
            }
        }

        if (bestDifference > matchThreshold) {
            return -2;
        }

        return bestMatch;
    }

    @Override
    public int[][] recognizeBoard(BufferedImage fullImage, int rows, int cols,
                                  int offsetX, int offsetY, int cellSize) {
        int[][] board = new int[rows][cols];

        System.out.println("\n🔍 Анализ поля по шаблонам...");

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                try {
                    int x = offsetX + col * cellSize;
                    int y = offsetY + row * cellSize;

                    if (x + cellSize <= fullImage.getWidth() && y + cellSize <= fullImage.getHeight()) {
                        BufferedImage cellImage = fullImage.getSubimage(
                                x, y, cellSize, cellSize
                        );

                        board[row][col] = recognizeCell(cellImage);
                    } else {
                        board[row][col] = -2;
                    }
                } catch (Exception e) {
                    board[row][col] = -2;
                }
            }
        }

        // Выводим распознанное поле
        System.out.println("\n📊 Распознанное поле:");
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String val;
                if (board[row][col] == -2) val = "❓";
                else if (board[row][col] == -3) val = "🚩";
                else if (board[row][col] == -1) val = "💣";
                else val = String.valueOf(board[row][col]);
                System.out.print(val + " ");
            }
            System.out.println();
        }

        return board;
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        scaled.getGraphics().drawImage(original, 0, 0, width, height, null);
        return scaled;
    }

    private double compareImages(BufferedImage img1, BufferedImage img2) {
        double totalDiff = 0;
        int pixels = 0;

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int gray1 = getGrayValue(rgb1);
                int gray2 = getGrayValue(rgb2);

                totalDiff += Math.abs(gray1 - gray2);
                pixels++;
            }
        }

        return totalDiff / pixels;
    }

    private int getGrayValue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }
}