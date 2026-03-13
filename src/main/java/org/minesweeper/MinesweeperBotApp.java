package org.minesweeper;

import org.minesweeper.bot.AdvancedMinesweeperBot;
import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;
import org.minesweeper.execution.MouseController;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.strategy.AdvancedStrategy;
import org.minesweeper.utils.Config;
import org.minesweeper.utils.Logger;
import org.minesweeper.utils.BotStatistics;
import org.minesweeper.vision.CellState;
import org.minesweeper.vision.ScreenCapture;
import org.minesweeper.vision.TemplateCellRecognizer;
import org.minesweeper.vision.TemplateLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class MinesweeperBotApp {
    private ScreenCapture screenCapture;
    private TemplateCellRecognizer cellRecognizer;
    private MinesweeperBot bot;
    private MouseController mouseController;
    private Logger logger;
    private Config config;
    private BotStatistics statistics;
    private boolean running;

    // Параметры доски
    private Rectangle boardArea;
    private int cellSize;
    private int rows;
    private int cols;
    private int totalMines;
    private Point boardOffset;

    // Состояние игры
    private Cell[][] lastBoardState;
    private int moveCount;
    private boolean firstMoveDone = false;
    private Set<String> attemptedMoves = new HashSet<>();

    // Результаты калибровки
    private CalibrationResult calibrationResult;

    public MinesweeperBotApp() {
        this.config = Config.getInstance();
        this.logger = Logger.getInstance();
        this.statistics = new BotStatistics();
        this.running = true;
        this.moveCount = 0;
        this.attemptedMoves = new HashSet<>();

        logger.info("MinesweeperBotApp инициализируется...");
    }

    /**
     * Инициализация всех компонентов
     */
    public void initialize() throws Exception {
        logger.info("Инициализация компонентов...");

        screenCapture = new ScreenCapture();

        // Запускаем калибровку
        calibrate();

        TemplateLoader templateLoader = new TemplateLoader();
        templateLoader.loadTemplates();
        cellRecognizer = new TemplateCellRecognizer(templateLoader);

        initializeBot();

        mouseController = new MouseController(cellSize, boardOffset,
                config.getClickDelay(), config.isDebugMode());

        logger.info("Инициализация завершена успешно");
    }

    /**
     * Полная калибровка игрового поля
     */
    private void calibrate() throws Exception {
        logger.info("Начало калибровки...");

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║         КАЛИБРОВКА ПОЛЯ              ║");
        System.out.println("╚══════════════════════════════════════╝");

        Scanner scanner = new Scanner(System.in);

        System.out.println("\nВыберите метод калибровки:");
        System.out.println("1. Автоматическая калибровка");
        System.out.println("2. Полуавтоматическая (с выбором углов)");
        System.out.println("3. Ручная калибровка (ввод параметров)");
        System.out.println("4. Загрузить сохраненную калибровку");
        System.out.print("Ваш выбор: ");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                autoCalibrate();
                break;
            case 2:
                semiAutoCalibrate();
                break;
            case 3:
                manualCalibrate();
                break;
            case 4:
                loadCalibration();
                break;
            default:
                autoCalibrate();
        }

        // Сохраняем результаты калибровки
        saveCalibration();

        logger.info(String.format("Калибровка завершена: %dx%d, клетка %dpx, смещение %s",
                rows, cols, cellSize, boardOffset));
    }

    /**
     * Автоматическая калибровка
     */
    private void autoCalibrate() throws Exception {
        logger.info("Автоматическая калибровка...");

        // Поиск области доски
        boardArea = screenCapture.findBoardArea();

        if (boardArea == null) {
            logger.warning("Автоматическая калибровка не удалась, переходим к полуавтоматической");
            semiAutoCalibrate();
            return;
        }

        // Анализируем доску для определения размера клеток
        analyzeBoardStructure();

        // Определяем количество мин
        determineMineCount();
    }

    /**
     * Полуавтоматическая калибровка (с выбором углов)
     */
    private void semiAutoCalibrate() throws Exception {
        logger.info("Полуавтоматическая калибровка...");

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n1. Наведите мышь на ЛЕВЫЙ ВЕРХНИЙ угол доски");
        System.out.println("   (самая верхняя левая клетка)");
        System.out.print("Нажмите Enter для захвата позиции...");
        scanner.nextLine();
        scanner.nextLine(); // Дополнительный Enter

        Point topLeft = getMousePosition();
        System.out.println("   Захвачена позиция: " + topLeft);

        System.out.println("\n2. Наведите мышь на ПРАВЫЙ НИЖНИЙ угол доски");
        System.out.println("   (самая нижняя правая клетка)");
        System.out.print("Нажмите Enter для захвата позиции...");
        scanner.nextLine();

        Point bottomRight = getMousePosition();
        System.out.println("   Захвачена позиция: " + bottomRight);

        boardArea = new Rectangle(
                topLeft.x, topLeft.y,
                bottomRight.x - topLeft.x,
                bottomRight.y - topLeft.y
        );

        System.out.print("\nВведите количество СТРОК на доске: ");
        rows = scanner.nextInt();

        System.out.print("Введите количество СТОЛБЦОВ на доске: ");
        cols = scanner.nextInt();

        // Вычисляем размер клетки
        int cellWidth = boardArea.width / cols;
        int cellHeight = boardArea.height / rows;
        cellSize = Math.min(cellWidth, cellHeight);

        // Корректируем область доски для точного соответствия
        boardArea.width = cols * cellSize;
        boardArea.height = rows * cellSize;

        boardOffset = new Point(boardArea.x, boardArea.y);

        System.out.println("\nРезультаты калибровки:");
        System.out.println("  Область: " + boardArea);
        System.out.println("  Размер клетки: " + cellSize + "px");
        System.out.println("  Сетка: " + rows + "x" + cols);

        determineMineCount();
    }

    /**
     * Ручная калибровка
     */
    private void manualCalibrate() {
        logger.info("Ручная калибровка...");

        Scanner scanner = new Scanner(System.in);

        System.out.print("\nВведите X координату левого верхнего угла: ");
        int x = scanner.nextInt();

        System.out.print("Введите Y координату левого верхнего угла: ");
        int y = scanner.nextInt();

        System.out.print("Введите ширину области доски (px): ");
        int width = scanner.nextInt();

        System.out.print("Введите высоту области доски (px): ");
        int height = scanner.nextInt();

        System.out.print("Введите количество строк: ");
        rows = scanner.nextInt();

        System.out.print("Введите количество столбцов: ");
        cols = scanner.nextInt();

        boardArea = new Rectangle(x, y, width, height);

        // Вычисляем размер клетки
        int cellWidth = width / cols;
        int cellHeight = height / rows;
        cellSize = Math.min(cellWidth, cellHeight);

        boardOffset = new Point(boardArea.x, boardArea.y);

        determineMineCount();
    }

    /**
     * Анализ структуры доски для определения размера клеток
     */
    private void analyzeBoardStructure() throws Exception {
        logger.info("Анализ структуры доски...");

        BufferedImage boardImage = screenCapture.captureBoardArea(boardArea);

        // Сохраняем скриншот для анализа
        screenCapture.saveScreenshot(boardImage, "board_debug.png");

        // Анализируем вертикальные линии
        List<Integer> verticalLines = detectVerticalLines(boardImage);
        // Анализируем горизонтальные линии
        List<Integer> horizontalLines = detectHorizontalLines(boardImage);

        if (verticalLines.size() > 1 && horizontalLines.size() > 1) {
            cols = verticalLines.size() - 1;
            rows = horizontalLines.size() - 1;

            // Вычисляем размер клетки
            int cellWidth = (verticalLines.get(1) - verticalLines.get(0));
            int cellHeight = (horizontalLines.get(1) - horizontalLines.get(0));
            cellSize = Math.min(cellWidth, cellHeight);

            logger.info("Определена сетка: " + rows + "x" + cols + ", клетка " + cellSize + "px");
        } else {
            // Если не удалось определить, используем значения по умолчанию
            logger.warning("Не удалось определить сетку, используем значения по умолчанию");
            rows = 16;
            cols = 30;
            cellSize = 24;
        }

        boardOffset = new Point(boardArea.x, boardArea.y);
    }

    /**
     * Детектирование вертикальных линий на изображении
     */
    private List<Integer> detectVerticalLines(BufferedImage image) {
        List<Integer> lines = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            int colorChanges = 0;

            for (int y = 0; y < height - 1; y++) {
                int rgb1 = image.getRGB(x, y);
                int rgb2 = image.getRGB(x, y + 1);

                if (colorDifference(rgb1, rgb2) > 50) {
                    colorChanges++;
                }
            }

            if (colorChanges > height / 10) {
                lines.add(x);
            }
        }

        return filterLines(lines);
    }

    /**
     * Детектирование горизонтальных линий на изображении
     */
    private List<Integer> detectHorizontalLines(BufferedImage image) {
        List<Integer> lines = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            int colorChanges = 0;

            for (int x = 0; x < width - 1; x++) {
                int rgb1 = image.getRGB(x, y);
                int rgb2 = image.getRGB(x + 1, y);

                if (colorDifference(rgb1, rgb2) > 50) {
                    colorChanges++;
                }
            }

            if (colorChanges > width / 10) {
                lines.add(y);
            }
        }

        return filterLines(lines);
    }

    /**
     * Вычисление разницы между двумя цветами
     */
    private double colorDifference(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }

    /**
     * Фильтрация и группировка линий
     */
    private List<Integer> filterLines(List<Integer> lines) {
        if (lines.isEmpty()) return lines;

        List<Integer> filtered = new ArrayList<>();
        Collections.sort(lines);

        int lastLine = lines.get(0);
        filtered.add(lastLine);

        for (int i = 1; i < lines.size(); i++) {
            int currentLine = lines.get(i);
            if (currentLine - lastLine > 10) {
                filtered.add(currentLine);
                lastLine = currentLine;
            }
        }

        return filtered;
    }

    /**
     * Определение количества мин
     */
    private void determineMineCount() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nВыберите способ определения количества мин:");
        System.out.println("1. Ввести вручную");
        System.out.println("2. Определить по счетчику на экране");
        System.out.println("3. Использовать стандартное значение");
        System.out.print("Ваш выбор: ");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                System.out.print("Введите количество мин на доске: ");
                totalMines = scanner.nextInt();
                break;

            case 2:
                detectMineCountFromScreen();
                break;

            default:
                if (rows <= 9 && cols <= 9) {
                    totalMines = 10;
                } else if (rows <= 16 && cols <= 16) {
                    totalMines = 40;
                } else {
                    totalMines = 99;
                }
                System.out.println("Установлено стандартное значение: " + totalMines + " мин");
        }
    }

    /**
     * Детектирование количества мин по счетчику на экране
     */
    private void detectMineCountFromScreen() {
        logger.info("Попытка определить количество мин по счетчику...");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите количество мин (по счетчику на экране): ");
        totalMines = scanner.nextInt();
    }

    /**
     * Сохранение результатов калибровки
     */
    private void saveCalibration() {
        calibrationResult = new CalibrationResult(
                boardArea, cellSize, rows, cols, totalMines, boardOffset
        );

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("calibration.dat"))) {
            oos.writeObject(calibrationResult);
            logger.info("Калибровка сохранена в файл: calibration.dat");
        } catch (IOException e) {
            logger.warning("Не удалось сохранить калибровку: " + e.getMessage());
        }
    }

    /**
     * Загрузка сохраненной калибровки
     */
    private void loadCalibration() {
        File calFile = new File("calibration.dat");
        if (calFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(calFile))) {
                calibrationResult = (CalibrationResult) ois.readObject();

                boardArea = calibrationResult.boardArea;
                cellSize = calibrationResult.cellSize;
                rows = calibrationResult.rows;
                cols = calibrationResult.cols;
                totalMines = calibrationResult.totalMines;
                boardOffset = calibrationResult.boardOffset;

                logger.info("Калибровка загружена из файла");
                System.out.println("\nЗагружена сохраненная калибровка:");
                System.out.println(calibrationResult);
            } catch (Exception e) {
                logger.error("Ошибка загрузки калибровки", e);
                try {
                    autoCalibrate();
                } catch (Exception ex) {
                    logger.error("Не удалось выполнить автоматическую калибровку", ex);
                }
            }
        } else {
            logger.warning("Файл калибровки не найден, запускаем новую калибровку");
            try {
                autoCalibrate();
            } catch (Exception e) {
                logger.error("Не удалось выполнить автоматическую калибровку", e);
            }
        }
    }

    /**
     * Визуальная проверка калибровки
     */
    public void verifyCalibration() throws Exception {
        logger.info("Проверка калибровки...");

        BufferedImage boardImage = screenCapture.captureBoardArea(boardArea);

        Graphics2D g = boardImage.createGraphics();
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2));

        for (int col = 0; col <= cols; col++) {
            int x = col * cellSize;
            g.drawLine(x, 0, x, boardImage.getHeight());
        }

        for (int row = 0; row <= rows; row++) {
            int y = row * cellSize;
            g.drawLine(0, y, boardImage.getWidth(), y);
        }

        g.dispose();

        screenCapture.saveScreenshot(boardImage, "calibration_verify.png");

        System.out.println("\nПроверка калибровки:");
        System.out.println("  - Сетка нарисована на изображении");
        System.out.println("  - Файл сохранен: screenshots/calibration_verify.png");
        System.out.println("\nПроверьте, совпадает ли сетка с клетками на доске.");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Калибровка верна? (1-Да, 2-Нет, повторить): ");

        int choice = scanner.nextInt();
        if (choice != 1) {
            logger.info("Повторная калибровка...");
            calibrate();
        }
    }

    /**
     * Класс для хранения результатов калибровки
     */
    public static class CalibrationResult implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Rectangle boardArea;
        public final int cellSize;
        public final int rows;
        public final int cols;
        public final int totalMines;
        public final Point boardOffset;

        public CalibrationResult(Rectangle boardArea, int cellSize,
                                 int rows, int cols, int totalMines, Point boardOffset) {
            this.boardArea = boardArea;
            this.cellSize = cellSize;
            this.rows = rows;
            this.cols = cols;
            this.totalMines = totalMines;
            this.boardOffset = boardOffset;
        }

        @Override
        public String toString() {
            return String.format(
                    "CalibrationResult{area=%dx%d at (%d,%d), cell=%dpx, grid=%dx%d, mines=%d}",
                    boardArea.width, boardArea.height, boardArea.x, boardArea.y,
                    cellSize, rows, cols, totalMines
            );
        }
    }

    private void initializeBot() {
        if (config.useAdvancedBot()) {
            AdvancedMinesweeperBot advancedBot = new AdvancedMinesweeperBot(rows, cols, totalMines);

            advancedBot.addStrategy(new BasicStrategy());
            advancedBot.addStrategy(new AdvancedStrategy());

            bot = advancedBot;
            logger.info("Создан продвинутый бот с " +
                    advancedBot.getStrategies().size() + " стратегиями");
        } else {
            bot = new MinesweeperBot(rows, cols, totalMines);
            logger.info("Создан обычный бот");
        }
    }

    /**
     * Захват текущего состояния доски с экрана
     */
    private boolean captureBoardState() {
        logger.debug("Захват состояния доски...");

        try {
            BufferedImage boardImage = screenCapture.captureBoardArea(boardArea);

            if (config.isDebugMode()) {
                screenCapture.saveScreenshot(boardImage, "board_state_" + moveCount + ".png");
            }

            boolean stateChanged = false;
            int revealedCount = 0;
            int flaggedCount = 0;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    BufferedImage cellImage = extractCellImage(boardImage, row, col);
                    CellState state = cellRecognizer.recognize(cellImage);

                    if (updateCellState(row, col, state)) {
                        stateChanged = true;
                    }

                    // Подсчитываем открытые клетки и флаги
                    if (isCellRevealed(row, col)) revealedCount++;
                    if (isCellFlagged(row, col)) flaggedCount++;
                }
            }

            logger.debug(String.format("Состояние: открыто %d/%d, флагов %d/%d",
                    revealedCount, rows * cols, flaggedCount, totalMines));

            return stateChanged;

        } catch (Exception e) {
            logger.error("Ошибка при захвате состояния доски", e);
            return false;
        }
    }

    /**
     * Проверка, открыта ли клетка
     */
    private boolean isCellRevealed(int row, int col) {
        try {
            java.lang.reflect.Field boardField = MinesweeperBot.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Cell[][] board = (Cell[][]) boardField.get(bot);
            return board[row][col].isRevealed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверка, есть ли флаг на клетке
     */
    private boolean isCellFlagged(int row, int col) {
        try {
            java.lang.reflect.Field boardField = MinesweeperBot.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Cell[][] board = (Cell[][]) boardField.get(bot);
            return board[row][col].isFlagged();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверка, можно ли открыть клетку
     */
    private boolean isCellOpenable(int row, int col) {
        try {
            java.lang.reflect.Field boardField = MinesweeperBot.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Cell[][] board = (Cell[][]) boardField.get(bot);
            return !board[row][col].isRevealed() && !board[row][col].isFlagged();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Извлечение изображения клетки
     */
    private BufferedImage extractCellImage(BufferedImage boardImage, int row, int col) {
        int x = col * cellSize;
        int y = row * cellSize;

        x = Math.min(x, boardImage.getWidth() - cellSize);
        y = Math.min(y, boardImage.getHeight() - cellSize);

        return boardImage.getSubimage(x, y, cellSize, cellSize);
    }

    /**
     * Обновление состояния клетки в боте
     */
    private boolean updateCellState(int row, int col, CellState state) {
        try {
            java.lang.reflect.Field boardField = MinesweeperBot.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Cell[][] board = (Cell[][]) boardField.get(bot);

            Cell cell = board[row][col];
            CellState oldState = getCellState(cell);

            if (oldState == state) {
                return false; // Состояние не изменилось
            }

            switch (state) {
                case CLOSED:
                    // Ничего не делаем
                    break;

                case EMPTY:
                    cell.setRevealed(true);
                    cell.setFlagged(false);
                    cell.setAdjacentMines(0);
                    logger.debug(String.format("Клетка [%d,%d] открыта (пусто)", row, col));
                    break;

                case FLAG:
                    cell.setFlagged(true);
                    cell.setRevealed(false);
                    logger.debug(String.format("Клетка [%d,%d] помечена флагом", row, col));
                    break;

                case MINE:
                    cell.setMine(true);
                    cell.setRevealed(true);
                    logger.debug(String.format("Клетка [%d,%d] - МИНА!", row, col));
                    break;

                case NUMBER_0: case NUMBER_1: case NUMBER_2: case NUMBER_3:
                case NUMBER_4: case NUMBER_5: case NUMBER_6: case NUMBER_7: case NUMBER_8:
                    cell.setRevealed(true);
                    cell.setFlagged(false);
                    cell.setAdjacentMines(state.getNumber());
                    logger.debug(String.format("Клетка [%d,%d] открыта: %d",
                            row, col, state.getNumber()));
                    break;

                default:
                    return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Не удалось обновить состояние клетки", e);
            return false;
        }
    }

    /**
     * Получение текущего состояния клетки
     */
    private CellState getCellState(Cell cell) {
        if (cell.isFlagged()) return CellState.FLAG;
        if (!cell.isRevealed()) return CellState.CLOSED;
        if (cell.isMine()) return CellState.MINE;

        int num = cell.getAdjacentMines();
        if (num == 0) return CellState.EMPTY;
        return CellState.fromNumber(num);
    }

    /**
     * Ожидание изменения состояния доски
     */
    private boolean waitForBoardChange(int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000;
        int checkCount = 0;

        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                Thread.sleep(300); // Проверяем чаще - каждые 300 мс
                checkCount++;

                if (captureBoardState()) {
                    logger.debug("Доска изменилась после " + (checkCount * 300) + "мс");
                    return true; // Состояние изменилось
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        logger.debug("Таймаут ожидания изменений на доске");
        return false; // Таймаут
    }

    /**
     * Проверка, был ли уже такой ход
     */
    private boolean isMoveAttempted(Move move) {
        String key = move.getRow() + "," + move.getCol() + "," + move.isFlag();
        return attemptedMoves.contains(key);
    }

    /**
     * Запоминаем попытку хода
     */
    private void rememberMove(Move move) {
        String key = move.getRow() + "," + move.getCol() + "," + move.isFlag();
        attemptedMoves.add(key);
    }

    /**
     * Поиск безопасного хода (когда стратегии не работают)
     */
    private Move findSafeMove() {
        List<int[]> possibleMoves = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (isCellOpenable(i, j)) {
                    // Проверяем, не пытались ли уже открыть эту клетку
                    String key = i + "," + j + ",false";
                    if (!attemptedMoves.contains(key)) {
                        possibleMoves.add(new int[]{i, j});
                    }
                }
            }
        }

        if (possibleMoves.isEmpty()) {
            return null;
        }

        // Предпочитаем клетки рядом с открытыми
        List<int[]> prioritizedMoves = new ArrayList<>();
        for (int[] move : possibleMoves) {
            if (hasRevealedNeighbor(move[0], move[1])) {
                prioritizedMoves.add(move);
            }
        }

        List<int[]> movesToUse = prioritizedMoves.isEmpty() ? possibleMoves : prioritizedMoves;
        int[] move = movesToUse.get(new Random().nextInt(movesToUse.size()));

        logger.warning("Безопасный ход: (" + move[0] + ", " + move[1] + ")");
        return new Move(move[0], move[1], false);
    }

    /**
     * Проверка, есть ли открытые соседи у клетки
     */
    private boolean hasRevealedNeighbor(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int nr = row + i;
                int nc = col + j;

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    try {
                        java.lang.reflect.Field boardField = MinesweeperBot.class.getDeclaredField("board");
                        boardField.setAccessible(true);
                        Cell[][] board = (Cell[][]) boardField.get(bot);

                        if (board[nr][nc].isRevealed()) {
                            return true;
                        }
                    } catch (Exception e) {
                        // Игнорируем
                    }
                }
            }
        }
        return false;
    }

    /**
     * Сброс набора попыток
     */
    private void resetAttemptedMoves() {
        attemptedMoves.clear();
        logger.debug("Сброс набора попыток");
    }

    /**
     * Получение количества открытых клеток
     */
    private int getRevealedCount() {
        int count = 0;
        try {
            java.lang.reflect.Field boardField = MinesweeperBot.class.getDeclaredField("board");
            boardField.setAccessible(true);
            Cell[][] board = (Cell[][]) boardField.get(bot);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j].isRevealed()) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при подсчете открытых клеток", e);
        }
        return count;
    }

    /**
     * Основной игровой цикл
     */
    public void run() {
        logger.info("Запуск игрового цикла...");

        try {
            verifyCalibration();
        } catch (Exception e) {
            logger.error("Ошибка проверки калибровки", e);
        }

        // Первый ход
        makeFirstMove();
        moveCount = 1;

        int noProgressCount = 0;
        int lastRevealedCount = getRevealedCount();

        // Основной цикл игры
        while (running && bot.isGameActive() && !bot.isGameWon()) {
            try {
                // Ждем изменения состояния доски после предыдущего хода
                if (!waitForBoardChange(3)) { // Уменьшил таймаут до 3 секунд
                    noProgressCount++;
                    logger.warning("Нет прогресса. Попытка " + noProgressCount);

                    // Проверяем, не закончилась ли игра
                    if (checkGameEnd()) {
                        break;
                    }

                    // Если нет прогресса 3 раза подряд, пробуем безопасный ход
                    if (noProgressCount >= 3) {
                        logger.warning("Нет прогресса 3 раза, пробуем безопасный ход");
                        Move safeMove = findSafeMove();

                        if (safeMove != null) {
                            moveCount++;
                            logger.info("Безопасный ход " + moveCount + ": " + safeMove);
                            executeMove(safeMove);
                            rememberMove(safeMove);
                            Thread.sleep(1000);
                            captureBoardState();
                            noProgressCount = 0;
                            continue;
                        } else {
                            logger.error("Нет доступных безопасных ходов!");
                            break;
                        }
                    }

                    continue;
                }

                // Сбрасываем счетчик прогресса
                noProgressCount = 0;

                // Проверяем, увеличилось ли количество открытых клеток
                int currentRevealed = getRevealedCount();
                if (currentRevealed > lastRevealedCount) {
                    logger.debug("Прогресс: открыто клеток " + currentRevealed);
                    lastRevealedCount = currentRevealed;
                    resetAttemptedMoves(); // Сбрасываем попытки при прогрессе
                }

                // Принимаем решение
                Move move = bot.makeMove();

                // Проверяем, не пытаемся ли мы сделать тот же ход
                if (move != null && isMoveAttempted(move)) {
                    logger.warning("Повторная попытка хода " + move + ", ищем альтернативу");
                    move = findSafeMove();
                }

                if (move == null) {
                    logger.warning("Бот не может сделать ход, ищем безопасный ход");
                    move = findSafeMove();

                    if (move == null) {
                        logger.error("Нет доступных ходов!");
                        break;
                    }
                }

                moveCount++;
                logger.info("Ход " + moveCount + ": " + move);

                // Запоминаем попытку
                rememberMove(move);

                // Выполняем ход
                executeMove(move);

                // Даем время на обновление
                Thread.sleep(800);

                // Проверяем результат хода
                captureBoardState();

            } catch (Exception e) {
                logger.error("Ошибка в игровом цикле", e);

                // Пытаемся восстановиться
                try {
                    Thread.sleep(2000);
                    captureBoardState();
                } catch (Exception ex) {
                    break;
                }
            }
        }

        // Завершение игры
        finishGame();
    }

    /**
     * Первый ход
     */
    private void makeFirstMove() {
        logger.info("Первый ход...");

        // Захватываем начальное состояние
        captureBoardState();

        // Получаем решение бота
        Move firstMove = bot.makeMove();

        // Выполняем ход
        logger.info("Первый ход: " + firstMove);
        executeMove(firstMove);
        rememberMove(firstMove);

        // Ждем обновления доски
        try {
            Thread.sleep(1500);
            captureBoardState();
        } catch (Exception e) {
            logger.error("Ошибка после первого хода", e);
        }

        firstMoveDone = true;
    }

    /**
     * Выполнение хода
     */
    private void executeMove(Move move) {
        logger.debug("Выполнение хода: " + move);

        if (move.isFlag()) {
            mouseController.rightClickCell(move.getRow(), move.getCol());
        } else {
            mouseController.clickCell(move.getRow(), move.getCol());
        }

        // Даем время игре отреагировать
        try {
            Thread.sleep(config.getClickDelay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Проверка окончания игры
     */
    private boolean checkGameEnd() {
        try {
            // Проверяем по внутреннему состоянию бота
            if (!bot.isGameActive() || bot.isGameWon()) {
                return true;
            }

            // Дополнительно проверяем по скриншоту (можно добавить распознавание)
            // BufferedImage boardImage = screenCapture.captureBoardArea(boardArea);
            // TODO: распознавание сообщения о победе/поражении

            return false;

        } catch (Exception e) {
            logger.error("Ошибка при проверке окончания игры", e);
            return false;
        }
    }

    /**
     * Завершение игры
     */
    private void finishGame() {
        if (bot.isGameWon()) {
            logger.info("ПОБЕДА! Бот выиграл!");
            System.out.println("\n🎉 БОТ ВЫИГРАЛ! 🎉");
            statistics.recordGame(true, moveCount, 0, "advanced");
        } else if (!bot.isGameActive()) {
            logger.info("Поражение...");
            System.out.println("\n💥 Бот проиграл 💥");
            statistics.recordGame(false, moveCount, 0, "advanced");
        } else {
            logger.info("Игра прервана");
        }

        statistics.printStatistics();
    }

    private Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public void stop() {
        running = false;
        logger.info("Приложение остановлено");
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     MINESWEEPER BOT v1.0             ║");
        System.out.println("║     с улучшенной калибровкой        ║");
        System.out.println("╚══════════════════════════════════════╝");

        System.out.println("\nВыберите режим запуска:");
        System.out.println("1. Консольный режим (симуляция)");
        System.out.println("2. Реальный режим (с захватом экрана)");
        System.out.print("Ваш выбор: ");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        try {
            if (choice == 1) {
                org.minesweeper.ui.BotConsole console = new org.minesweeper.ui.BotConsole();
                console.start();
            } else {
                MinesweeperBotApp app = new MinesweeperBotApp();
                app.initialize();
                app.run();
            }
        } catch (Exception e) {
            Logger.getInstance().error("Критическая ошибка", e);
            e.printStackTrace();
        }
    }
}