package org.minesweeper.core;

/**
 * Представляет один ход бота
 */
public class Move {
    private final int row;
    private final int col;
    private final boolean isFlag; // true = поставить флаг, false = открыть
    private final double risk;     // риск от 0 до 1 (для вероятностных ходов)
    private final String reason;   // причина выбора этого хода

    public Move(int row, int col, boolean isFlag) {
        this(row, col, isFlag, 0.0, "Детерминированный ход");
    }

    public Move(int row, int col, boolean isFlag, double risk, String reason) {
        this.row = row;
        this.col = col;
        this.isFlag = isFlag;
        this.risk = risk;
        this.reason = reason;
    }

    // Геттеры
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isFlag() { return isFlag; }
    public double getRisk() { return risk; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return String.format("Move{row=%d, col=%d, %s, risk=%.2f, reason='%s'}",
                row, col, isFlag ? "FLAG" : "OPEN", risk, reason);
    }
}
