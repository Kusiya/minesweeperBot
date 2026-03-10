package org.minesweeper;

import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.ui.BotConsole;
import org.minesweeper.utils.Logger;

/**
 * Главный класс приложения
 */
public class MinesweeperBotApp {
    public static void main(String[] args) {
        try {
            Logger.info("Запуск Minesweeper Bot...");

            // Создаем бота
            MinesweeperBot bot = new MinesweeperBot();

            // Базовая калибровка (значения нужно подобрать под вашу игру)
            bot.calibrate(100, 100, 30);

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