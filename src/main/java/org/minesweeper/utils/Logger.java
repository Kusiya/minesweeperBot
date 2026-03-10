package org.minesweeper.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Простой логгер для вывода сообщений
 */
public class Logger {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void debug(String message) {
        log("DEBUG", message);
    }

    private static void log(String level, String message) {
        String time = LocalDateTime.now().format(formatter);
        System.out.println(time + " [" + level + "] " + message);
    }
}
