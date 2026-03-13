package org.minesweeper;

import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.ui.BotConsole;
import org.minesweeper.utils.Logger;

import java.util.Scanner;

public class MinesweeperBotApp {
    public static void main(String[] args) {
        try {
            Logger.info("Запуск Minesweeper Bot...");

            // Создаем бота
            MinesweeperBot bot = new MinesweeperBot();

            // Выбор уровня сложности
            Scanner scanner = new Scanner(System.in);

            System.out.println("\n🎮 ВЫБОР УРОВНЯ СЛОЖНОСТИ");
            System.out.println("1. Легкий (9x9, 10 мин)");
            System.out.println("1" +
                    "2. Средний (16x16, 40 мин)");
            System.out.println("3. Сложный (16x30, 99 мин)");
            System.out.print("Выбор (1-3): ");

            String level = scanner.nextLine();

            switch (level) {
                case "1":
                    bot.setGameParameters(9, 9, 10);
                    System.out.println("✅ Установлен легкий уровень");
                    break;
                case "2":
                    bot.setGameParameters(16, 16, 40);
                    System.out.println("✅ Установлен средний уровень");
                    break;
                case "3":
                    bot.setGameParameters(16, 30, 99);
                    System.out.println("✅ Установлен сложный уровень");
                    break;
                default:
                    bot.setGameParameters(9, 9, 10);
                    System.out.println("⚠️ Неверный выбор. Установлен легкий уровень");
                    break;
            }

            // Запускаем консольный интерфейс
            BotConsole console = new BotConsole(bot);
            console.start();

        } catch (Exception e) {
            Logger.error("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
