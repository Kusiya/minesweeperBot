package org.minesweeper.utils;

import java.io.*;
import java.util.Properties;

public class Config {
    private static Config instance;
    private Properties props;
    private String configFile = "minesweeper.properties";

    private Config() {
        props = new Properties();
        load();
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private void load() {
        try (InputStream input = new FileInputStream(configFile)) {
            props.load(input);
            System.out.println("✅ Конфигурация загружена из " + configFile);
        } catch (IOException e) {
            System.out.println("⚠️ Файл конфигурации не найден, создаем новый");
            setDefaults();
        }
    }

    public void save() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            props.store(output, "Minesweeper Bot Configuration");
            System.out.println("✅ Конфигурация сохранена в " + configFile);
        } catch (IOException e) {
            System.err.println("❌ Ошибка сохранения конфигурации: " + e.getMessage());
        }
    }

    private void setDefaults() {
        props.setProperty("offsetX", "0");
        props.setProperty("offsetY", "0");
        props.setProperty("cellSize", "30");
        props.setProperty("rows", "9");
        props.setProperty("cols", "9");
        props.setProperty("mines", "10");
        props.setProperty("delay", "2000");
        props.setProperty("debug", "true");
    }

    public int getInt(String key) {
        return Integer.parseInt(props.getProperty(key, "0"));
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getString(String key) {
        return props.getProperty(key, "");
    }

    public void setInt(String key, int value) {
        props.setProperty(key, String.valueOf(value));
    }

    public void setString(String key, String value) {
        props.setProperty(key, value);
    }

    public void printConfig() {
        System.out.println("\n📋 ТЕКУЩАЯ КОНФИГУРАЦИЯ:");
        System.out.println("   offsetX = " + getInt("offsetX"));
        System.out.println("   offsetY = " + getInt("offsetY"));
        System.out.println("   cellSize = " + getInt("cellSize"));
        System.out.println("   rows = " + getInt("rows"));
        System.out.println("   cols = " + getInt("cols"));
        System.out.println("   mines = " + getInt("mines"));
        System.out.println("   delay = " + getInt("delay"));
        System.out.println("   debug = " + getString("debug"));
    }
}