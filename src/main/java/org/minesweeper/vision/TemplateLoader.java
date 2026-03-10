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
    private final Map<Integer, BufferedImage> templates; // сделали final
    private String templatePath = "templates/"; // папка с шаблонами

    public TemplateLoader() {
        this.templates = new HashMap<>();
    }

    /**
     * Загрузить все шаблоны из папки
     */
    public void loadAllTemplates() throws IOException {
        // Шаблоны должны называться: 0.png, 1.png, 2.png, ... , 8.png, mine.png, flag.png
        loadTemplate(0, "0.png");
        loadTemplate(1, "1.png");
        loadTemplate(2, "2.png");
        loadTemplate(3, "3.png");
        loadTemplate(4, "4.png");
        loadTemplate(5, "5.png");
        loadTemplate(6, "6.png");
        loadTemplate(7, "7.png");
        loadTemplate(8, "8.png");
        loadTemplate(-1, "mine.png");
        loadTemplate(-2, "closed.png");
        loadTemplate(-3, "flag.png");

        System.out.println("Загружено шаблонов: " + templates.size());
    }

    /**
     * Загрузить один шаблон
     */
    private void loadTemplate(int value, String filename) throws IOException {
        File file = new File(templatePath + filename);
        if (file.exists()) {
            BufferedImage img = ImageIO.read(file);
            templates.put(value, img);
            System.out.println("  + Загружен шаблон: " + filename + " для значения " + value);
        } else {
            System.out.println("  ! Внимание: шаблон не найден: " + filename);
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
     * Установить путь к шаблонам
     */
    public void setTemplatePath(String path) {
        this.templatePath = path;
    }

    /**
     * Получить количество загруженных шаблонов
     */
    public int getTemplateCount() {
        return templates.size();
    }
}