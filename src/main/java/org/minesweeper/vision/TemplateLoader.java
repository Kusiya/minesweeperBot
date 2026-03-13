package org.minesweeper.vision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Загружает ВСЕ шаблоны из папки templates
 * Теперь хранит множественные шаблоны для каждого значения
 */
public class TemplateLoader {
    // Изменяем структуру: теперь для каждого значения храним СПИСОК шаблонов
    private final Map<Integer, List<BufferedImage>> templates;
    private String templatePath = "templates/";

    public TemplateLoader() {
        this.templates = new HashMap<>();

        // Инициализируем списки для всех значений
        for (int i = 0; i <= 8; i++) {
            templates.put(i, new ArrayList<>());
        }
        templates.put(-1, new ArrayList<>()); // MINE
        templates.put(-2, new ArrayList<>()); // CLOSED/UNKNOWN
        templates.put(-3, new ArrayList<>()); // FLAG
    }

    /**
     * Загрузить ВСЕ шаблоны из папки
     */
    public void loadAllTemplates() {
        File dir = new File(templatePath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("⚠️ Папка не найдена: " + templatePath);
            return;
        }

        File[] files = dir.listFiles((d, name) ->
                name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg"));

        if (files == null || files.length == 0) {
            System.out.println("⚠️ В папке нет файлов шаблонов");
            return;
        }

        System.out.println("\n📁 ЗАГРУЗКА ШАБЛОНОВ ИЗ ПАПКИ: " + templatePath);
        System.out.println("========================================");

        int totalLoaded = 0;

        for (File file : files) {
            try {
                String name = file.getName();
                BufferedImage img = ImageIO.read(file);

                // Определяем значение по первой цифре в имени файла
                int value = extractValueFromFilename(name);

                if (value != Integer.MIN_VALUE) {
                    templates.get(value).add(img);
                    totalLoaded++;
                    System.out.printf("  ✅ %s -> значение %d (всего: %d)%n",
                            name, value, templates.get(value).size());
                } else {
                    System.out.println("  ❓ " + name + " - не удалось определить значение");
                }

            } catch (IOException e) {
                System.out.println("  ❌ Ошибка загрузки " + file.getName() + ": " + e.getMessage());
            }
        }

        // Выводим статистику
        System.out.println("\n📊 СТАТИСТИКА ЗАГРУЗКИ:");
        for (Map.Entry<Integer, List<BufferedImage>> entry : templates.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue().size();
            String valueName = getValueName(value);
            System.out.printf("  %s (%d): %d шаблонов%n", valueName, value, count);
        }
        System.out.println("========================================");
        System.out.println("✅ Всего загружено: " + totalLoaded + " шаблонов\n");
    }

    /**
     * Извлекает значение из имени файла
     * Поддерживает форматы: 1.png, 1_001.png, 1-001.png, mine.png, flag.png, closed.png
     */
    private int extractValueFromFilename(String filename) {
        String name = filename.toLowerCase();

        // Специальные имена
        if (name.contains("mine")) return -1;
        if (name.contains("flag")) return -3;
        if (name.contains("closed") || name.contains("unknown")) return -2;
        if (name.contains("empty") || name.contains("0")) return 0;

        // Пробуем извлечь первую цифру
        try {
            // Ищем первую цифру в имени
            for (int i = 0; i < name.length(); i++) {
                if (Character.isDigit(name.charAt(i))) {
                    // Берем цифру
                    int digit = Character.getNumericValue(name.charAt(i));
                    if (digit >= 1 && digit <= 8) {
                        return digit;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // Игнорируем
        }

        return Integer.MIN_VALUE;
    }

    private String getValueName(int value) {
        switch (value) {
            case -3: return "FLAG";
            case -2: return "CLOSED";
            case -1: return "MINE";
            case 0: return "EMPTY";
            default: return String.valueOf(value);
        }
    }

    /**
     * Получить ВСЕ шаблоны для указанного значения
     */
    public List<BufferedImage> getTemplates(int value) {
        return templates.getOrDefault(value, new ArrayList<>());
    }

    /**
     * Получить все значения и их шаблоны
     */
    public Map<Integer, List<BufferedImage>> getAllTemplates() {
        return templates;
    }

    public void setTemplatePath(String path) {
        this.templatePath = path;
    }
}