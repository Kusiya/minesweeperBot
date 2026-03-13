package org.minesweeper.execution;

import org.minesweeper.core.Move;
import org.minesweeper.utils.Logger;

import java.awt.*;
import java.awt.event.InputEvent;

public class MouseController {
    private Robot robot;
    private int offsetX;
    private int offsetY;
    private int cellSize;
    private boolean debugMode = true;

    public MouseController() throws AWTException {
        this.robot = new Robot();
        this.offsetX = 902;  // из вашей калибровки
        this.offsetY = 307;  // из вашей калибровки
        this.cellSize = 23;  // из вашей калибровки

        Logger.info("🖱️ MouseController инициализирован");
    }

    public void executeMove(Move move) {
        if (move == null) return;

        try {
            int screenX = offsetX + move.getCol() * cellSize + cellSize / 2;
            int screenY = offsetY + move.getRow() * cellSize + cellSize / 2;

            Logger.info("🎯 Цель: клетка [" + move.getRow() + "," + move.getCol() + "] -> (" + screenX + ", " + screenY + ")");
            Logger.info("   Тип хода: " + (move.isFlag() ? "ФЛАГ" : "ОТКРЫТЬ"));

            // Перемещаем мышь
            moveMouse(screenX, screenY);
            robot.delay(200);

            // Выполняем клик в зависимости от типа хода
            if (move.isFlag()) {
                Logger.info("   🚩 Выполняю ПРАВЫЙ клик (флаг)");
                rightClick();
            } else {
                Logger.info("   🔴 Выполняю ЛЕВЫЙ клик (открыть)");
                leftClick();
            }

            robot.delay(200);

        } catch (Exception e) {
            Logger.error("Ошибка при выполнении хода: " + e.getMessage());
        }
    }

    private void moveMouse(int x, int y) {
        try {
            robot.mouseMove(x, y);
            robot.delay(100);
        } catch (Exception e) {
            Logger.error("Ошибка перемещения мыши: " + e.getMessage());
        }
    }

    private void leftClick() {
        try {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(50);
            Logger.debug("   Левый клик выполнен");
        } catch (Exception e) {
            Logger.error("Ошибка левого клика: " + e.getMessage());
        }
    }

    private void rightClick() {
        try {
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.delay(50);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            robot.delay(50);
            Logger.debug("   Правый клик выполнен");
        } catch (Exception e) {
            Logger.error("Ошибка правого клика: " + e.getMessage());
        }
    }

    public static Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
        Logger.info("MouseController offset: (" + x + ", " + y + ")");
    }

    public void setCellSize(int size) {
        this.cellSize = size;
        Logger.info("MouseController cellSize: " + size);
    }
}