import org.minesweeper.bot.MinesweeperBot;
import org.minesweeper.core.Move;

import java.util.Scanner;

public class TestMinesweeprrGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Игра Сапер с ботом ===");
        System.out.print("Введите количество строк: ");
        int rows = scanner.nextInt();
        System.out.print("Введите количество столбцов: ");
        int cols = scanner.nextInt();
        System.out.print("Введите количество мин: ");
        int mines = scanner.nextInt();

        MinesweeperBot bot = new MinesweeperBot(rows, cols, mines);

        System.out.println("\nВыберите режим:");
        System.out.println("1 - Играть самому");
        System.out.println("2 - Наблюдать за ботом");
        System.out.print("Ваш выбор: ");
        int mode = scanner.nextInt();

        if (mode == 1) {
            playManually(bot, scanner);
        } else {
            watchBot(bot);
        }

        scanner.close();
    }

    private static void playManually(MinesweeperBot bot, Scanner scanner) {
        System.out.println("\nПервый ход (введите координаты):");
        System.out.print("Строка: ");
        int firstRow = scanner.nextInt();
        System.out.print("Столбец: ");
        int firstCol = scanner.nextInt();

        bot.placeMines(firstRow, firstCol);
        bot.processMove(new Move(firstRow, firstCol, false));
        bot.printBoard();

        while (bot.isGameActive() && !bot.isGameWon()) {
            System.out.println("\nВаш ход:");
            System.out.print("Строка: ");
            int row = scanner.nextInt();
            System.out.print("Столбец: ");
            int col = scanner.nextInt();
            System.out.print("Флаг? (true/false): ");
            boolean isFlag = scanner.nextBoolean();

            Move move = new Move(row, col, isFlag);
            boolean success = bot.processMove(move);

            if (success) {
                bot.printBoard();
            } else {
                System.out.println("Некорректный ход!");
            }
        }

        if (bot.isGameWon()) {
            System.out.println("\nПоздравляю! Вы выиграли!");
        } else {
            System.out.println("\nИгра окончена! Вы проиграли.");
        }
    }

    private static void watchBot(MinesweeperBot bot) {
        System.out.println("\nНаблюдаем за игрой бота...");

        // Первый ход бота
        Move firstMove = bot.makeMove();
        System.out.println("Первый ход бота: " + firstMove);
        bot.placeMines(firstMove.getRow(), firstMove.getCol());
        bot.processMove(firstMove);
        bot.printBoard();

        int moveCount = 1;

        while (bot.isGameActive() && !bot.isGameWon()) {
            try {
                Thread.sleep(1000); // Задержка для наблюдения
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Move move = bot.makeMove();
            if (move == null) break;

            moveCount++;
            System.out.println("\nХод " + moveCount + ": " + move);
            boolean success = bot.processMove(move);

            if (success) {
                bot.printBoard();
            } else {
                System.out.println("Бот сделал некорректный ход!");
            }
        }

        if (bot.isGameWon()) {
            System.out.println("\nБот выиграл за " + moveCount + " ходов!");
        } else {
            System.out.println("\nБот проиграл на " + moveCount + " ходу.");
        }
    }
}
