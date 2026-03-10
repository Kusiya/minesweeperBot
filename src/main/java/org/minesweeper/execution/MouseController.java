package org.minesweeper.execution;

import org.minesweeper.core.Move;
import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Управляет мышью для выполнения кликов
 */
public class MouseController {
    private Robot robot;
    private int offsetX;      // координата левого верхнего угла первой клетки
    private int offsetY;
    private int cellSize;     // размер клетки в пикселях

    public MouseController() throws AWTException {
        this.robot = new Robot();
        this.offsetX = 100;
        this.offsetY = 100;
        this.cellSize = 30;
    }

    /**
     * Выполнить ход
     */
    public void executeMove(Move move) {
        int screenX = offsetX + move.getCol() * cellSize + cellSize / 2;
        int screenY = offsetY + move.getRow() * cellSize + cellSize / 2;

        moveMouse(screenX, screenY);

        if (move.isFlag()) {
            rightClick();
        } else {
            leftClick();
        }

        // Небольшая задержка после клика
        robot.delay(50);
    }

    /**
     * Переместить курсор
     */
    private void moveMouse(int x, int y) {
        robot.mouseMove(x, y);
        robot.delay(20);
    }

    /**
     * Левый клик (открыть клетку)
     */
    private void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Правый клик (поставить флаг)
     */
    private void rightClick() {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    /**
     * Двойной клик (для открытия области)
     */
    public void doubleClick() {
        leftClick();
        robot.delay(50);
        leftClick();
    }

    // Методы для калибровки
    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public void setCellSize(int size) {
        this.cellSize = size;
    }

    /**
     * Получить текущие координаты мыши (для калибровки)
     */
    public Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }
}
