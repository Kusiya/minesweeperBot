package org.minesweeper.vision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Загружает и хранит эталонные изображения цифр и символов
 */
public class TemplateLoader {
    private final Map<Integer, BufferedImage> templates;
    private String templatePath = "templates/";

    public TemplateLoader() {
        this.templates = new HashMap<>();
    }

    /**
     * Загрузить все шаблоны из папки templates/
     */
    public void loadAllTemplates() {
        System.out.println("\n📁 Загрузка шаблонов из папки: " +
                new File(templatePath).getAbsolutePath());

        // Загружаем цифры 0-8
        for (int i = 0; i <= 8; i++) {
            loadTemplate(i, i + ".png");
        }

        // Загружаем специальные шаблоны
        loadTemplate(-1, "mine.png");
        loadTemplate(-2, "closed.png");
        loadTemplate(-3, "flag.png");

        System.out.println("📊 Загружено шаблонов: " + templates.size());
    }

    /**
     * Загрузить один шаблон
     */
    private void loadTemplate(int value, String filename) {
        File file = new File(templatePath + filename);
        try {
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                templates.put(value, img);
                System.out.println("  ✓ Загружен: " + filename + " -> значение " + value);
            } else {
                System.out.println("  ✗ Не найден: " + filename);
            }
        } catch (IOException e) {
            System.out.println("  ✗ Ошибка загрузки " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Получить все шаблоны
     */
    public Map<Integer, BufferedImage> getAllTemplates() {
        return templates;
    }

    /**
     * Получить конкретный шаблон
     */
    public BufferedImage getTemplate(int value) {
        return templates.get(value);
    }

    /**
     * Проверить, загружен ли шаблон
     */
    public boolean hasTemplate(int value) {
        return templates.containsKey(value);
    }

    /**
     * Получить количество загруженных шаблонов
     */
    public int getTemplateCount() {
        return templates.size();
    }

    /**
     * Установить путь к шаблонам
     */
    public void setTemplatePath(String path) {
        this.templatePath = path;
    }
}