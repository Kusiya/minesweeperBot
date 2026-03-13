package org.minesweeper.utils;

import java.io.*;
import java.util.Properties;

public class Config {
    private static Config instance;
    private Properties properties;
    private String configFile = "minesweeper.properties";
    private Logger logger;  // Теперь может быть null при инициализации

    /**
     * Приватный конструктор - ТЕПЕРЬ НЕ ИСПОЛЬЗУЕТ LOGGER СРАЗУ
     */
    private Config() {
        this.properties = new Properties();
        // Не инициализируем logger здесь!
        loadProperties();
    }

    /**
     * Получение единственного экземпляра
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            // После создания Config, обновляем Logger
            Logger.getInstance().updateFromConfig(instance);
        }
        return instance;
    }

    /**
     * Загрузка настроек из файла
     */
    private void loadProperties() {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            // Не используем logger здесь, так как он может быть еще не инициализирован
            System.out.println("Конфигурация загружена из " + configFile);
        } catch (FileNotFoundException e) {
            System.out.println("Файл конфигурации не найден, создаем с настройками по умолчанию");
            createDefaultConfig();
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }

    /**
     * Создание конфигурации по умолчанию
     */
    private void createDefaultConfig() {
        // Параметры игры
        properties.setProperty("board.rows", "9");
        properties.setProperty("board.cols", "9");
        properties.setProperty("board.mines", "10");

        // Параметры бота
        properties.setProperty("bot.advanced.enabled", "true");
        properties.setProperty("bot.max.solutions", "10000");

        // Параметры интерфейса
        properties.setProperty("ui.animation.delay", "500");
        properties.setProperty("ui.show.probabilities", "true");

        // Параметры логирования
        properties.setProperty("log.level", "INFO");
        properties.setProperty("log.file", "minesweeper.log");
        properties.setProperty("log.console", "true");

        // Параметры распознавания
        properties.setProperty("vision.template.directory", "templates");
        properties.setProperty("vision.similarity.threshold", "0.85");
        properties.setProperty("vision.cell.size", "24");

        // Параметры выполнения
        properties.setProperty("execution.click.delay", "100");
        properties.setProperty("execution.debug.mode", "false");

        save();
    }

    /**
     * Сохранение настроек в файл
     */
    public void save() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Minesweeper Bot Configuration");
            // Используем logger только если он уже инициализирован
            if (logger != null) {
                logger.info("Конфигурация сохранена в " + configFile);
            } else {
                System.out.println("Конфигурация сохранена в " + configFile);
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.error("Ошибка сохранения конфигурации", e);
            } else {
                System.err.println("Ошибка сохранения конфигурации: " + e.getMessage());
            }
        }
    }

    /**
     * Установка логгера (вызывается после инициализации)
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    // ... все остальные методы get, getInt, getBoolean и т.д. остаются без изменений ...

    /**
     * Получение строкового значения
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Получение строкового значения с значением по умолчанию
     */
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Получение целочисленного значения
     */
    public int getInt(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            if (logger != null) {
                logger.error("Ошибка парсинга int для ключа: " + key);
            }
            return 0;
        }
    }

    /**
     * Получение целочисленного значения с значением по умолчанию
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Получение булевого значения
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    /**
     * Получение булевого значения с значением по умолчанию
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Получение double значения
     */
    public double getDouble(String key) {
        try {
            return Double.parseDouble(properties.getProperty(key));
        } catch (NumberFormatException e) {
            if (logger != null) {
                logger.error("Ошибка парсинга double для ключа: " + key);
            }
            return 0.0;
        }
    }

    /**
     * Получение double значения с значением по умолчанию
     */
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Установка значения
     */
    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    // Специализированные геттеры

    public int getBoardRows() {
        return getInt("board.rows", 9);
    }

    public int getBoardCols() {
        return getInt("board.cols", 9);
    }

    public int getTotalMines() {
        return getInt("board.mines", 10);
    }

    public int getAnimationDelay() {
        return getInt("ui.animation.delay", 500);
    }

    public void setAnimationDelay(int delay) {
        set("ui.animation.delay", String.valueOf(delay));
    }

    public boolean showProbabilities() {
        return getBoolean("ui.show.probabilities", true);
    }

    public void setShowProbabilities(boolean show) {
        set("ui.show.probabilities", String.valueOf(show));
    }

    public String getLogLevel() {
        return get("log.level", "INFO");
    }

    public void setLogLevel(String level) {
        set("log.level", level);
    }

    public String getLogFile() {
        return get("log.file", "minesweeper.log");
    }

    public String getTemplateDirectory() {
        return get("vision.template.directory", "templates");
    }

    public double getSimilarityThreshold() {
        return getDouble("vision.similarity.threshold", 0.85);
    }

    public int getCellSize() {
        return getInt("vision.cell.size", 24);
    }

    public int getClickDelay() {
        return getInt("execution.click.delay", 100);
    }

    public boolean isDebugMode() {
        return getBoolean("execution.debug.mode", false);
    }

    public void setDebugMode(boolean debug) {
        set("execution.debug.mode", String.valueOf(debug));
    }

    public int getMaxSolutions() {
        return getInt("bot.max.solutions", 10000);
    }

    public boolean useAdvancedBot() {
        return getBoolean("bot.advanced.enabled", true);
    }
}