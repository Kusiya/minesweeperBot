package org.minesweeper.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Утилита для сбора шаблонов клеток из реальной игры
 */
public class TemplateCollector {

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();

        System.out.println("🎮 СБОРЩИК ШАБЛОНОВ ДЛЯ САПЁРА");
        System.out.println("=================================");
        System.out.println("1. Откройте игру в Сапёра");
        System.out.println("2. Убедитесь, что видно разные клетки:");
        System.out.println("   - Закрытые клетки");
        System.out.println("   - Пустые открытые");
        System.out.println("   - Цифры 1-8");
        System.out.println("   - Флаги");
        System.out.println("   - Мины (после проигрыша)");
        System.out.println("3. Нажмите Enter для начала сбора...");
        System.in.read();

        // Создаем папку для шаблонов
        File templatesDir = new File("templates");
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }

        // Сбор шаблонов
        collectTemplate(robot, "closed", "закрытая клетка");
        collectTemplate(robot, "0", "пустая открытая клетка");
        collectTemplate(robot, "1", "цифра 1");
        collectTemplate(robot, "2", "цифра 2");
        collectTemplate(robot, "3", "цифра 3");
        collectTemplate(robot, "4", "цифра 4");
        collectTemplate(robot, "5", "цифра 5");
        collectTemplate(robot, "6", "цифра 6");
        collectTemplate(robot, "7", "цифра 7");
        collectTemplate(robot, "8", "цифра 8");
        collectTemplate(robot, "flag", "флаг");
        collectTemplate(robot, "mine", "мина");

        System.out.println("\n✅ Сбор шаблонов завершен!");
        System.out.println("Шаблоны сохранены в папке: " + templatesDir.getAbsolutePath());
    }

    private static void collectTemplate(Robot robot, String name, String description) throws Exception {
        System.out.println("\n📸 Сбор шаблона: " + name + " (" + description + ")");
        System.out.println("Наведите мышь на центр такой клетки и нажмите Enter...");
        System.in.read();

        // Получаем позицию мыши
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        System.out.println("Позиция: (" + mousePos.x + ", " + mousePos.y + ")");

        // Захватываем область вокруг курсора (40x40 пикселей)
        Rectangle area = new Rectangle(mousePos.x - 20, mousePos.y - 20, 40, 40);
        BufferedImage screenshot = robot.createScreenCapture(area);

        // Сохраняем в разных размерах
        String filename = "templates/" + name + ".png";
        ImageIO.write(screenshot, "png", new File(filename));
        System.out.println("  ✓ Сохранен: " + filename);

        // Создаем уменьшенную версию для сравнения
        BufferedImage small = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.drawImage(screenshot, 0, 0, 20, 20, null);
        g.dispose();

        String smallFilename = "templates/" + name + "_small.png";
        ImageIO.write(small, "png", new File(smallFilename));
        System.out.println("  ✓ Сохранен уменьшенный: " + smallFilename);

        // Небольшая пауза
        Thread.sleep(500);
    }
}
