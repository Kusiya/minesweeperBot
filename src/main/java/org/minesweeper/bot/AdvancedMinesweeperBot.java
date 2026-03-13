package org.minesweeper.bot;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;
import org.minesweeper.strategy.Strategy;
import org.minesweeper.strategy.pattern.PatternDetector;
import org.minesweeper.utils.Logger;

import java.util.*;

public class AdvancedMinesweeperBot extends MinesweeperBot {
    private final Map<String, Double> probabilityCache;  // сделал final
    private PatternDetector patternDetector;
    private List<Strategy> strategies;
    private Logger logger;  // будет инициализирован в конструкторе
    private static final int MONTE_CARLO_SIMULATIONS = 1000;
    private static final int EXACT_SOLUTION_THRESHOLD = 12;

    public AdvancedMinesweeperBot(int rows, int cols, int totalMines) {
        super(rows, cols, totalMines);
        this.probabilityCache = new HashMap<>();
        this.patternDetector = new PatternDetector();
        this.strategies = new ArrayList<>();
        this.logger = Logger.getInstance();  // инициализация logger
        logger.info("Продвинутый бот инициализирован");
    }

    public void addStrategy(Strategy strategy) {
        if (strategy == null) {
            logger.warning("Попытка добавить null стратегию");
            return;
        }

        strategies.add(strategy);
        // Сортируем стратегии по приоритету (от высокого к низкому)
        strategies.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));

        logger.debug("Добавлена стратегия: " + strategy.getName() +
                " с приоритетом " + strategy.getPriority());
    }

    /**
     * Добавление нескольких стратегий
     */
    public void addStrategies(Strategy... strategies) {
        for (Strategy strategy : strategies) {
            addStrategy(strategy);
        }
    }

    /**
     * Удаление стратегии
     * @param strategyName имя стратегии для удаления
     * @return true если стратегия была удалена
     */
    public boolean removeStrategy(String strategyName) {
        Iterator<Strategy> iterator = strategies.iterator();
        while (iterator.hasNext()) {
            Strategy strategy = iterator.next();
            if (strategy.getName().equals(strategyName)) {
                iterator.remove();
                logger.debug("Удалена стратегия: " + strategyName);
                return true;
            }
        }
        logger.warning("Стратегия не найдена: " + strategyName);
        return false;
    }

    /**
     * Получение списка всех стратегий
     */
    public List<Strategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    /**
     * Очистка всех стратегий
     */
    public void clearStrategies() {
        strategies.clear();
        logger.debug("Все стратегии удалены");
    }

    /**
     * Установка списка стратегий
     */
    public void setStrategies(List<Strategy> newStrategies) {
        this.strategies = new ArrayList<>(newStrategies);
        strategies.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
        logger.debug("Установлено " + strategies.size() + " стратегий");
    }

    @Override
    public Move makeMove() {
        probabilityCache.clear();  // очищаем кэш при каждом ходе

        if (!gameActive) {
            return null;
        }

        if (isGameWon()) {
            return null;
        }

        logger.debug("Продвинутый бот ищет ход...");

        Move guaranteedMove = findGuaranteedMove();
        if (guaranteedMove != null) {
            guaranteedMove.setConfidence(1.0);
            guaranteedMove.setStrategy("Guaranteed");
            return guaranteedMove;
        }

        Move patternMove = patternDetector.detectPattern(board);
        if (patternMove != null) {
            patternMove.setConfidence(0.95);
            patternMove.setStrategy(patternMove.getStrategy());
            logger.info("Найден паттерн: " + patternMove.getStrategy());
            return patternMove;
        }

        Move logicalMove = findLogicalMove();
        if (logicalMove != null) {
            logicalMove.setConfidence(0.8);
            logicalMove.setStrategy("Logical");
            return logicalMove;
        }

        logger.debug("Применяем вероятностный анализ");
        return findAdvancedProbabilisticMove();
    }

    private Move findLogicalMove() {
        int unrevealedCount = getUnrevealedCount();
        int remainingMines = totalMines - getFlaggedCount();

        if (unrevealedCount == remainingMines) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                        logger.debug("Все оставшиеся клетки - мины");
                        return new Move(i, j, true);
                    }
                }
            }
        }

        if (remainingMines == 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                        logger.debug("Мин больше нет, открываем безопасную клетку");
                        return new Move(i, j, false);
                    }
                }
            }
        }

        List<Cell> boundaryCells = findBoundaryCells();
        if (!boundaryCells.isEmpty() && boundaryCells.size() < unrevealedCount / 2) {
            Cell randomBoundary = boundaryCells.get(random.nextInt(boundaryCells.size()));
            logger.debug("Выбрана граничная клетка");
            return new Move(randomBoundary.getRow(), randomBoundary.getCol(), false);
        }

        return null;
    }

    private List<Cell> findBoundaryCells() {
        List<Cell> boundary = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            int ni = i + di;
                            int nj = j + dj;
                            if (isValidCell(ni, nj) && board[ni][nj].isRevealed()) {
                                boundary.add(board[i][j]);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return boundary;
    }

    private Move findAdvancedProbabilisticMove() {
        List<Cell> unrevealedCells = getAllUnrevealedCells();

        if (unrevealedCells.isEmpty()) {
            return null;
        }

        if (unrevealedCells.size() <= EXACT_SOLUTION_THRESHOLD) {
            logger.debug("Мало клеток, применяем точное решение");
            return findExactSolution(unrevealedCells);
        }

        logger.debug("Много клеток, применяем Monte Carlo");
        return findMonteCarloMove(unrevealedCells);
    }

    private Move findExactSolution(List<Cell> unrevealedCells) {
        List<Set<Cell>> possibleSolutions = generatePossibleSolutions(unrevealedCells);

        if (possibleSolutions.isEmpty()) {
            logger.warning("Не найдено решений, используем базовую вероятность");
            return findProbabilisticMove();
        }

        logger.debug("Найдено " + possibleSolutions.size() + " возможных решений");

        Map<Cell, Integer> mineCount = new HashMap<>();
        for (Cell cell : unrevealedCells) {
            mineCount.put(cell, 0);
        }

        for (Set<Cell> solution : possibleSolutions) {
            for (Cell cell : solution) {
                mineCount.put(cell, mineCount.get(cell) + 1);
            }
        }

        Cell safest = null;
        double minProbability = 1.0;
        Cell mostLikelyMine = null;
        double maxProbability = 0.0;

        for (Cell cell : unrevealedCells) {
            double prob = (double) mineCount.get(cell) / possibleSolutions.size();

            if (prob < minProbability) {
                minProbability = prob;
                safest = cell;
            }

            if (prob > maxProbability && prob > 0.7) {
                maxProbability = prob;
                mostLikelyMine = cell;
            }
        }

        if (mostLikelyMine != null && maxProbability > 0.7) {
            logger.debug("Клетка с высокой вероятностью мины: " + maxProbability);
            Move move = new Move(mostLikelyMine.getRow(), mostLikelyMine.getCol(), true);
            move.setConfidence(maxProbability);
            return move;
        }

        if (safest != null) {
            logger.debug("safest клетка с вероятностью мины " + minProbability);
            Move move = new Move(safest.getRow(), safest.getCol(), false);
            move.setConfidence(1.0 - minProbability);
            return move;
        }

        return findProbabilisticMove();
    }

    private List<Set<Cell>> generatePossibleSolutions(List<Cell> unrevealedCells) {
        List<Set<Cell>> solutions = new ArrayList<>();
        int remainingMines = totalMines - getFlaggedCount();

        List<Set<Cell>> components = findConnectedComponents(unrevealedCells);
        List<List<Set<Cell>>> componentSolutions = new ArrayList<>();

        for (Set<Cell> component : components) {
            List<Cell> componentList = new ArrayList<>(component);
            List<Set<Cell>> compSolutions = new ArrayList<>();
            generateCombinationsForComponent(componentList, 0, new HashSet<>(),
                    Math.min(remainingMines, component.size()), compSolutions);
            componentSolutions.add(compSolutions);
        }

        combineComponentSolutions(componentSolutions, 0, new HashSet<>(), solutions);

        return solutions;
    }

    private List<Set<Cell>> findConnectedComponents(List<Cell> cells) {
        List<Set<Cell>> components = new ArrayList<>();
        Set<Cell> visited = new HashSet<>();

        for (Cell cell : cells) {
            if (!visited.contains(cell)) {
                Set<Cell> component = new HashSet<>();
                dfsComponent(cell, cells, visited, component);
                if (!component.isEmpty()) {
                    components.add(component);
                }
            }
        }

        return components;
    }

    private void dfsComponent(Cell cell, List<Cell> allCells, Set<Cell> visited, Set<Cell> component) {
        visited.add(cell);
        component.add(cell);

        for (Cell neighbor : allCells) {
            if (!visited.contains(neighbor) && areAdjacent(cell, neighbor)) {
                dfsComponent(neighbor, allCells, visited, component);
            }
        }
    }

    private boolean areAdjacent(Cell c1, Cell c2) {
        return Math.abs(c1.getRow() - c2.getRow()) <= 1 &&
                Math.abs(c1.getCol() - c2.getCol()) <= 1;
    }

    private void generateCombinationsForComponent(List<Cell> cells, int index,
                                                  Set<Cell> current, int maxMines, List<Set<Cell>> solutions) {
        if (index == cells.size()) {
            if (isConsistent(current)) {
                solutions.add(new HashSet<>(current));
            }
            return;
        }

        if (current.size() > maxMines) {
            return;
        }

        generateCombinationsForComponent(cells, index + 1, current, maxMines, solutions);

        current.add(cells.get(index));
        generateCombinationsForComponent(cells, index + 1, current, maxMines, solutions);
        current.remove(cells.get(index));
    }

    private void combineComponentSolutions(List<List<Set<Cell>>> componentSolutions,
                                           int index, Set<Cell> current, List<Set<Cell>> result) {
        if (index == componentSolutions.size()) {
            if (isConsistent(current)) {
                result.add(new HashSet<>(current));
            }
            return;
        }

        for (Set<Cell> solution : componentSolutions.get(index)) {
            current.addAll(solution);
            combineComponentSolutions(componentSolutions, index + 1, current, result);
            current.removeAll(solution);
        }
    }

    private boolean isConsistent(Set<Cell> mines) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isRevealed()) {
                    int expectedMines = board[i][j].getAdjacentMines();
                    int actualMines = 0;

                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            int ni = i + di;
                            int nj = j + dj;
                            if (isValidCell(ni, nj)) {
                                Cell neighbor = board[ni][nj];
                                if (mines.contains(neighbor) || neighbor.isFlagged()) {
                                    actualMines++;
                                }
                            }
                        }
                    }

                    if (actualMines != expectedMines) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Move findMonteCarloMove(List<Cell> unrevealedCells) {
        int simulations = MONTE_CARLO_SIMULATIONS;
        Map<Cell, Integer> mineHits = new HashMap<>();

        for (Cell cell : unrevealedCells) {
            mineHits.put(cell, 0);
        }

        int validSimulations = 0;

        for (int sim = 0; sim < simulations; sim++) {
            Set<Cell> simulatedMines = new HashSet<>(getFlaggedCellsFromBoard());
            List<Cell> available = new ArrayList<>(unrevealedCells);
            Collections.shuffle(available);

            int minesToPlace = totalMines - simulatedMines.size();
            for (int i = 0; i < minesToPlace && i < available.size(); i++) {
                simulatedMines.add(available.get(i));
            }

            if (isConsistent(simulatedMines)) {
                validSimulations++;
                for (Cell cell : unrevealedCells) {
                    if (simulatedMines.contains(cell)) {
                        mineHits.put(cell, mineHits.get(cell) + 1);
                    }
                }
            }
        }

        if (validSimulations == 0) {
            logger.warning("Monte Carlo не нашел валидных симуляций");
            return findProbabilisticMove();
        }

        logger.debug("Monte Carlo: " + validSimulations + " валидных симуляций");

        Cell safest = null;
        double minProbability = 1.0;
        Cell mostLikelyMine = null;
        double maxProbability = 0.0;

        for (Cell cell : unrevealedCells) {
            double prob = (double) mineHits.get(cell) / validSimulations;

            if (prob < minProbability) {
                minProbability = prob;
                safest = cell;
            }

            if (prob > maxProbability) {
                maxProbability = prob;
                mostLikelyMine = cell;
            }
        }

        if (mostLikelyMine != null && maxProbability > 0.8) {
            logger.debug("Monte Carlo: высокая вероятность мины " + maxProbability);
            Move move = new Move(mostLikelyMine.getRow(), mostLikelyMine.getCol(), true);
            move.setConfidence(maxProbability);
            return move;
        }

        if (safest != null) {
            logger.debug("Monte Carlo: safest клетка с вероятностью мины " + minProbability);
            Move move = new Move(safest.getRow(), safest.getCol(), false);
            move.setConfidence(1.0 - minProbability);
            return move;
        }

        return findProbabilisticMove();
    }

    /**
     * НОВЫЙ МЕТОД: получение клеток с флагами из текущей доски
     */
    private Set<Cell> getFlaggedCellsFromBoard() {
        Set<Cell> flagged = new HashSet<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isFlagged()) {
                    flagged.add(board[i][j]);
                }
            }
        }
        return flagged;
    }

    private List<Cell> getAllUnrevealedCells() {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                    cells.add(board[i][j]);
                }
            }
        }
        return cells;
    }
}