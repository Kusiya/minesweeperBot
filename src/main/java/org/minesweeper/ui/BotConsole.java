package org.minesweeper.ui;

import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.utils.Logger;
import java.util.Scanner;

/**
 * Консольный интерфейс для управления ботом
 */
public class BotConsole {
    private MinesweeperBot bot;
    private Scanner scanner;
    private boolean running;

    public BotConsole(MinesweeperBot bot) {
        this.bot = bot;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    /**
     * Запустить консольный интерфейс
     */
    public void start() {
        printWelcome();

        while (running) {
            printMenu();
            String choice = scanner.nextLine();
            handleChoice(choice);
        }
    }

    private void printWelcome() {
        System.out.println("=================================");
        System.out.println("   Minesweeper Bot v1.0");
        System.out.println("=================================");
    }

    private void printMenu() {
        System.out.println("\nВыберите действие:");
        System.out.println("1. Запустить бота");
        System.out.println("2. Остановить бота");
        System.out.println("3. Калибровка");
        System.out.println("4. Статистика");
        System.out.println("5. Настройки");
        System.out.println("6. Выход");
        System.out.print("> ");
    }

    private void handleChoice(String choice) {
        switch (choice) {
            case "1":
                startBot();
                break;
            case "2":
                stopBot();
                break;
            case "3":
                calibrate();
                break;
            case "4":
                showStatistics();
                break;
            case "5":
                showSettings();
                break;
            case "6":
                exit();
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private void startBot() {
        System.out.println("Запуск бота...");
        new Thread(() -> {
            try {
                bot.start();
            } catch (Exception e) {
                Logger.error("Ошибка при запуске бота: " + e.getMessage());
            }
        }).start();
    }

    private void stopBot() {
        bot.stop();
        System.out.println("Бот остановлен");
    }

    private void calibrate() {
        System.out.println("Режим калибровки:");
        System.out.println("1. Наведите мышь на левый верхний угол поля и нажмите Enter");
        scanner.nextLine();

        // Получить координаты мыши
        // Здесь нужно добавить получение координат

        System.out.println("2. Введите размер клетки в пикселях (обычно 30-40):");
        int size = Integer.parseInt(scanner.nextLine());

        System.out.println("Калибровка завершена!");
    }

    private void showStatistics() {
        System.out.println("\n=== Статистика ===");
        System.out.println("Игр сыграно: " + bot.getGamesPlayed());
        System.out.println("Побед: " + bot.getGamesWon());
        System.out.println("Поражений: " + bot.getGamesLost());
        System.out.println("Процент побед: " +
                String.format("%.1f%%", bot.getWinRate() * 100));
    }

    private void showSettings() {
        System.out.println("\n=== Настройки ===");
        System.out.println("Скорость (мс между ходами): " + bot.getDelay());
        System.out.println("Стратегия: " + bot.getStrategyName());
        System.out.println("Использовать паттерны: Да");
        System.out.println("Вероятностный анализ: Да");
    }

    private void exit() {
        running = false;
        bot.stop();
        System.out.println("До свидания!");
        System.exit(0);
    }
}
