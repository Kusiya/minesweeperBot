package org.minesweeper.ui;

import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.execution.MouseController;
import org.minesweeper.utils.Logger;
import org.minesweeper.strategy.BasicStrategy;
import org.minesweeper.strategy.AdvancedStrategy;

import java.awt.*;
import java.util.Scanner;
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
        System.out.println("8. 🚨 ЭКСТРЕННАЯ ОСТАНОВКА (ESC)");
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
                emergencyStop();
                break;
            case "9":
                exit();
                break;
            default:
                System.out.println("❌ Неверный выбор");
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

        Point p1 = MouseController.getMousePosition();
        System.out.println("   Координаты: (" + p1.x + ", " + p1.y + ")");

        System.out.println("2. Наведите мышь на ПРАВЫЙ НИЖНИЙ угол поля и нажмите Enter");
        scanner.nextLine();

        Point p2 = MouseController.getMousePosition();
        System.out.println("   Координаты: (" + p2.x + ", " + p2.y + ")");

        int cellSize = (p2.x - p1.x) / 9; // предполагаем 9x9 поле
        System.out.println("3. Размер клетки: " + cellSize + " пикселей");

        bot.calibrate(p1.x, p1.y, cellSize);
        System.out.println("✅ Калибровка завершена!");
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