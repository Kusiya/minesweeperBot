package org.minesweeper.core;

import java.util.Objects;

/**
 * Модель клетки игрового поля.
 * Содержит всю информацию о конкретной клетке.
 */
public class Cell {
    private final int row;                 // Координата по вертикали
    private final int col;                  // Координата по горизонтали
    private boolean isMine;                  // Является ли клетка миной
    private boolean isRevealed;              // Открыта ли клетка
    private boolean isFlagged;               // Поставлен ли флаг
    private int adjacentMines;               // Количество мин вокруг (0-8)

    /**
     * Создание новой клетки с заданными координатами
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    /**
     * Конструктор копирования
     */
    public Cell(Cell other) {
        this.row = other.row;
        this.col = other.col;
        this.isMine = other.isMine;
        this.isRevealed = other.isRevealed;
        this.isFlagged = other.isFlagged;
        this.adjacentMines = other.adjacentMines;
    }

    // Геттеры и сеттеры
    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }

    public boolean isRevealed() { return isRevealed; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int mines) {
        if (mines < 0 || mines > 8) {
            throw new IllegalArgumentException("Количество мин должно быть от 0 до 8");
        }
        this.adjacentMines = mines;
    }

    /**
     * Проверка, является ли клетка безопасной для открытия
     */
    public boolean isSafeToReveal() {
        return !isMine && !isRevealed && !isFlagged;
    }

    /**
     * Сброс состояния клетки (для новой игры)
     */
    public void reset() {
        isMine = false;
        isRevealed = false;
        isFlagged = false;
        adjacentMines = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cell cell = (Cell) obj;
        return row == cell.row && col == cell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Строковое представление для отладки
     */
    @Override
    public String toString() {
        if (isFlagged) return "F";
        if (!isRevealed) return "?";
        if (isMine) return "*";
        return adjacentMines == 0 ? "." : String.valueOf(adjacentMines);
    }

    /**
     * Полное описание клетки для логирования
     */
    public String toDetailedString() {
        return String.format("Cell[%d,%d] mine=%b revealed=%b flagged=%b adjacent=%d",
                row, col, isMine, isRevealed, isFlagged, adjacentMines);
    }
}