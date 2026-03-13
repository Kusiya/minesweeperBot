package org.minesweeper.core;

/**
 * Представляет решение бота о следующем действии.
 */
public class Move {
    private final int row;                    // Целевая строка
    private final int col;                     // Целевой столбец
    private final boolean isFlag;               // true = поставить флаг, false = открыть
    private double confidence;                  // Уверенность в ходе (0.0 - 1.0)
    private String strategy;                     // Стратегия, принявшая решение
    private String reasoning;                     // Обоснование хода

    /**
     * Создание простого хода
     */
    public Move(int row, int col, boolean isFlag) {
        this(row, col, isFlag, 0.5, "Unknown", "");
    }

    /**
     * Создание хода с дополнительной информацией
     */
    public Move(int row, int col, boolean isFlag, double confidence, String strategy) {
        this(row, col, isFlag, confidence, strategy, "");
    }

    /**
     * Полный конструктор
     */
    public Move(int row, int col, boolean isFlag, double confidence,
                String strategy, String reasoning) {
        validateCoordinates(row, col);
        validateConfidence(confidence);

        this.row = row;
        this.col = col;
        this.isFlag = isFlag;
        this.confidence = confidence;
        this.strategy = strategy != null ? strategy : "Unknown";
        this.reasoning = reasoning != null ? reasoning : "";
    }

    /**
     * Проверка координат
     */
    private void validateCoordinates(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Координаты не могут быть отрицательными");
        }
    }

    /**
     * Проверка уверенности
     */
    private void validateConfidence(double confidence) {
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Уверенность должна быть между 0 и 1");
        }
    }

    // Геттеры
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isFlag() { return isFlag; }
    public double getConfidence() { return confidence; }
    public String getStrategy() { return strategy; }
    public String getReasoning() { return reasoning; }

    /**
     * Установка уверенности (для случаев, когда стратегия вычисляет её позже)
     */
    public void setConfidence(double confidence) {
        validateConfidence(confidence);
        this.confidence = confidence;
    }

    /**
     * Установка стратегии
     */
    public void setStrategy(String strategy) {
        this.strategy = strategy != null ? strategy : "Unknown";
    }

    /**
     * Проверка, является ли ход безопасным (открытие клетки)
     */
    public boolean isSafeMove() {
        return !isFlag;
    }

    /**
     * Проверка, является ли ход высоконадежным
     */
    public boolean isHighConfidence() {
        return confidence > 0.8;
    }

    /**
     * Проверка, является ли ход рискованным
     */
    public boolean isRisky() {
        return confidence < 0.3;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return row == move.row && col == move.col && isFlag == move.isFlag;
    }

    @Override
    public int hashCode() {
        return 31 * row + 31 * col + (isFlag ? 1 : 0);
    }

    @Override
    public String toString() {
        String action = isFlag ? "Флаг" : "Открыть";
        String confidenceStr = String.format("%.2f", confidence);
        return String.format("%s (%d,%d) [%s, %s]", action, row, col, confidenceStr, strategy);
    }

    /**
     * Подробное описание для отладки
     */
    public String toDetailedString() {
        return String.format("Move{action=%s, pos=(%d,%d), confidence=%.2f, strategy='%s', reasoning='%s'}",
                isFlag ? "FLAG" : "REVEAL", row, col, confidence, strategy, reasoning);
    }
}