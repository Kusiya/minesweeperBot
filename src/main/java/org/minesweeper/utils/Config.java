package org.minesweeper.utils;

import java.io.*;
import java.util.Properties;

/**
 * Загрузка и сохранение настроек
 */
public class Config {
    private Properties properties;
    private String configFile;

    public Config(String configFile) {
        this.configFile = configFile;
        this.properties = new Properties();
        load();
    }

    /**
     * Загрузить настройки из файла
     */
    private void load() {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            Logger.info("Конфигурация загружена из " + configFile);
        } catch (IOException e) {
            Logger.warn("Не удалось загрузить конфигурацию: " + e.getMessage());
            setDefaults();
        }
    }

    /**
     * Сохранить настройки в файл
     */
    public void save() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Minesweeper Bot Configuration");
            Logger.info("Конфигурация сохранена в " + configFile);
        } catch (IOException e) {
            Logger.error("Не удалось сохранить конфигурацию: " + e.getMessage());
        }
    }

    /**
     * Установить значения по умолчанию
     */
    private void setDefaults() {
        properties.setProperty("offsetX", "100");
        properties.setProperty("offsetY", "100");
        properties.setProperty("cellSize", "30");
        properties.setProperty("delay", "200");
        properties.setProperty("rows", "9");
        properties.setProperty("cols", "9");
        properties.setProperty("mines", "10");
        properties.setProperty("strategy", "advanced");
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void setInt(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public void setString(String key, String value) {
        properties.setProperty(key, value);
    }
}
