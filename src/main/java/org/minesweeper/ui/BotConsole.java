package org.minesweeper.ui;

import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.execution.MouseController;
import org.minesweeper.utils.Logger;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.strategy.AdvancedStrategy;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

public class BotConsole {
    private MinesweeperBot bot;
    private Scanner scanner;
    private boolean consoleRunning;
    private javax.swing.Timer emergencyTimer;
    private JFrame hiddenFrame; // Сохраняем ссылку на скрытое окно

    public BotConsole(MinesweeperBot bot) {
        this.bot = bot;
        this.scanner = new Scanner(System.in);
        this.consoleRunning = true;

        setupEmergencyListener();

        // Добавляем обработчик Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n📢 Получен сигнал завершения (Ctrl+C)");
            emergencyStop();
        }));
    }

    private void setupEmergencyListener() {
        // Создаем скрытое окно для прослушивания клавиш
        hiddenFrame = new JFrame();
        hiddenFrame.setUndecorated(true);
        hiddenFrame.setSize(0, 0);
        hiddenFrame.setLocationRelativeTo(null);
        hiddenFrame.setVisible(true);

        // Регистрируем горячие клавиши
        JPanel panel = new JPanel();

        // ESC для экстренной остановки
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "emergencyStop");
        panel.getActionMap().put("emergencyStop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emergencyStop();
            }
        });

        // Пробел для паузы
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePause");
        panel.getActionMap().put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bot.isRunning()) {
                    bot.togglePause();
                }
            }
        });

        hiddenFrame.add(panel);
        hiddenFrame.requestFocus();

        // Таймер для периодического возврата фокуса
        emergencyTimer = new javax.swing.Timer(1000, e -> {
            if (!hiddenFrame.hasFocus() && consoleRunning) {
                hiddenFrame.requestFocus();
            }
        });
        emergencyTimer.start();
    }

    // ⚠️ ВАЖНО: Этот метод должен быть на уровне класса, а не внутри setupEmergencyListener!
    private void emergencyStop() {
        System.out.println("\n🚨 ЭКСТРЕННАЯ ОСТАНОВКА!");

        // Останавливаем бота
        bot.emergencyStop();

        // Возвращаем мышь в безопасное место
        try {
            Robot robot = new Robot();
            robot.mouseMove(0, 0);

            // Имитируем нажатие ESC для выхода из возможных меню
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (AWTException ex) {
            // Игнорируем
        }

        // Даем время на освобождение ресурсов
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Мышь освобождена. Управление возвращено.");
    }

    public void start() {
        printWelcome();

        while (consoleRunning) {
            try {
                printMenu();
                String choice = scanner.nextLine().trim().toLowerCase();
                handleCommand(choice);
            } catch (Exception e) {
                Logger.error("Ошибка ввода: " + e.getMessage());
            }
        }

        scanner.close();

        // Закрываем скрытое окно при выходе
        if (hiddenFrame != null) {
            hiddenFrame.dispose();
        }
        if (emergencyTimer != null) {
            emergencyTimer.stop();
        }
    }

    private void printWelcome() {
        System.out.println("\n" +
                "╔════════════════════════════════════╗\n" +
                "║     MINESWEEPER BOT v1.0          ║\n" +
                "║    Управление через консоль       ║\n" +
                "║    ESC - экстренная остановка     ║\n" +
                "╚════════════════════════════════════╝");
    }

    private void printMenu() {
        String status;
        if (!bot.isRunning()) {
            status = "⏹️ ОСТАНОВЛЕН";
        } else if (bot.isPaused()) {
            status = "⏸️ ПАУЗА";
        } else {
            status = "▶️ РАБОТАЕТ";
        }

        System.out.println("\n📌 Текущий статус: " + status);
        System.out.println("─────────────────────────────");
        System.out.println("1. 🚀 Запустить бота");
        System.out.println("2. 🛑 Остановить бота");
        System.out.println("3. ⏸️ Пауза/Продолжить");
        System.out.println("4. 📊 Статистика");
        System.out.println("5. ⚡ Изменить скорость");
        System.out.println("6. 🎯 Выбрать стратегию");
        System.out.println("7. 📐 Калибровка");
        System.out.println("8. 🧪 Тест мыши"); // НОВЫЙ ПУНКТ
        System.out.println("9. ❌ Выход");
        System.out.print("👉 Выберите действие (1-9): ");
    }

    private void handleCommand(String choice) {
        switch (choice) {
            case "1":
            case "start":
            case "🚀":
                startBot();
                break;

            case "2":
            case "stop":
            case "🛑":
                stopBot();
                break;

            case "3":
            case "pause":
            case "⏸️":
                togglePause();
                break;

            case "4":
            case "stats":
            case "📊":
                showStatistics();
                break;

            case "5":
            case "speed":
            case "⚡":
                changeSpeed();
                break;

            case "6":
            case "strategy":
            case "🎯":
                changeStrategy();
                break;

            case "7":
            case "calibrate":
            case "📐":
                calibrate();
                break;
            case "8":
            case "test":
            case "🧪":
                testMouse();
                break;
            case "9":
            case "exit":
            case "❌":
                exit();
                break;
            case "0":
                case "learn":
                learningMode();
                break;
            default:
                System.out.println("❌ Неизвестная команда. Введите 1-9");
        }
    }

    private void learningMode() {
        System.out.println("\n🎓 РЕЖИМ ОБУЧЕНИЯ");
        System.out.println("==================");

        try {
            Robot robot = new Robot();

            for (int digit = 1; digit <= 8; digit++) {
                System.out.println("\n📸 Наведите мышь на клетку с цифрой " + digit);
                System.out.println("и нажмите Enter (или 'q' для выхода)...");

                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("q")) break;

                // Получаем координаты мыши
                Point p = MouseInfo.getPointerInfo().getLocation();
                System.out.println("   Координаты: (" + p.x + ", " + p.y + ")");

                // Захватываем клетку (предполагаем размер 30x30)
                Rectangle cellRect = new Rectangle(p.x - 15, p.y - 15, 30, 30);
                BufferedImage cell = robot.createScreenCapture(cellRect);

                // Анализируем
                int brightness = getSimpleBrightness(cell);
                double variance = getSimpleVariance(cell);

                System.out.printf("   Яркость: %d, Вариативность: %.2f%n", brightness, variance);

                // Сохраняем
                String filename = String.format("templates/%d_%d.png",
                        digit, System.currentTimeMillis());

                File file = new File(filename);
                file.getParentFile().mkdirs();
                ImageIO.write(cell, "png", file);

                System.out.println("   ✅ Сохранено: " + filename);
            }

            System.out.println("\n🎓 Обучение завершено! Перезапустите бота.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private int getSimpleBrightness(BufferedImage img) {
        long sum = 0;
        for (int x = 0; x < img.getWidth(); x+=3) {
            for (int y = 0; y < img.getHeight(); y+=3) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                sum += (r + g + b) / 3;
            }
        }
        return (int)(sum / ((img.getWidth() * img.getHeight()) / 9));
    }

    private double getSimpleVariance(BufferedImage img) {
        List<Double> values = new ArrayList<>();
        for (int x = 0; x < img.getWidth(); x+=3) {
            for (int y = 0; y < img.getHeight(); y+=3) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                values.add((r + g + b) / 3.0);
            }
        }

        double mean = values.stream().mapToDouble(d -> d).average().orElse(0);
        double variance = values.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    private void testMouse() {
        System.out.println("\n🧪 ТЕСТ МЫШИ");
        System.out.println("Бот проверит работу мыши за 5 секунд...");
        System.out.println("Наблюдайте за курсором!");

        try {
            // Получаем MouseController через рефлексию или добавляем метод в MinesweeperBot
            // Пока просто вызовем тестовый метод
            // Временно добавим прямой тест
            Robot robot = new Robot();

            // Тест перемещения
            System.out.println("1. Перемещение на (500, 500)");
            robot.mouseMove(500, 500);
            Thread.sleep(1000);

            System.out.println("2. Левый клик");
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(1000);

            System.out.println("3. Правый клик");
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            Thread.sleep(100);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            Thread.sleep(1000);

            System.out.println("4. Возврат на (100, 100)");
            robot.mouseMove(100, 100);

            System.out.println("✅ Тест завершен");

        } catch (Exception e) {
            System.out.println("❌ Ошибка теста: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startBot() {
        if (bot.isRunning()) {
            System.out.println("⚠️ Бот уже запущен!");
            return;
        }

        System.out.print("🎲 Введите размеры поля (rows cols mines) [9 9 10]: ");
        String input = scanner.nextLine();

        if (!input.isEmpty()) {
            try {
                String[] parts = input.split(" ");
                if (parts.length == 3) {
                    int rows = Integer.parseInt(parts[0]);
                    int cols = Integer.parseInt(parts[1]);
                    int mines = Integer.parseInt(parts[2]);
                    bot.setGameParameters(rows, cols, mines);
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Неверный формат, использую значения по умолчанию");
            }
        }

        System.out.println("🚀 Запуск бота через 3 секунды...");
        System.out.println("Быстро переключитесь на окно с игрой!");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        bot.start();
    }

    private void stopBot() {
        if (!bot.isRunning()) {
            System.out.println("⚠️ Бот не запущен!");
            return;
        }

        bot.stop();
    }

    private void togglePause() {
        bot.togglePause();
    }

    private void showStatistics() {
        System.out.println("\n📊 СТАТИСТИКА");
        System.out.println("────────────────");
        System.out.printf("Игр сыграно:    %d%n", bot.getGamesPlayed());
        System.out.printf("Побед:          %d%n", bot.getGamesWon());
        System.out.printf("Поражений:      %d%n", bot.getGamesLost());
        System.out.printf("Процент побед:  %.1f%%%n", bot.getWinRate() * 100);
        System.out.printf("Скорость:       %d мс/ход%n", bot.getDelay());
        System.out.printf("Статус:         %s%n",
                bot.isRunning() ? (bot.isPaused() ? "Пауза" : "Работает") : "Остановлен");
    }

    private void changeSpeed() {
        System.out.print("⚡ Введите задержку между ходами (мс) [" + bot.getDelay() + "]: ");
        String input = scanner.nextLine();

        if (!input.isEmpty()) {
            try {
                int speed = Integer.parseInt(input);
                bot.setDelay(speed);
                System.out.println("✅ Скорость изменена на " + speed + " мс");
            } catch (NumberFormatException e) {
                System.out.println("❌ Неверный формат");
            }
        }
    }

    private void changeStrategy() {
        System.out.println("🎯 Доступные стратегии:");
        System.out.println("1. Базовая (простые правила)");
        System.out.println("2. Продвинутая (с вероятностным анализом)");
        System.out.print("Выберите (1-2): ");

        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            bot.setStrategy(new BasicStrategy());
            System.out.println("✅ Выбрана базовая стратегия");
        } else if (choice.equals("2")) {
            bot.setStrategy(new AdvancedStrategy());
            System.out.println("✅ Выбрана продвинутая стратегия");
        } else {
            System.out.println("❌ Неверный выбор");
        }
    }

    private void calibrate() {
        System.out.println("\n📐 РЕЖИМ КАЛИБРОВКИ");
        System.out.println("1. Наведите мышь на ЛЕВЫЙ ВЕРХНИЙ угол поля и нажмите Enter");
        scanner.nextLine();

        Point topLeft = MouseController.getMousePosition();
        System.out.println("   Координаты: (" + topLeft.x + ", " + topLeft.y + ")");

        System.out.println("\n4. Наведите мышь на ПРАВЫЙ НИЖНИЙ УГОЛ последней клетки");
        System.out.println("5. Нажмите Enter...");
        scanner.nextLine();

        Point bottomRight = MouseController.getMousePosition();
        System.out.println("   Координаты: (" + bottomRight.x + ", " + bottomRight.y + ")");

        // Рассчитываем размер поля
        int totalWidth = bottomRight.x - topLeft.x;
        int totalHeight = bottomRight.y - topLeft.y;

        System.out.println("   Общая ширина: " + totalWidth + " пикселей");
        System.out.println("   Общая высота: " + totalHeight + " пикселей");

        // Спрашиваем размер поля
        System.out.print("\nВведите количество строк [9]: ");
        String rowsStr = scanner.nextLine();
        int rows = rowsStr.isEmpty() ? 9 : Integer.parseInt(rowsStr);

        System.out.print("Введите количество столбцов [9]: ");
        String colsStr = scanner.nextLine();
        int cols = colsStr.isEmpty() ? 9 : Integer.parseInt(colsStr);

        // Определяем размер клетки
        int cellSizeByWidth = totalWidth / cols;
        int cellSizeByHeight = totalHeight / rows;
        int cellSize = (cellSizeByWidth + cellSizeByHeight) / 2;

        System.out.println("   Размер клетки (по ширине): " + cellSizeByWidth);
        System.out.println("   Размер клетки (по высоте): " + cellSizeByHeight);
        System.out.println("   Используем средний: " + cellSize + " пикселей");

        // Устанавливаем калибровку
        bot.calibrate(topLeft.x, topLeft.y, cellSize, rows, cols);

        System.out.println("✅ Калибровка завершена!");
        System.out.println("   offset=(" + topLeft.x + "," + topLeft.y + "), cellSize=" + cellSize);
        System.out.println("   поле=" + rows + "x" + cols);
    }

    private void exit() {
        System.out.println("👋 Завершение работы...");
        if (bot.isRunning()) {
            bot.stop();
        }

        // Останавливаем таймер и закрываем окно
        if (emergencyTimer != null) {
            emergencyTimer.stop();
        }
        if (hiddenFrame != null) {
            hiddenFrame.dispose();
        }

        consoleRunning = false;
        System.out.println("До свидания!");
    }
}