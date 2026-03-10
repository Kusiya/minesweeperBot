package org.minesweeper.vision;

import java.awt.image.BufferedImage;

public interface CellRecognizer {
    int recognizeCell(BufferedImage cellImage);

    // ИСПРАВЛЕНО: добавили параметры калибровки
    int[][] recognizeBoard(BufferedImage fullImage, int rows, int cols,
                           int offsetX, int offsetY, int cellSize);
}