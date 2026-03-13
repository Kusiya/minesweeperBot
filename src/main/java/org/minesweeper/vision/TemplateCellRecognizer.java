package org.minesweeper.vision;

import org.minesweeper.utils.Logger;
import org.minesweeper.utils.Config;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Распознавание клеток по шаблонам.
 * Сравнивает скриншот клетки с шаблонами цифр, мин и т.д.
 */
public class TemplateCellRecognizer implements CellRecognizer {
    private TemplateLoader templateLoader;
    private double similarityThreshold;
    private Logger logger;
    private Config config;
    private Map<String, Double> templateCache;

    public TemplateCellRecognizer(TemplateLoader loader) {
        this.templateLoader = loader;
        this.logger = Logger.getInstance();
        this.config = Config.getInstance();
        this.similarityThreshold = config.getSimilarityThreshold();
        this.templateCache = new HashMap<>();
    }

    @Override
    public CellState recognize(BufferedImage cellImage) {
        RecognitionResult result = recognizeWithConfidence(cellImage);
        return result.getState();
    }

    @Override
    public RecognitionResult recognizeWithConfidence(BufferedImage cellImage) {
        templateCache.clear();

        // 1. Проверка на закрытую клетку
        RecognitionResult closedResult = compareWithTemplate(cellImage, "closed");
        if (closedResult != null && closedResult.getConfidence() > similarityThreshold) {
            return closedResult;
        }

        // 2. Проверка на флаг
        RecognitionResult flagResult = compareWithTemplate(cellImage, "flag");
        if (flagResult != null && flagResult.getConfidence() > similarityThreshold) {
            return flagResult;
        }

        // 3. Проверка на мину
        RecognitionResult mineResult = compareWithTemplate(cellImage, "mine");
        if (mineResult != null && mineResult.getConfidence() > similarityThreshold) {
            return mineResult;
        }

        // 4. Проверка на пустую клетку
        RecognitionResult emptyResult = compareWithTemplate(cellImage, "empty");
        if (emptyResult != null && emptyResult.getConfidence() > similarityThreshold) {
            return emptyResult;
        }

        // 5. Проверка на цифры (0-8)
        double bestConfidence = 0;
        CellState bestState = CellState.UNKNOWN;
        String bestMethod = "none";

        for (int i = 0; i <= 8; i++) {
            RecognitionResult digitResult = compareWithTemplate(cellImage, "digit_" + i);
            if (digitResult != null) {
                if (digitResult.getConfidence() > bestConfidence) {
                    bestConfidence = digitResult.getConfidence();
                    bestState = CellState.fromNumber(i);
                    bestMethod = digitResult.getMethod();
                }
            }
        }

        if (bestConfidence > similarityThreshold) {
            return new RecognitionResult(bestState, bestConfidence, bestMethod);
        }

        // 6. Если ничего не распознано, пробуем по цвету
        return recognizeByColor(cellImage);
    }

    /**
     * Сравнение с конкретным шаблоном
     */
    private RecognitionResult compareWithTemplate(BufferedImage image, String templateName) {
        BufferedImage template = templateLoader.getTemplate(templateName);
        if (template == null) {
            return null;
        }

        // Проверяем кэш
        String cacheKey = templateName + "_" + image.hashCode();
        if (templateCache.containsKey(cacheKey)) {
            double cachedConfidence = templateCache.get(cacheKey);
            CellState state = mapTemplateNameToState(templateName);
            return new RecognitionResult(state, cachedConfidence, "template(cached)");
        }

        double similarity = calculateSimilarity(image, template);
        templateCache.put(cacheKey, similarity);

        CellState state = mapTemplateNameToState(templateName);
        return new RecognitionResult(state, similarity, "template");
    }

    /**
     * Преобразование имени шаблона в состояние
     */
    private CellState mapTemplateNameToState(String templateName) {
        if (templateName.startsWith("digit_")) {
            int digit = Integer.parseInt(templateName.substring(6));
            return CellState.fromNumber(digit);
        }

        switch (templateName) {
            case "closed": return CellState.CLOSED;
            case "flag": return CellState.FLAG;
            case "mine": return CellState.MINE;
            case "empty": return CellState.EMPTY;
            default: return CellState.UNKNOWN;
        }
    }

    /**
     * Вычисление схожести двух изображений
     */
    private double calculateSimilarity(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            // Масштабируем, если размеры разные
            img2 = resizeImage(img2, img1.getWidth(), img1.getHeight());
        }

        int width = img1.getWidth();
        int height = img1.getHeight();

        double totalDifference = 0;
        int pixelsCompared = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c1 = new Color(img1.getRGB(x, y));
                Color c2 = new Color(img2.getRGB(x, y));

                double diff = Math.abs(c1.getRed() - c2.getRed()) / 255.0 +
                        Math.abs(c1.getGreen() - c2.getGreen()) / 255.0 +
                        Math.abs(c1.getBlue() - c2.getBlue()) / 255.0;

                totalDifference += diff / 3.0; // Нормализуем
                pixelsCompared++;
            }
        }

        double averageDifference = totalDifference / pixelsCompared;
        return 1.0 - averageDifference; // Чем меньше разница, тем выше схожесть
    }

    /**
     * Изменение размера изображения
     */
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, original.getType());
        java.awt.Graphics2D g = resized.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }

    /**
     * Распознавание по цвету (запасной вариант)
     */
    private RecognitionResult recognizeByColor(BufferedImage image) {
        // Анализируем средний цвет
        int width = image.getWidth();
        int height = image.getHeight();

        long totalR = 0, totalG = 0, totalB = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                totalR += color.getRed();
                totalG += color.getGreen();
                totalB += color.getBlue();
            }
        }

        int avgR = (int) (totalR / (width * height));
        int avgG = (int) (totalG / (width * height));
        int avgB = (int) (totalB / (width * height));

        // Типичные цвета для разных состояний
        if (isCloseToGray(avgR, avgG, avgB)) {
            if (avgR > 200) {
                return new RecognitionResult(CellState.EMPTY, 0.6, "color(bright)");
            } else if (avgR < 100) {
                return new RecognitionResult(CellState.CLOSED, 0.6, "color(dark)");
            }
        }

        return new RecognitionResult(CellState.UNKNOWN, 0.3, "color(fallback)");
    }

    /**
     * Проверка, является ли цвет серым
     */
    private boolean isCloseToGray(int r, int g, int b) {
        int maxDiff = Math.max(Math.abs(r - g), Math.max(Math.abs(g - b), Math.abs(r - b)));
        return maxDiff < 30;
    }

    /**
     * Установка порога схожести
     */
    public void setSimilarityThreshold(double threshold) {
        this.similarityThreshold = threshold;
    }

    /**
     * Очистка кэша
     */
    public void clearCache() {
        templateCache.clear();
    }
}