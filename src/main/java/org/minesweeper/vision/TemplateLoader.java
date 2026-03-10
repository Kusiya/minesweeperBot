package org.minesweeper.vision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Загружает шаблоны из папки resources
 */
public class TemplateLoader {
    private final Map<Integer, BufferedImage> templates;

    public TemplateLoader() {
        this.templates = new HashMap<>();
    }

    /**
     * Загрузить все шаблоны из resources/templates/
     */
    public void loadAllTemplates() {
        // Загружаем цифры 0-8
        for (int i = 0; i <= 8; i++) {
            loadTemplate(i, "templates/" + i + ".png");
        }

        // Загружаем специальные шаблоны
        loadTemplate(-1, "templates/mine.png");
        loadTemplate(-2, "templates/closed.png");
        loadTemplate(-3, "templates/flag.png");

        System.out.println("Загружено шаблонов: " + templates.size());
    }

    /**
     * Загрузить один шаблон из resources
     */
    private void loadTemplate(int value, String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                templates.put(value, img);
                System.out.println("  + Загружен: " + path);
            } else {
                System.out.println("  ! Не найден: " + path);
            }
        } catch (IOException e) {
            System.out.println("  ! Ошибка загрузки " + path + ": " + e.getMessage());
        }
    }

    public Map<Integer, BufferedImage> getAllTemplates() {
        return templates;
    }

    public BufferedImage getTemplate(int value) {
        return templates.get(value);
    }
}