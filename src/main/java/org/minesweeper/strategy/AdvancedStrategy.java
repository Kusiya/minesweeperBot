package org.minesweeper.strategy;

import org.minesweeper.core.Cell;
import org.minesweeper.core.GameState;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.pattern.PatternDetector;
import org.minesweeper.strategy.pattern.OneTwoOnePattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Продвинутая стратегия, использующая паттерны и вероятностный анализ
 */
public class AdvancedStrategy implements Strategy {
    private final BasicStrategy basicStrategy;
    private final List<PatternDetector> patternDetectors;

    public AdvancedStrategy() {
        this.basicStrategy = new BasicStrategy();
        this.patternDetectors = new ArrayList<>();

        // Регистрируем все паттерны
        registerPatterns();
    }

    private void registerPatterns() {
        patternDetectors.add(new OneTwoOnePattern());
        // Здесь можно добавить другие паттерны
        // patternDetectors.add(new OneTwoTwoOnePattern());
        // patternDetectors.add(new EdgePattern());
    }

    @Override
    public String getName() {
        return "Продвинутая стратегия (паттерны + вероятность)";
    }

    @Override
    public Move nextMove(GameState state) {
        // 1. Сначала пробуем базовые правила
        Move basicMove = basicStrategy.nextMove(state);
        if (basicMove != null) {
            return basicMove;
        }

        // 2. Ищем паттерны
        Move patternMove = findPatternMove(state);
        if (patternMove != null) {
            return patternMove;
        }

        // 3. Если ничего не нашли - вероятностный анализ
        return probabilisticMove(state);
    }

    /**
     * Поиск хода по паттернам
     */
    private Move findPatternMove(GameState state) {
        for (int i = 0; i < state.getRows(); i++) {
            for (int j = 0; j < state.getCols(); j++) {
                for (PatternDetector detector : patternDetectors) {
                    Move move = detector.detectPattern(state, i, j);
                    if (move != null) {
                        return move;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Вероятностный анализ - выбираем клетку с наименьшей вероятностью мины
     */
    private Move probabilisticMove(GameState state) {
        List<Move> candidates = new ArrayList<>();

        for (int i = 0; i < state.getRows(); i++) {
            for (int j = 0; j < state.getCols(); j++) {
                Cell cell = state.getCell(i, j);
                if (!cell.isOpenable()) continue;

                double risk = estimateRisk(state, i, j);
                candidates.add(new Move(i, j, false, risk,
                        String.format("Вероятностный анализ (риск %.1f%%)", risk * 100)));
            }
        }

        // Сортируем по риску (от меньшего к большему)
        candidates.sort((m1, m2) -> Double.compare(m1.getRisk(), m2.getRisk()));

        return candidates.isEmpty() ? null : candidates.get(0);
    }

    /**
     * Оценить риск для клетки
     */
    private double estimateRisk(GameState state, int row, int col) {
        List<Cell> neighbors = state.getNeighbors(row, col);
        double totalRisk = 0;
        int validNeighbors = 0;

        for (Cell neighbor : neighbors) {
            if (neighbor.isRevealed() && neighbor.getAdjacentMines() > 0) {
                int number = neighbor.getAdjacentMines();
                int flags = state.countFlagsAround(neighbor.getRow(), neighbor.getCol());
                int unknown = state.countUnknownAround(neighbor.getRow(), neighbor.getCol());

                if (unknown > 0) {
                    double localRisk = (number - flags) / (double) unknown;
                    totalRisk += localRisk;
                    validNeighbors++;
                }
            }
        }

        return validNeighbors > 0 ? totalRisk / validNeighbors : 0.5;
    }
}
