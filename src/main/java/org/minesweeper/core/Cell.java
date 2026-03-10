package org.minesweeper.core;

/**
 * Класс, представляющий одну клетку игрового поля
 */
public class Cell {
    private final int row;
    private final int col;
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines; // число мин вокруг (0-8)

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    // Геттеры
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isMine() { return isMine; }
    public boolean isRevealed() { return isRevealed; }
    public boolean isFlagged() { return isFlagged; }
    public int getAdjacentMines() { return adjacentMines; }

    // Сеттеры
    public void setMine(boolean mine) { isMine = mine; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }
    public void setAdjacentMines(int count) { adjacentMines = count; }

    /**
     * @return true если клетку можно открыть (не открыта и не отмечена флагом)
     */
    public boolean isOpenable() {
        return !isRevealed && !isFlagged;
    }

    @Override
    public String toString() {
        if (isFlagged) return "F";
        if (!isRevealed) return "?";
        if (isMine) return "*";
        if (adjacentMines == 0) return " ";
        return String.valueOf(adjacentMines);
    }
}
