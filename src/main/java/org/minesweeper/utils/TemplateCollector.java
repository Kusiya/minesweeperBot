package org.minesweeper.utils;

import org.minesweeper.vision.ScreenCapture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Утилита для сбора и сохранения шаблонов клеток.
 * Помогает создать набор шаблонов для распознавания.
 */
public class TemplateCollector {
    private ScreenCapture screenCapture;
    private String outputDirectory;
    private Logger logger;
    private Scanner scanner;

    public TemplateCollector(String outputDirectory) throws AWTException {
        this.screenCapture = new ScreenCapture();
        this.outputDirectory = outputDirectory;
        this.logger = Logger.getInstance();
        this.scanner = new Scanner(System.in);

        // Создаем директорию, если её нет
        new File(outputDirectory).mkdirs();

        logger.info("TemplateCollector инициализирован, директория: " + outputDirectory);
    }

    /**
     * Захват и сохранение шаблона клетки
     */
    public void captureTemplate(String name, int x, int y, int width, int height)
            throws Exception {

        logger.info("Захват шаблона '" + name + "' в позиции (" + x + ", " + y + ")");

        BufferedImage screen = screenCapture.captureScreen();
        BufferedImage template = screen.getSubimage(x, y, width, height);

        File outputFile = new File(outputDirectory, name + ".png");
        ImageIO.write(template, "png", outputFile);

        logger.info("Шаблон сохранен: " + outputFile.getAbsolutePath());
        System.out.println("✓ Шаблон сохранен: " + name);
    }

    /**
     * Интерактивный сбор шаблонов
     */
    public void interactiveCollect() throws Exception {
        System.out.println("\n=== ИНТЕРАКТИВНЫЙ СБОР ШАБЛОНОВ ===");
        System.out.println("Наведите мышь на нужную клетку и нажмите Enter");

        while (true) {
            System.out.println("\n1. Собрать шаблон цифры");
            System.out.println("2. Собрать шаблон мины");
            System.out.println("3. Собрать шаблон флага");
            System.out.println("4. Собрать шаблон пустой клетки");
            System.out.println("5. Собрать шаблон закрытой клетки");
            System.out.println("6. Выход");
            System.out.print("Выберите тип: ");

            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 6) break;

            System.out.print("Введите имя шаблона (например, digit_1_1): ");
            String name = scanner.nextLine();

            System.out.println("Наведите мышь на левый верхний угол клетки...");
            waitForEnter();
            Point topLeft = getMousePosition();

            System.out.println("Наведите мышь на правый нижний угол клетки...");
            waitForEnter();
            Point bottomRight = getMousePosition();

            int width = bottomRight.x - topLeft.x;
            int height = bottomRight.y - topLeft.y;

            captureTemplate(name, topLeft.x, topLeft.y, width, height);
        }
    }

    /**
     * Автоматический сбор всех шаблонов с доски
     */
    public void collectAllTemplates(int rows, int cols, int cellSize, Point boardOffset)
            throws Exception {

        System.out.println("\n=== АВТОМАТИЧЕСКИЙ СБОР ШАБЛОНОВ ===");
        System.out.println("Будет выполнено распознавание всех клеток");
        System.out.println("Для каждой клетки укажите её тип");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Вычисляем координаты клетки
                int x = boardOffset.x + j * cellSize;
                int y = boardOffset.y + i * cellSize;

                // Захватываем изображение клетки
                BufferedImage cellImage = captureCell(i, j, cellSize, boardOffset);

                // Сохраняем временный файл для просмотра
                File tempFile = new File(outputDirectory, "temp.png");
                ImageIO.write(cellImage, "png", tempFile);

                System.out.println("\nКлетка (" + i + ", " + j + ")");
                System.out.println("Типы: 0-8, mine, flag, empty, closed");
                System.out.print("Введите тип: ");

                String type = scanner.nextLine().trim().toLowerCase();

                if (!type.isEmpty()) {
                    String filename = type + "_" + i + "_" + j + ".png";
                    File outputFile = new File(outputDirectory, filename);
                    ImageIO.write(cellImage, "png", outputFile);
                    System.out.println("Сохранено: " + filename);
                }

                // Удаляем временный файл
                tempFile.delete();
            }
        }

        System.out.println("\nСбор шаблонов завершен!");
    }

    /**
     * Захват изображения конкретной клетки
     */
    private BufferedImage captureCell(int row, int col, int cellSize, Point boardOffset)
            throws Exception {

        int x = boardOffset.x + col * cellSize;
        int y = boardOffset.y + row * cellSize;

        BufferedImage screen = screenCapture.captureScreen();
        return screen.getSubimage(x, y, cellSize, cellSize);
    }

    /**
     * Получение текущей позиции мыши
     */
    private Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * Ожидание нажатия Enter
     */
    private void waitForEnter() {
        System.out.println("Нажмите Enter для продолжения...");
        scanner.nextLine();
    }

    /**
     * Создание структуры директорий для шаблонов
     */
    public void createTemplateStructure() {
        String[] themes = {"default", "dark", "classic"};
        String[] types = {"digits", "mines", "flags", "empty", "closed"};

        for (String theme : themes) {
            File themeDir = new File(outputDirectory, theme);
            themeDir.mkdirs();

            for (String type : types) {
                File typeDir = new File(themeDir, type);
                typeDir.mkdirs();
            }
        }

        logger.info("Структура директорий для шаблонов создана");
    }

    /**
     * Пакетный захват всех цифр
     */
    public void captureAllDigits(int startX, int startY, int cellSize, int spacing)
            throws Exception {

        System.out.println("\n=== ЗАХВАТ ЦИФР ===");

        for (int digit = 0; digit <= 8; digit++) {
            int x = startX + digit * (cellSize + spacing);

            System.out.println("Захват цифры " + digit);
            System.out.println("Наведите мышь на клетку с цифрой " + digit);
            waitForEnter();

            BufferedImage digitImage = screenCapture.captureScreen()
                    .getSubimage(x, startY, cellSize, cellSize);

            String filename = "digit_" + digit + ".png";
            File outputFile = new File(outputDirectory, filename);
            ImageIO.write(digitImage, "png", outputFile);

            System.out.println("✓ Сохранено: " + filename);
        }
    }

    /**
     * Валидация шаблонов
     */
    public void validateTemplates() {
        System.out.println("\n=== ВАЛИДАЦИЯ ШАБЛОНОВ ===");

        File dir = new File(outputDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));

        if (files == null || files.length == 0) {
            System.out.println("Шаблоны не найдены");
            return;
        }

        System.out.println("Найдено " + files.length + " шаблонов:");

        for (File file : files) {
            try {
                BufferedImage img = ImageIO.read(file);
                System.out.printf("  %s: %dx%d\n", file.getName(),
                        img.getWidth(), img.getHeight());
            } catch (IOException e) {
                System.out.println("  Ошибка чтения: " + file.getName());
            }
        }
    }

    /**
     * Закрытие ресурсов
     */
    public void close() {
        scanner.close();
    }
}