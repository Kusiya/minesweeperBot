package org.minesweeper.vision;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Интерфейс для различных методов распознавания клеток.
 */
public interface CellRecognizer {

    /**
     * Распознать состояние клетки по изображению
     * @param cellImage изображение клетки
     * @return распознанное состояние
     */
    CellState recognize(BufferedImage cellImage);

    /**
     * Распознать состояние клетки с указанием уверенности
     * @param cellImage изображение клетки
     * @return результат распознавания с уверенностью
     */
    RecognitionResult recognizeWithConfidence(BufferedImage cellImage);

    /**
     * Обучить распознаватель (для ML подходов)
     * @param images изображения для обучения
     * @param labels правильные метки
     */
    default void train(List<BufferedImage> images, List<CellState> labels) {
        // Базовая реализация ничего не делает
    }

    /**
     * Результат распознавания с уверенностью
     */
    class RecognitionResult {
        private final CellState state;
        private final double confidence;
        private final String method;

        public RecognitionResult(CellState state, double confidence, String method) {
            this.state = state;
            this.confidence = confidence;
            this.method = method;
        }

        public CellState getState() { return state; }
        public double getConfidence() { return confidence; }
        public String getMethod() { return method; }

        public boolean isReliable() {
            return confidence > 0.8;
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f%%) via %s", state, confidence * 100, method);
        }
    }
}