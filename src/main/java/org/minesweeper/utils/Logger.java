package org.minesweeper.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static Logger instance;
    private PrintWriter fileWriter;
    private LogLevel currentLevel;
    private boolean consoleOutput;
    private String logFile;
    private SimpleDateFormat dateFormat;

    public enum LogLevel {
        DEBUG(0), INFO(1), WARNING(2), ERROR(3);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public static LogLevel fromString(String level) {
            try {
                return valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                return INFO;
            }
        }
    }

    private Logger() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        this.currentLevel = LogLevel.INFO;
        this.consoleOutput = true;
        this.logFile = "minesweeper.log";
        initFileWriter();
    }

    private void initFileWriter() {
        try {
            fileWriter = new PrintWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            System.err.println("Не удалось создать файл лога: " + e.getMessage());
            fileWriter = null;
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void updateFromConfig(Config config) {
        if (config != null) {
            this.currentLevel = LogLevel.fromString(config.getLogLevel());
            this.consoleOutput = config.getBoolean("log.console", true);
            this.logFile = config.getLogFile();

            if (fileWriter != null) {
                fileWriter.close();
            }
            initFileWriter();
        }
    }

    /**
     * Логирование статистики игры
     * @param won true если игра выиграна, false если проиграна
     * @param moves количество сделанных ходов
     * @param timeMs время игры в миллисекундах
     */
    public void logGameStats(boolean won, int moves, long timeMs) {
        if (currentLevel.getLevel() > LogLevel.INFO.getLevel()) {
            return; // Не логируем, если уровень ниже INFO
        }

        String result = won ? "ПОБЕДА" : "ПОРАЖЕНИЕ";

        // Форматируем время
        String timeFormatted = formatTime(timeMs);

        // Создаем сообщение
        String message = String.format("🎮 Игра завершена: %s | Ходов: %d | Время: %s",
                result, moves, timeFormatted);

        // Добавляем эмодзи в зависимости от результата
        if (won) {
            message = "🏆 " + message;
        } else {
            message = "💥 " + message;
        }

        // Логируем
        log("STATS", message);

        // Дополнительно сохраняем в отдельный файл статистики, если нужно
        logToStatsFile(won, moves, timeMs);
    }

    /**
     * Логирование статистики игры с дополнительной информацией
     * @param won true если игра выиграна
     * @param moves количество ходов
     * @param timeMs время в миллисекундах
     * @param difficulty уровень сложности
     * @param botType тип бота
     */
    public void logGameStats(boolean won, int moves, long timeMs, String difficulty, String botType) {
        if (currentLevel.getLevel() > LogLevel.INFO.getLevel()) {
            return;
        }

        String result = won ? "ПОБЕДА" : "ПОРАЖЕНИЕ";
        String timeFormatted = formatTime(timeMs);

        String message = String.format("🎮 [%s] [%s] Игра завершена: %s | Ходов: %d | Время: %s",
                difficulty, botType, result, moves, timeFormatted);

        if (won) {
            message = "🏆 " + message;
        } else {
            message = "💥 " + message;
        }

        log("STATS", message);
        logToStatsFile(won, moves, timeMs, difficulty, botType);
    }

    /**
     * Форматирование времени
     */
    private String formatTime(long timeMs) {
        long hours = timeMs / 3600000;
        long minutes = (timeMs % 3600000) / 60000;
        long seconds = (timeMs % 60000) / 1000;
        long millis = timeMs % 1000;

        if (hours > 0) {
            return String.format("%dч %dм %dс", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds);
        } else if (seconds > 0) {
            return String.format("%dс %dмс", seconds, millis);
        } else {
            return String.format("%dмс", millis);
        }
    }

    /**
     * Логирование в отдельный файл статистики
     */
    private void logToStatsFile(boolean won, int moves, long timeMs) {
        logToStatsFile(won, moves, timeMs, "unknown", "unknown");
    }

    /**
     * Логирование в отдельный файл статистики с дополнительной информацией
     */
    private void logToStatsFile(boolean won, int moves, long timeMs, String difficulty, String botType) {
        String statsFile = "stats.csv";
        boolean fileExists = new File(statsFile).exists();

        try (PrintWriter statsWriter = new PrintWriter(new FileWriter(statsFile, true))) {
            // Если файл не существовал, пишем заголовок
            if (!fileExists) {
                statsWriter.println("timestamp,result,moves,time_ms,difficulty,bot_type");
            }

            String timestamp = dateFormat.format(new Date());
            String result = won ? "win" : "loss";

            statsWriter.printf("%s,%s,%d,%d,%s,%s%n",
                    timestamp, result, moves, timeMs, difficulty, botType);

        } catch (IOException e) {
            // Не используем logger здесь, чтобы избежать рекурсии
            System.err.println("Не удалось записать статистику в файл: " + e.getMessage());
        }
    }

    /**
     * Логирование серии игр
     * @param games сыграно игр
     * @param won выиграно игр
     * @param totalTime общее время
     */
    public void logGameSeriesStats(int games, int won, long totalTime) {
        if (currentLevel.getLevel() > LogLevel.INFO.getLevel()) {
            return;
        }

        double winRate = (won * 100.0) / games;
        String timeFormatted = formatTime(totalTime);
        double avgTime = (double) totalTime / games;

        String message = String.format(
                "📊 СЕРИЯ ИГР: %d игр, %d побед (%.1f%%), общее время: %s, среднее время: %.0f мс",
                games, won, winRate, timeFormatted, avgTime);

        log("STATS", message);
    }

    /**
     * Логирование рекорда
     */
    public void logRecord(String recordType, Object value) {
        if (currentLevel.getLevel() > LogLevel.INFO.getLevel()) {
            return;
        }

        String message = String.format("🏆 НОВЫЙ РЕКОРД! %s: %s", recordType, value);
        log("RECORD", message);

        // Сохраняем рекорд в отдельный файл
        try (PrintWriter recordWriter = new PrintWriter(new FileWriter("records.txt", true))) {
            String timestamp = dateFormat.format(new Date());
            recordWriter.printf("[%s] %s: %s%n", timestamp, recordType, value);
        } catch (IOException e) {
            System.err.println("Не удалось записать рекорд: " + e.getMessage());
        }
    }

    public void debug(String message) {
        if (currentLevel.getLevel() <= LogLevel.DEBUG.getLevel()) {
            log("DEBUG", message);
        }
    }

    public void info(String message) {
        if (currentLevel.getLevel() <= LogLevel.INFO.getLevel()) {
            log("INFO", message);
        }
    }

    public void warning(String message) {
        if (currentLevel.getLevel() <= LogLevel.WARNING.getLevel()) {
            log("WARNING", message);
        }
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void error(String message, Exception e) {
        log("ERROR", message + ": " + e.getMessage());
        if (currentLevel.getLevel() <= LogLevel.DEBUG.getLevel()) {
            e.printStackTrace();
            if (fileWriter != null) {
                e.printStackTrace(fileWriter);
                fileWriter.flush();
            }
        }
    }

    private void log(String level, String message) {
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] %s: %s", timestamp, level, message);

        if (consoleOutput) {
            System.out.println(logMessage);
        }

        if (fileWriter != null) {
            fileWriter.println(logMessage);
            fileWriter.flush();
        }
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}