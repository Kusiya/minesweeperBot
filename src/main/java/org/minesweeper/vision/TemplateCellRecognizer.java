package org.minesweeper.vision;

import org.minesweeper.core.GameState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class TemplateCellRecognizer {
    private final TemplateLoader templateLoader;

    public static final int UNKNOWN = -2;
    public static final int MINE = -1;
    public static final int FLAG = -3;
    public static final int EMPTY = 0;

    private boolean debugMode = true;

    // Индивидуальные пороги для каждой цифры (на основе ваших данных)
    private static final Map<Integer, Double> DIGIT_THRESHOLDS = new HashMap<>();
    static {
        DIGIT_THRESHOLDS.put(1, 38.0);
        DIGIT_THRESHOLDS.put(2, 40.0);
        DIGIT_THRESHOLDS.put(3, 37.0); // 3 хорошо определяется
        DIGIT_THRESHOLDS.put(4, 43.0);
        DIGIT_THRESHOLDS.put(5, 48.0);
        DIGIT_THRESHOLDS.put(6, 41.0);
        DIGIT_THRESHOLDS.put(7, 45.0);
        DIGIT_THRESHOLDS.put(8, 39.0);
    }

    // Веса для каждой цифры (чем меньше вес, тем выше приоритет)
    private static final Map<Integer, Double> DIGIT_WEIGHTS = new HashMap<>();
    static {
        DIGIT_WEIGHTS.put(1, 1.0);
        DIGIT_WEIGHTS.put(2, 0.98);
        DIGIT_WEIGHTS.put(3, 0.95); // 3 имеет высокий приоритет
        DIGIT_WEIGHTS.put(4, 1.02);
        DIGIT_WEIGHTS.put(5, 1.05);
        DIGIT_WEIGHTS.put(6, 0.98);
        DIGIT_WEIGHTS.put(7, 1.02);
        DIGIT_WEIGHTS.put(8, 0.97);
    }

    private static final double EMPTY_THRESHOLD = 28.0;  // Повысили до 28
    private static final double CLOSED_THRESHOLD = 45.0; // CLOSED обычно 40-50

    public TemplateCellRecognizer(TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
        System.out.println("🖼️ TemplateCellRecognizer инициализирован");
    }

    public int recognizeCell(BufferedImage cellImage, int row, int col) {
        if (debugMode && row >= 0 && col >= 0) {
            System.out.print("\n🔍 Клетка [" + row + "," + col + "]");
        }

        // 1. Анализируем яркость и вариативность
        int brightness = getAverageBrightness(cellImage);
        double colorVariance = getColorVariance(cellImage);

        if (debugMode) {
            System.out.print(" ярк=" + brightness + " var=" + String.format("%.2f", colorVariance));
        }

        // 2. Проверка на флаг (красный)
        if (isRed(cellImage)) {
            if (debugMode) System.out.println(" -> 🚩 ФЛАГ");
            return FLAG;
        }
        if (brightness < 70 && colorVariance < 30) {
            if (debugMode) System.out.println(" -> 💥 МИНА");
            return MINE;
        }


        // 3. Получаем ВСЕ шаблоны
        Map<Integer, List<BufferedImage>> allTemplates = templateLoader.getAllTemplates();
        Map<Integer, Double> bestDiffs = new HashMap<>();

        // Для каждого значения находим ЛУЧШЕЕ совпадение среди всех его шаблонов
        for (Map.Entry<Integer, List<BufferedImage>> entry : allTemplates.entrySet()) {
            int value = entry.getKey();
            List<BufferedImage> templates = entry.getValue();

            if (templates.isEmpty()) continue;

            double bestDiff = Double.MAX_VALUE;
            for (BufferedImage template : templates) {
                double diff = compareImages(cellImage, template);
                if (diff < bestDiff) {
                    bestDiff = diff;
                }
            }
            bestDiffs.put(value, bestDiff);
        }

        // 4. Выводим результаты
        double closedDiff = bestDiffs.getOrDefault(UNKNOWN, 999.0);
        double emptyDiff = bestDiffs.getOrDefault(EMPTY, 999.0);

        if (debugMode) {
            System.out.println();
            System.out.printf("      CLOSED: %.2f%n", closedDiff);
            System.out.printf("      EMPTY:  %.2f%n", emptyDiff);

            // Сортируем цифры по разнице
            List<Map.Entry<Integer, Double>> sortedDigits = new ArrayList<>();
            for (int num = 1; num <= 8; num++) {
                if (bestDiffs.containsKey(num)) {
                    sortedDigits.add(new AbstractMap.SimpleEntry<>(num, bestDiffs.get(num)));
                }
            }
            sortedDigits.sort(Map.Entry.comparingByValue());

            for (Map.Entry<Integer, Double> e : sortedDigits) {
                System.out.printf("      %d:      %.2f%n", e.getKey(), e.getValue());
            }
        }

        // 5. ПРОВЕРКА КАЖДОЙ ЦИФРЫ ПО ИНДИВИДУАЛЬНОМУ ПОРОГУ
        for (int num = 1; num <= 8; num++) {
            if (bestDiffs.containsKey(num)) {
                double rawDiff = bestDiffs.get(num);
                double threshold = DIGIT_THRESHOLDS.getOrDefault(num, 40.0);
                double weight = DIGIT_WEIGHTS.getOrDefault(num, 1.0);

                // Взвешенная разница
                double weightedDiff = rawDiff * weight;

                if (weightedDiff < threshold) {
                    if (debugMode) {
                        System.out.printf("   👉 ЦИФРА %d (разница %.2f, вес %.2f, взвешенная %.2f)%n",
                                num, rawDiff, weight, weightedDiff);
                    }
                    return num;
                }
            }
        }

        // 6. Проверка на закрытую клетку
        if (closedDiff < CLOSED_THRESHOLD) {
            if (debugMode) System.out.println("   👉 ЗАКРЫТАЯ");
            return UNKNOWN;
        }

        // 7. Проверка на пустую клетку
        if (emptyDiff < EMPTY_THRESHOLD) {
            if (debugMode) System.out.println("   👉 ПУСТАЯ");
            return EMPTY;
        }

        // 8. Если ничего не подошло - проверяем по яркости
        if (brightness > 200 && colorVariance < 20) {
            if (debugMode) System.out.println("   👉 ПУСТАЯ (по яркости)");
            return EMPTY;
        }

        if (brightness > 100 && brightness < 180 && colorVariance < 30) {
            if (debugMode) System.out.println("   👉 ЗАКРЫТАЯ (по яркости)");
            return UNKNOWN;
        }

        if (debugMode) System.out.println("   👉 ПО УМОЛЧАНИЮ = ЗАКРЫТАЯ");
        return UNKNOWN;
    }

    private double getColorVariance(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        // Собираем все значения яркости
        java.util.ArrayList<Double> values = new java.util.ArrayList<>();

        for (int x = 0; x < w; x += 2) {
            for (int y = 0; y < h; y += 2) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                double brightness = (r + g + b) / 3.0;
                values.add(brightness);
            }
        }

        // Вычисляем среднее
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        double mean = sum / values.size();

        // Вычисляем дисперсию
        double variance = 0;
        for (double v : values) {
            variance += Math.pow(v - mean, 2);
        }
        variance /= values.size();

        return Math.sqrt(variance);
    }

    private int getAverageBrightness(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        long total = 0;
        int pixels = 0;

        for (int x = 0; x < w; x += 2) {
            for (int y = 0; y < h; y += 2) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                total += (r + g + b) / 3;
                pixels++;
            }
        }

        return (int)(total / pixels);
    }

    private boolean isRed(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int redPixels = 0;
        int total = 0;

        for (int x = 0; x < w; x += 2) {
            for (int y = 0; y < h; y += 2) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (r > 150 && g < 100 && b < 100) {
                    redPixels++;
                }
                total++;
            }
        }

        return redPixels > total / 3;
    }

    private double compareImages(BufferedImage img1, BufferedImage img2) {
        BufferedImage scaled1 = scaleImage(img1, 20, 20);
        BufferedImage scaled2 = scaleImage(img2, 20, 20);

        double totalDiff = 0;
        int pixels = 0;

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                int rgb1 = scaled1.getRGB(x, y);
                int rgb2 = scaled2.getRGB(x, y);

                int gray1 = getGrayValue(rgb1);
                int gray2 = getGrayValue(rgb2);

                double weight = 1.0;
                double dx = (x - 10) / 10.0;
                double dy = (y - 10) / 10.0;
                double dist = Math.sqrt(dx*dx + dy*dy);

                if (dist < 0.7) {
                    weight = 2.0; // центр в 2 раза важнее
                } else if (dist > 1.2) {
                    weight = 0.5; // края менее важны
                }

                totalDiff += Math.abs(gray1 - gray2) * weight;
                pixels += weight;
            }
        }

        return totalDiff / pixels;
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    private int getGrayValue(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }

    public int[][] recognizeBoard(BufferedImage fullImage, int rows, int cols,
                                  int offsetX, int offsetY, int cellSize) {
        int[][] board = new int[rows][cols];

        System.out.println("\n🔍 АНАЛИЗ ПОЛЯ:");
        System.out.println("   Размер скриншота: " + fullImage.getWidth() + "x" + fullImage.getHeight());
        System.out.println("──────────────────────────");

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                try {
                    int x = col * cellSize;
                    int y = row * cellSize;

                    if (x + cellSize <= fullImage.getWidth() &&
                            y + cellSize <= fullImage.getHeight()) {

                        BufferedImage cellImage = fullImage.getSubimage(x, y, cellSize, cellSize);
                        int value = recognizeCell(cellImage, row, col);
                        board[row][col] = value;
                    } else {
                        board[row][col] = UNKNOWN;
                    }
                } catch (Exception e) {
                    board[row][col] = UNKNOWN;
                }
            }
        }

        printBoard(board);
        return board;
    }

    private void printBoard(int[][] board) {
        System.out.println("\n📋 РАСПОЗНАННОЕ ПОЛЕ:");
        for (int i = 0; i < board.length; i++) {
            System.out.print("   ");
            for (int j = 0; j < board[i].length; j++) {
                String val;
                switch (board[i][j]) {
                    case MINE: val = "💣"; break;
                    case FLAG: val = "🚩"; break;
                    case UNKNOWN: val = "❓"; break;
                    case EMPTY: val = "·"; break;
                    default: val = String.valueOf(board[i][j]);
                }
                System.out.print(val + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }
}