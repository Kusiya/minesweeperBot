package org.minesweeper.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BotStatistics {
    private AtomicInteger gamesPlayed;
    private AtomicInteger gamesWon;
    private List<Integer> movesPerGame;
    private List<Long> timePerGame;
    private Map<String, Integer> strategyUsage;
    private Map<String, GameStats> difficultyStats;
    private Logger logger;

    public BotStatistics() {
        this.gamesPlayed = new AtomicInteger(0);
        this.gamesWon = new AtomicInteger(0);
        this.movesPerGame = Collections.synchronizedList(new ArrayList<>());
        this.timePerGame = Collections.synchronizedList(new ArrayList<>());
        this.strategyUsage = new ConcurrentHashMap<>();
        this.difficultyStats = new ConcurrentHashMap<>();
        this.logger = Logger.getInstance();

        difficultyStats.put("novice", new GameStats());
        difficultyStats.put("intermediate", new GameStats());
        difficultyStats.put("expert", new GameStats());
    }

    /**
     * Запись результатов игры
     */
    public void recordGame(boolean won, int moves, long timeMs, String botType) {
        gamesPlayed.incrementAndGet();
        if (won) gamesWon.incrementAndGet();

        movesPerGame.add(moves);
        timePerGame.add(timeMs);

        // Используем новый метод логирования
        logger.logGameStats(won, moves, timeMs);
    }

    /**
     * Запись игры с указанием сложности
     */
    public void recordGame(String difficulty, boolean won, int moves, long timeMs, String botType) {
        recordGame(won, moves, timeMs, botType);

        GameStats stats = difficultyStats.get(difficulty);
        if (stats != null) {
            stats.addGame(won, moves, timeMs);
        }

        // Логируем с дополнительной информацией
        logger.logGameStats(won, moves, timeMs, difficulty, botType);

        // Проверяем рекорды
        checkRecords(difficulty, won, moves, timeMs);
    }

    /**
     * Проверка и запись рекордов
     */
    private void checkRecords(String difficulty, boolean won, int moves, long timeMs) {
        if (!won) return;

        GameStats stats = difficultyStats.get(difficulty);
        if (stats == null) return;

        // Проверка рекорда по минимальному количеству ходов
        Integer minMovesRecord = getMinMovesRecord(difficulty);
        if (minMovesRecord == null || moves < minMovesRecord) {
            logger.logRecord("Минимальное количество ходов (" + difficulty + ")", moves);
            saveMinMovesRecord(difficulty, moves);
        }

        // Проверка рекорда по минимальному времени
        Long minTimeRecord = getMinTimeRecord(difficulty);
        if (minTimeRecord == null || timeMs < minTimeRecord) {
            logger.logRecord("Минимальное время (" + difficulty + ")", formatTime(timeMs));
            saveMinTimeRecord(difficulty, timeMs);
        }
    }

    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long millis = timeMs % 1000;
        return String.format("%d.%03d с", seconds, millis);
    }

    // Методы для работы с рекордами (можно реализовать сохранение в файл)
    private Integer getMinMovesRecord(String difficulty) {
        // Заглушка - в реальности читать из файла
        return null;
    }

    private void saveMinMovesRecord(String difficulty, int moves) {
        // Заглушка - сохранять в файл
    }

    private Long getMinTimeRecord(String difficulty) {
        // Заглушка
        return null;
    }

    private void saveMinTimeRecord(String difficulty, long timeMs) {
        // Заглушка
    }

    /**
     * Запись использования стратегии
     */
    public void recordStrategy(String strategyName) {
        strategyUsage.merge(strategyName, 1, Integer::sum);
    }

    /**
     * Вывод статистики
     */
    public void printStatistics() {
        int played = gamesPlayed.get();
        int won = gamesWon.get();

        System.out.println("\n📊 ОБЩАЯ СТАТИСТИКА:");
        System.out.printf("  Всего игр: %d\n", played);
        System.out.printf("  Побед: %d\n", won);

        if (played > 0) {
            double winRate = (won * 100.0) / played;
            System.out.printf("  Процент побед: %.2f%%\n", winRate);

            // Логируем статистику серии
            long totalTime = timePerGame.stream().mapToLong(Long::longValue).sum();
            logger.logGameSeriesStats(played, won, totalTime);
        }

        // ... остальной код вывода статистики ...
    }

    /**
     * Внутренний класс для статистики по уровню сложности
     */
    public static class GameStats {
        private int gamesPlayed;
        private int gamesWon;
        private List<Integer> moves;
        private List<Long> times;

        public GameStats() {
            this.moves = new ArrayList<>();
            this.times = new ArrayList<>();
        }

        public void addGame(boolean won, int moveCount, long timeMs) {
            gamesPlayed++;
            if (won) gamesWon++;
            moves.add(moveCount);
            times.add(timeMs);
        }

        public int getGamesPlayed() { return gamesPlayed; }
        public int getGamesWon() { return gamesWon; }

        public double getWinRate() {
            return gamesPlayed > 0 ? (gamesWon * 100.0) / gamesPlayed : 0;
        }

        public double getAverageMoves() {
            return moves.stream().mapToInt(Integer::intValue).average().orElse(0);
        }

        public double getAverageTime() {
            return times.stream().mapToLong(Long::longValue).average().orElse(0);
        }

        public void printStats(int indent) {
            String prefix = "  ".repeat(indent);
            System.out.printf(prefix + "Игр: %d\n", gamesPlayed);
            System.out.printf(prefix + "Побед: %d (%.2f%%)\n", gamesWon, getWinRate());
            System.out.printf(prefix + "Среднее ходов: %.2f\n", getAverageMoves());
            System.out.printf(prefix + "Среднее время: %.2f мс\n", getAverageTime());
        }

        public void reset() {
            gamesPlayed = 0;
            gamesWon = 0;
            moves.clear();
            times.clear();
        }
    }
}