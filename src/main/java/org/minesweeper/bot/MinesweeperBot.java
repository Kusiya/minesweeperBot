package org.minesweeper.bot;

import org.minesweeper.core.Cell;
import org.minesweeper.core.Move;
import java.util.*;

public class MinesweeperBot {
    public int rows;
    public int cols;
    public int totalMines;
    public Cell[][] board;
    protected boolean gameActive;
    protected Random random;

    public MinesweeperBot(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.board = new Cell[rows][cols];
        this.gameActive = true;
        this.random = new Random();
        initializeBoard();
    }

    protected void initializeBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Cell(i, j);
            }
        }
    }

    public void placeMines(int firstRow, int firstCol) {
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            // Не размещаем мину на первой открытой клетке и вокруг нее
            if (Math.abs(row - firstRow) <= 1 && Math.abs(col - firstCol) <= 1) {
                continue;
            }

            if (!board[row][col].isMine()) {
                board[row][col].setMine(true);
                minesPlaced++;
            }
        }

        calculateNumbers();
    }

    protected void calculateNumbers() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    board[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    protected int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && board[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    protected boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public Move makeMove() {
        if (!gameActive) {
            return null;
        }

        // Сначала пробуем найти гарантированные ходы
        Move guaranteedMove = findGuaranteedMove();
        if (guaranteedMove != null) {
            return guaranteedMove;
        }

        // Если нет гарантированных ходов, используем вероятностный подход
        return findProbabilisticMove();
    }

    protected Move findGuaranteedMove() {
        // Ищем клетки, которые можно безопасно открыть
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isRevealed() && board[i][j].getAdjacentMines() > 0) {
                    Set<Cell> unrevealedNeighbors = getUnrevealedNeighbors(i, j);
                    Set<Cell> flaggedNeighbors = getFlaggedNeighbors(i, j);

                    int minesNeeded = board[i][j].getAdjacentMines() - flaggedNeighbors.size();

                    // Если количество неоткрытых соседей равно количеству нужных мин
                    if (unrevealedNeighbors.size() == minesNeeded && minesNeeded > 0) {
                        // Все неоткрытые соседи - мины
                        for (Cell cell : unrevealedNeighbors) {
                            if (!cell.isFlagged()) {
                                return new Move(cell.getRow(), cell.getCol(), true);
                            }
                        }
                    }

                    // Если все мины уже отмечены, остальные можно открыть
                    if (minesNeeded == 0 && !unrevealedNeighbors.isEmpty()) {
                        for (Cell cell : unrevealedNeighbors) {
                            if (!cell.isFlagged()) {
                                return new Move(cell.getRow(), cell.getCol(), false);
                            }
                        }
                    }
                }
            }
        }

        // Если первый ход, открываем центральную клетку
        if (getRevealedCount() == 0) {
            return new Move(rows / 2, cols / 2, false);
        }

        return null;
    }

    protected Move findProbabilisticMove() {
        List<Cell> unrevealedCells = new ArrayList<>();

        // Собираем все неоткрытые клетки
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed() && !board[i][j].isFlagged()) {
                    unrevealedCells.add(board[i][j]);
                }
            }
        }

        // Если остались только неоткрытые клетки
        if (unrevealedCells.size() == totalMines - getFlaggedCount()) {
            // Все оставшиеся клетки - мины, ставим флаги
            for (Cell cell : unrevealedCells) {
                if (!cell.isFlagged()) {
                    return new Move(cell.getRow(), cell.getCol(), true);
                }
            }
        }

        // Если есть информация от соседей, используем её
        Map<Cell, Double> probabilities = new HashMap<>();
        for (Cell cell : unrevealedCells) {
            double probability = calculateBasicProbability(cell);
            probabilities.put(cell, probability);
        }

        // Находим клетку с наименьшей вероятностью мины
        Cell safestCell = null;
        double minProb = 1.0;

        for (Map.Entry<Cell, Double> entry : probabilities.entrySet()) {
            if (entry.getValue() < minProb) {
                minProb = entry.getValue();
                safestCell = entry.getKey();
            }
        }

        if (safestCell != null) {
            return new Move(safestCell.getRow(), safestCell.getCol(), minProb > 0.5);
        }

        // Если ничего не нашли, открываем случайную клетку
        Cell randomCell = unrevealedCells.get(random.nextInt(unrevealedCells.size()));
        return new Move(randomCell.getRow(), randomCell.getCol(), false);
    }

    protected double calculateBasicProbability(Cell cell) {
        List<Cell> adjacentRevealed = getAdjacentRevealedCells(cell);
        if (adjacentRevealed.isEmpty()) {
            // Если нет информации, используем общую вероятность
            int unrevealedCount = getUnrevealedCount();
            int remainingMines = totalMines - getFlaggedCount();
            return (double) remainingMines / unrevealedCount;
        }

        double totalProbability = 0;
        int count = 0;

        for (Cell revealed : adjacentRevealed) {
            Set<Cell> unrevealedNeighbors = getUnrevealedNeighbors(revealed.getRow(), revealed.getCol());
            Set<Cell> flaggedNeighbors = getFlaggedNeighbors(revealed.getRow(), revealed.getCol());

            if (unrevealedNeighbors.contains(cell)) {
                int minesNeeded = revealed.getAdjacentMines() - flaggedNeighbors.size();
                if (minesNeeded > 0) {
                    totalProbability += (double) minesNeeded / unrevealedNeighbors.size();
                    count++;
                }
            }
        }

        return count > 0 ? totalProbability / count : 0.5;
    }

    public boolean processMove(Move move) {
        if (!gameActive) {
            return false;
        }

        Cell cell = board[move.getRow()][move.getCol()];

        if (move.isFlag()) {
            if (!cell.isRevealed() && !cell.isFlagged()) {
                cell.setFlagged(true);
                return true;
            }
            return false;
        } else {
            if (cell.isFlagged()) {
                return false;
            }

            if (cell.isMine()) {
                gameActive = false;
                return false;
            }

            revealCell(cell.getRow(), cell.getCol());
            return true;
        }
    }

    protected void revealCell(int row, int col) {
        if (!isValidCell(row, col) || board[row][col].isRevealed() || board[row][col].isFlagged()) {
            return;
        }

        board[row][col].setRevealed(true);

        if (board[row][col].getAdjacentMines() == 0) {
            // Рекурсивно открываем соседние клетки
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        revealCell(row + i, col + j);
                    }
                }
            }
        }
    }

    protected Set<Cell> getUnrevealedNeighbors(int row, int col) {
        Set<Cell> neighbors = new HashSet<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && !board[newRow][newCol].isRevealed()) {
                    neighbors.add(board[newRow][newCol]);
                }
            }
        }
        return neighbors;
    }

    protected Set<Cell> getFlaggedNeighbors(int row, int col) {
        Set<Cell> neighbors = new HashSet<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && board[newRow][newCol].isFlagged()) {
                    neighbors.add(board[newRow][newCol]);
                }
            }
        }
        return neighbors;
    }

    protected List<Cell> getAdjacentRevealedCells(Cell cell) {
        List<Cell> revealed = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = cell.getRow() + i;
                int newCol = cell.getCol() + j;
                if (isValidCell(newRow, newCol) && board[newRow][newCol].isRevealed()) {
                    revealed.add(board[newRow][newCol]);
                }
            }
        }
        return revealed;
    }

    protected int getRevealedCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getFlaggedCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j].isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    protected int getUnrevealedCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isRevealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public boolean isGameWon() {
        int revealedCount = getRevealedCount();
        return revealedCount == rows * cols - totalMines;
    }

    public void updateCellFromVision(int row, int col, boolean isRevealed, boolean isFlagged, int number) {
        if (isValidCell(row, col)) {
            board[row][col].setRevealed(isRevealed);
            board[row][col].setFlagged(isFlagged);
            board[row][col].setAdjacentMines(number);
        }
    }

    public void printBoard() {
        System.out.println("\nТекущее состояние доски:");
        System.out.print("   ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("%2d ", j);
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.printf("%2d ", i);
            for (int j = 0; j < cols; j++) {
                Cell cell = board[i][j];
                if (cell.isFlagged()) {
                    System.out.print(" F ");
                } else if (!cell.isRevealed()) {
                    System.out.print(" ? ");
                } else if (cell.isMine()) {
                    System.out.print(" * ");
                } else {
                    int num = cell.getAdjacentMines();
                    System.out.print(num == 0 ? " . " : " " + num + " ");
                }
            }
            System.out.println();
        }
    }
}