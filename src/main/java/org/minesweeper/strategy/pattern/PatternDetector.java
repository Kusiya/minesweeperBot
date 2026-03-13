package org.minesweeper.strategy.pattern;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;
import org.minesweeper.utils.Logger;

import java.util.*;

/**
 * Детектор и обработчик игровых паттернов.
 */
public class PatternDetector {
    private List<Pattern> patterns;
    private Logger logger;

    public PatternDetector() {
        this.logger = Logger.getInstance();
        this.patterns = new ArrayList<>();

        // Регистрируем все известные паттерны
        registerPattern(new OneTwoOnePattern());
        registerPattern(new OneTwoTwoOnePattern());
        registerPattern(new TwoTwoPattern());
        registerPattern(new OneOnePattern());
        registerPattern(new EdgePattern());

        logger.info("PatternDetector инициализирован с " + patterns.size() + " паттернами");
    }

    /**
     * Поиск любого известного паттерна на доске
     */
    public Move detectPattern(Cell[][] board) {
        for (Pattern pattern : patterns) {
            Move move = pattern.detect(board);
            if (move != null) {
                logger.debug("Найден паттерн: " + pattern.getName());
                return move;
            }
        }
        return null;
    }

    /**
     * Добавление нового паттерна
     */
    public void registerPattern(Pattern pattern) {
        if (pattern != null) {
            patterns.add(pattern);
            logger.debug("Зарегистрирован паттерн: " + pattern.getName());
        }
    }

    /**
     * Получение списка всех паттернов
     */
    public List<Pattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }
}
