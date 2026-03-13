package org.minesweeper.ui;

import org.minesweeper.bot.AdvancedMinesweeperBot;
import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.*;
import org.minesweeper.strategy.pattern.*;
import org.minesweeper.utils.Config;
import org.minesweeper.utils.Logger;
import org.minesweeper.utils.BotStatistics;

import java.util.Scanner;

/**
 * Консольный интерфейс для управления ботом.
 * Позволяет настраивать параметры и наблюдать за игрой.
 */
public class BotConsole {
    private Scanner scanner;
    private MinesweeperBot bot;
    private BotStatistics statistics;
    private Logger logger;
    private Config config;
    private boolean running;

    public BotConsole() {
        this.scanner = new Scanner(System.in);
        this.statistics = new BotStatistics();
        this.logger = Logger.getInstance();
        this.config = Config.getInstance();
        this.running = true;
    }

    /**
     * Главный метод запуска консоли
     */
    public void start() {
        printWelcome();

        while (running) {
            printMainMenu();
            int choice = readInt(1, 6);
            handleMainMenuChoice(choice);
        }

        scanner.close();
        logger.info("Программа завершена");
    }

    /**
     * Приветственное сообщение
     */
    private void printWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     Minesweeper Bot v1.0             ║");
        System.out.println("║     Бот для игры в Сапера            ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    /**
     * Главное меню
     */
    private void printMainMenu() {
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("1. Начать новую игру");
        System.out.println("2. Наблюдать за ботом");
        System.out.println("3. Запустить тестирование");
        System.out.println("4. Настройки");
        System.out.println("5. Статистика");
        System.out.println("6. Выход");
        System.out.print("Выберите опцию: ");
    }

    /**
     * Обработка выбора главного меню
     */
    private void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1:
                startNewGame();
                break;
            case 2:
                watchBot();
                break;
            case 3:
                runBenchmark();
                break;
            case 4:
                showSettings();
                break;
            case 5:
                statistics.printStatistics();
                break;
            case 6:
                running = false;
                break;
        }
    }

    /**
     * Начало новой игры
     */
    private void startNewGame() {
        System.out.println("\n=== НОВАЯ ИГРА ===");

        // Выбор сложности
        System.out.println("Выберите сложность:");
        System.out.println("1. Новичок (9x9, 10 мин)");
        System.out.println("2. Любитель (16x16, 40 мин)");
        System.out.println("3. Профессионал (16x30, 99 мин)");
        System.out.println("4. Свои параметры");
        System.out.print("Ваш выбор: ");

        int difficulty = readInt(1, 4);
        int rows, cols, mines;

        switch (difficulty) {
            case 1:
                rows = 9; cols = 9; mines = 10;
                break;
            case 2:
                rows = 16; cols = 16; mines = 40;
                break;
            case 3:
                rows = 16; cols = 30; mines = 99;
                break;
            default:
                System.out.print("Введите количество строк: ");
                rows = readInt(1, 100);
                System.out.print("Введите количество столбцов: ");
                cols = readInt(1, 100);
                System.out.print("Введите количество мин: ");
                mines = readInt(1, rows * cols - 1);
        }

        // Выбор типа бота
        System.out.println("\nВыберите тип бота:");
        System.out.println("1. Обычный бот");
        System.out.println("2. Продвинутый бот");
        System.out.print("Ваш выбор: ");

        int botType = readInt(1, 2);

        if (botType == 1) {
            bot = new MinesweeperBot(rows, cols, mines);
        } else {
            bot = new AdvancedMinesweeperBot(rows, cols, mines);
            // Добавляем стратегии
            ((AdvancedMinesweeperBot) bot).addStrategy(new BasicStrategy());
            ((AdvancedMinesweeperBot) bot).addStrategy(new AdvancedStrategy());
        }

        // Режим игры
        System.out.println("\nРежим игры:");
        System.out.println("1. Играть самому");
        System.out.println("2. Наблюдать за ботом");
        System.out.print("Ваш выбор: ");

        int mode = readInt(1, 2);

        if (mode == 1) {
            playManually();
        } else {
            watchBotSingle();
        }
    }

    /**
     * Ручная игра
     */
    private void playManually() {
        System.out.println("\n=== Ручная игра ===");
        System.out.println("Введите координаты первого хода:");
        System.out.print("Строка: ");
        int firstRow = readInt(0, bot.rows - 1);
        System.out.print("Столбец: ");
        int firstCol = readInt(0, bot.cols - 1);

        bot.placeMines(firstRow, firstCol);
        bot.processMove(new Move(firstRow, firstCol, false));
        bot.printBoard();

        while (bot.isGameActive() && !bot.isGameWon()) {
            System.out.println("\nВаш ход:");
            System.out.print("Строка: ");
            int row = readInt(0, bot.rows - 1);
            System.out.print("Столбец: ");
            int col = readInt(0, bot.cols - 1);
            System.out.print("Действие (1-открыть, 2-флаг): ");
            int action = readInt(1, 2);

            Move move = new Move(row, col, action == 2);
            boolean success = bot.processMove(move);

            if (success) {
                bot.printBoard();
            } else {
                System.out.println("Некорректный ход!");
            }
        }

        if (bot.isGameWon()) {
            System.out.println("\nПОЗДРАВЛЯЮ! Вы выиграли!");
        } else {
            System.out.println("\nИгра окончена! Вы проиграли.");
            bot.printBoard();
        }
    }

    /**
     * Наблюдение за одной игрой бота
     */
    private void watchBotSingle() {
        System.out.println("\n=== Наблюдение за ботом ===");

        Move firstMove = bot.makeMove();
        System.out.println("Первый ход: " + firstMove);
        bot.placeMines(firstMove.getRow(), firstMove.getCol());
        bot.processMove(firstMove);
        bot.printBoard();

        int moveCount = 1;

        while (bot.isGameActive() && !bot.isGameWon()) {
            try {
                Thread.sleep(config.getAnimationDelay());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Move move = bot.makeMove();
            if (move == null) break;

            moveCount++;
            System.out.println("\nХод " + moveCount + ": " + move);
            bot.processMove(move);
            bot.printBoard();
        }

        if (bot.isGameWon()) {
            System.out.println("\nБОТ ВЫИГРАЛ за " + moveCount + " ходов!");
        } else {
            System.out.println("\nБот проиграл на " + moveCount + " ходу.");
        }
    }

    /**
     * Наблюдение за ботом в автоматическом режиме
     */
    private void watchBot() {
        System.out.println("\n=== РЕЖИМ НАБЛЮДЕНИЯ ===");
        System.out.println("Бот будет играть автоматически");

        while (true) {
            System.out.println("\n1. Одна игра");
            System.out.println("2. Несколько игр");
            System.out.println("3. Назад");
            System.out.print("Выбор: ");

            int choice = readInt(1, 3);

            if (choice == 1) {
                startNewGame();
            } else if (choice == 2) {
                runMultipleGames();
            } else {
                break;
            }
        }
    }

    /**
     * Запуск нескольких игр подряд
     */
    private void runMultipleGames() {
        System.out.print("Введите количество игр: ");
        int games = readInt(1, 1000);

        System.out.print("Использовать продвинутого бота? (1-да, 2-нет): ");
        boolean useAdvanced = readInt(1, 2) == 1;

        System.out.println("Выберите сложность:");
        System.out.println("1. Новичок (9x9, 10 мин)");
        System.out.println("2. Любитель (16x16, 40 мин)");
        System.out.println("3. Профессионал (16x30, 99 мин)");
        System.out.print("Выбор: ");

        int difficulty = readInt(1, 3);
        int rows, cols, mines;

        switch (difficulty) {
            case 1:
                rows = 9; cols = 9; mines = 10;
                break;
            case 2:
                rows = 16; cols = 16; mines = 40;
                break;
            default:
                rows = 16; cols = 30; mines = 99;
        }

        System.out.println("\nЗапуск " + games + " игр...");

        for (int i = 0; i < games; i++) {
            System.out.print("Игра " + (i + 1) + ": ");

            MinesweeperBot testBot;
            if (useAdvanced) {
                testBot = new AdvancedMinesweeperBot(rows, cols, mines);
                ((AdvancedMinesweeperBot) testBot).addStrategy(new BasicStrategy());
                ((AdvancedMinesweeperBot) testBot).addStrategy(new AdvancedStrategy());
            } else {
                testBot = new MinesweeperBot(rows, cols, mines);
            }

            long startTime = System.currentTimeMillis();
            int moves = 0;

            // Первый ход
            Move firstMove = testBot.makeMove();
            testBot.placeMines(firstMove.getRow(), firstMove.getCol());
            testBot.processMove(firstMove);
            moves++;

            // Играем до конца
            while (testBot.isGameActive() && !testBot.isGameWon()) {
                Move move = testBot.makeMove();
                if (move == null) break;
                testBot.processMove(move);
                moves++;
            }

            boolean won = testBot.isGameWon();
            long time = System.currentTimeMillis() - startTime;

            statistics.recordGame(won, moves, time, useAdvanced ? "advanced" : "basic");

            System.out.println((won ? "ПОБЕДА" : "ПОРАЖЕНИЕ") +
                    " за " + moves + " ходов, " + time + " мс");
        }

        System.out.println("\nТестирование завершено!");
        statistics.printStatistics();
    }

    /**
     * Запуск бенчмарка
     */
    private void runBenchmark() {
        System.out.println("\n=== БЕНЧМАРК ===");
        System.out.println("Тестирование производительности...");

        int[] sizes = {9, 16, 16};
        int[] mines = {10, 40, 99};
        String[] names = {"Новичок", "Любитель", "Профессионал"};

        for (int test = 0; test < sizes.length; test++) {
            System.out.println("\nТест " + names[test] + ":");

            // Тест обычного бота
            MinesweeperBot basicBot = new MinesweeperBot(sizes[test], sizes[test], mines[test]);
            long startTime = System.currentTimeMillis();

            Move firstMove = basicBot.makeMove();
            basicBot.placeMines(firstMove.getRow(), firstMove.getCol());
            basicBot.processMove(firstMove);

            int moves = 1;
            while (basicBot.isGameActive() && !basicBot.isGameWon() && moves < 100) {
                Move move = basicBot.makeMove();
                if (move == null) break;
                basicBot.processMove(move);
                moves++;
            }

            long basicTime = System.currentTimeMillis() - startTime;

            // Тест продвинутого бота
            AdvancedMinesweeperBot advancedBot = new AdvancedMinesweeperBot(
                    sizes[test], sizes[test], mines[test]);
            advancedBot.addStrategy(new BasicStrategy());
            advancedBot.addStrategy(new AdvancedStrategy());

            startTime = System.currentTimeMillis();

            firstMove = advancedBot.makeMove();
            advancedBot.placeMines(firstMove.getRow(), firstMove.getCol());
            advancedBot.processMove(firstMove);

            moves = 1;
            while (advancedBot.isGameActive() && !advancedBot.isGameWon() && moves < 100) {
                Move move = advancedBot.makeMove();
                if (move == null) break;
                advancedBot.processMove(move);
                moves++;
            }

            long advancedTime = System.currentTimeMillis() - startTime;

            System.out.printf("  Обычный бот: %d мс\n", basicTime);
            System.out.printf("  Продвинутый бот: %d мс\n", advancedTime);
            System.out.printf("  Разница: %.2fx\n", (double) advancedTime / basicTime);
        }
    }

    /**
     * Настройки
     */
    private void showSettings() {
        System.out.println("\n=== НАСТРОЙКИ ===");
        System.out.println("1. Задержка анимации: " + config.getAnimationDelay() + " мс");
        System.out.println("2. Уровень логирования: " + config.getLogLevel());
        System.out.println("3. Показывать вероятности: " + (config.showProbabilities() ? "Да" : "Нет"));
        System.out.println("4. Режим отладки: " + (config.isDebugMode() ? "Да" : "Нет"));
        System.out.println("5. Назад");
        System.out.print("Выберите параметр для изменения: ");

        int choice = readInt(1, 5);

        switch (choice) {
            case 1:
                System.out.print("Введите задержку (мс): ");
                int delay = readInt(0, 5000);
                config.setAnimationDelay(delay);
                break;
            case 2:
                System.out.println("Уровни: DEBUG, INFO, WARNING, ERROR");
                System.out.print("Введите уровень: ");
                String level = scanner.next();
                config.setLogLevel(level);
                break;
            case 3:
                config.setShowProbabilities(!config.showProbabilities());
                break;
            case 4:
                config.setDebugMode(!config.isDebugMode());
                break;
        }

        config.save();
        logger.info("Настройки сохранены");
    }

    /**
     * Чтение целого числа с проверкой
     */
    private int readInt(int min, int max) {
        while (true) {
            try {
                int value = scanner.nextInt();
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.print("Введите число от " + min + " до " + max + ": ");
            } catch (Exception e) {
                System.out.print("Некорректный ввод. Повторите: ");
                scanner.next();
            }
        }
    }
}