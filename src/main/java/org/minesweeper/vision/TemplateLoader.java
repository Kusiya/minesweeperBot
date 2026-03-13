package org.minesweeper.vision;

import org.minesweeper.utils.Logger;
import org.minesweeper.utils.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Загрузка и управление шаблонами для распознавания.
 */
public class TemplateLoader {
    private Map<String, BufferedImage> templates;
    private String templateDirectory;
    private Logger logger;
    private Config config;

    public TemplateLoader() {
        this.templates = new HashMap<>();
        this.logger = Logger.getInstance();
        this.config = Config.getInstance();
        this.templateDirectory = config.getTemplateDirectory();
    }

    /**
     * Загрузка всех шаблонов из директории
     */
    public void loadTemplates(String directory) {
        this.templateDirectory = directory;
        loadTemplates();
    }

    /**
     * Загрузка всех шаблонов
     */
    public void loadTemplates() {
        File dir = new File(templateDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warning("Директория шаблонов не найдена: " + templateDirectory);
            return;
        }

        templates.clear();
        loadTemplatesFromDir(dir);

        logger.info("Загружено " + templates.size() + " шаблонов из " + templateDirectory);
    }

    /**
     * Рекурсивная загрузка шаблонов из директории
     */
    private void loadTemplatesFromDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadTemplatesFromDir(file);
            } else if (file.getName().toLowerCase().endsWith(".png")) {
                loadTemplate(file);
            }
        }
    }

    /**
     * Загрузка одного шаблона
     */
    private void loadTemplate(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            String name = getTemplateName(file);
            templates.put(name, image);
            logger.debug("Загружен шаблон: " + name);
        } catch (IOException e) {
            logger.error("Ошибка загрузки шаблона: " + file.getName(), e);
        }
    }

    /**
     * Получение имени шаблона из пути к файлу
     */
    private String getTemplateName(File file) {
        String path = file.getPath();
        // Убираем расширение .png
        String name = path.substring(0, path.length() - 4);
        // Убираем путь к директории шаблонов
        if (name.startsWith(templateDirectory)) {
            name = name.substring(templateDirectory.length());
        }
        // Заменяем разделители на точки
        return name.replace(File.separatorChar, '.').replaceAll("^\\.+", "");
    }

    /**
     * Загрузка темы оформления
     */
    public void loadTheme(String themeName) {
        String themePath = templateDirectory + File.separator + themeName;
        File themeDir = new File(themePath);

        if (!themeDir.exists() || !themeDir.isDirectory()) {
            logger.warning("Тема не найдена: " + themeName);
            return;
        }

        // Сохраняем текущие шаблоны
        Map<String, BufferedImage> oldTemplates = new HashMap<>(templates);

        // Загружаем новую тему
        templates.clear();
        loadTemplatesFromDir(themeDir);

        // Если в теме нет шаблонов, восстанавливаем старые
        if (templates.isEmpty()) {
            templates.putAll(oldTemplates);
            logger.warning("Тема " + themeName + " не содержит шаблонов");
        } else {
            logger.info("Загружена тема: " + themeName);
        }
    }

    /**
     * Получение шаблона по имени
     */
    public BufferedImage getTemplate(String name) {
        return templates.get(name);
    }

    /**
     * Получение шаблона цифры
     */
    public BufferedImage getDigitTemplate(int digit) {
        return getTemplate("digits." + digit);
    }

    /**
     * Получение всех шаблонов цифр
     */
    public Map<Integer, BufferedImage> getDigitTemplates() {
        Map<Integer, BufferedImage> digitTemplates = new HashMap<>();

        for (Map.Entry<String, BufferedImage> entry : templates.entrySet()) {
            String key = entry.getKey();
            if (key.contains("digits.") || key.matches(".*\\d.*")) {
                try {
                    int digit = extractDigit(key);
                    digitTemplates.put(digit, entry.getValue());
                } catch (NumberFormatException e) {
                    // Игнорируем
                }
            }
        }

        return digitTemplates;
    }

    /**
     * Извлечение цифры из имени шаблона
     */
    private int extractDigit(String name) {
        // Ищем последнее число в строке
        String[] parts = name.split("\\D+");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                return Integer.parseInt(parts[i]);
            }
        }
        throw new NumberFormatException("Цифра не найдена в: " + name);
    }

    /**
     * Получение шаблона мины
     */
    public BufferedImage getMineTemplate() {
        BufferedImage template = getTemplate("mine");
        if (template == null) {
            template = getTemplate("mines.mine");
        }
        return template;
    }

    /**
     * Получение шаблона флага
     */
    public BufferedImage getFlagTemplate() {
        BufferedImage template = getTemplate("flag");
        if (template == null) {
            template = getTemplate("flags.flag");
        }
        return template;
    }

    /**
     * Получение шаблона пустой клетки
     */
    public BufferedImage getEmptyTemplate() {
        BufferedImage template = getTemplate("empty");
        if (template == null) {
            template = getTemplate("empty.empty");
        }
        return template;
    }

    /**
     * Получение шаблона закрытой клетки
     */
    public BufferedImage getClosedTemplate() {
        BufferedImage template = getTemplate("closed");
        if (template == null) {
            template = getTemplate("closed.closed");
        }
        return template;
    }

    /**
     * Проверка наличия шаблона
     */
    public boolean hasTemplate(String name) {
        return templates.containsKey(name);
    }

    /**
     * Получение всех имен шаблонов
     */
    public java.util.Set<String> getTemplateNames() {
        return templates.keySet();
    }

    /**
     * Очистка кэша шаблонов
     */
    public void clear() {
        templates.clear();
        logger.debug("Кэш шаблонов очищен");
    }
}