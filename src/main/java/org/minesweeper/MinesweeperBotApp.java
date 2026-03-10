package org.minesweeper;

import java.awt.Point;
import java.awt.MouseInfo;
import org.minesweeper.bot.MinesweeperBot;
import java.awt.*;
import java.util.Scanner;

/**
 * Главный класс приложения
 */
public class MinesweeperBotApp {
    public static void main(String[] args) {
        try {
            System.out.println("Minesweeper Bot");
            System.out.println("==================");

            // Создаем бота
            MinesweeperBot bot = new MinesweeperBot();

            Scanner scanner = new Scanner(System.in);

            // Калибровка
            System.out.println("\nКАЛИБРОВКА");
            System.out.println("1. Откройте игру в Сапёра");
            System.out.println("2. Наведите мышь на ЛЕВЫЙ ВЕРХНИЙ УГОЛ первой клетки");
            System.out.println("3. Нажмите Enter...");
            scanner.nextLine();

            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            int offsetX = mousePos.x;
            int offsetY = mousePos.y;

            System.out.println("Координаты угла: (" + offsetX + ", " + offsetY + ")");

            System.out.println("\n4. Наведите мышь на ПРАВЫЙ НИЖНИЙ УГОЛ последней клетки");
            System.out.println("5. Нажмите Enter...");
            scanner.nextLine();

            mousePos = MouseInfo.getPointerInfo().getLocation();
            int bottomX = mousePos.x;
            int bottomY = mousePos.y;

            // Вычисляем размер клетки (для поля 9x9)
            int cellSize = (bottomX - offsetX) / 9;
            System.out.println("Размер клетки: " + cellSize + " пикселей");

            bot.calibrate(offsetX, offsetY, cellSize);

            // Меню
            while (true) {
                System.out.println("\nМЕНЮ");
                System.out.println("1. Запустить бота");
                System.out.println("2. Настроить скорость");
                System.out.println("3. Сменить уровень");
                System.out.println("4. Выход");
                System.out.print("Выбор: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.println("Запуск через 3 секунды...");
                        System.out.println("Быстро переключитесь на окно с игрой!");
                        Thread.sleep(3000);
                        bot.start();
                        break;
                    case "2":
                        System.out.print("Задержка между ходами (мс, рекомендуем 200-500): ");
                        try {
                            int delay = Integer.parseInt(scanner.nextLine());
                            bot.setDelay(delay);
                            System.out.println("Скорость установлена: " + delay + " мс");
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка: введите число");
                        }
                        break;
                    case "3":
                        System.out.println("Уровни сложности:");
                        System.out.println("1. Легкий (9x9, 10 мин)");
                        System.out.println("2. Средний (16x16, 40 мин)");
                        System.out.println("3. Сложный (16x30, 99 мин)");
                        System.out.print("Выбор: ");
                        String level = scanner.nextLine();
                        switch (level) {
                            case "1":
                                bot.setGameParameters(9, 9, 10);
                                System.out.println("Установлен легкий уровень");
                                break;
                            case "2":
                                bot.setGameParameters(16, 16, 40);
                                System.out.println("Установлен средний уровень");
                                break;
                            case "3":
                                bot.setGameParameters(16, 30, 99);
                                System.out.println("Установлен сложный уровень");
                                break;
                            default:
                                System.out.println("Неверный выбор");
                        }
                        break;
                    case "4":
                        System.out.println("До свидания!");
                        bot.stop();
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}