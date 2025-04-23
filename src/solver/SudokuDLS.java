package solver;
import java.util.*;

public class SudokuDLS {

    private final SudokuGraph graph = new SudokuGraph();
    private final List<int[][]> solutions = new ArrayList<>();
    private final SudokuGUI gui;
    private int[][] originalBoard;
    private int maxDepth;

    //Constructor that takes a GUI instance
    public SudokuDLS(SudokuGUI gui) {
        this.gui = gui;
    }

    public List<int[][]> solve(int[][] board, int maxDepth) {
        this.maxDepth = maxDepth;
        this.originalBoard = deepCopy(board);
        solutions.clear();
        dfs(board, 0);
        return solutions;
    }

    private void dfs(int[][] board, int depth) {
        if (depth > maxDepth) return;

        int[] emptyCell = findEmpty(board);
        if (emptyCell == null) {
            solutions.add(deepCopy(board));
            return;
        }
        if (!solutions.isEmpty()) return;

        int row = emptyCell[0];
        int col = emptyCell[1];
        int index = row * 9 + col;

        for (int num = 1; num <= 9; num++) {
            if (isValid(board, index, num)) {
                board[row][col] = num;

                //GUI animation block
                if (gui != null) {
                    for (int r = 0; r < 9; r++) {
                        for (int c = 0; c < 9; c++) {
                            int val = board[r][c];
                            boolean isFixed = originalBoard[r][c] != 0;
                            gui.updateCell(r, c, val, isFixed);
                        }
                    }
                    try {
                        Thread.sleep(100); //adjust speed
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                dfs(board, depth + 1); //recursive call
                if (!solutions.isEmpty()) return; //early exit after solution found
                board[row][col] = 0;   //backtrack
            }
        }
    }

    private int[] findEmpty(int[][] board) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (board[r][c] == 0)
                    return new int[]{r, c};
        return null;
    }

    private boolean isValid(int[][] board, int index, int num) {
        int row = index / 9;
        int col = index % 9;
        for (int neighbor : graph.getNeighbors(index)) {
            int r = neighbor / 9;
            int c = neighbor % 9;
            if (board[r][c] == num) return false;
        }
        return true;
    }

    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++)
            copy[i] = Arrays.copyOf(original[i], 9);
        return copy;
    }
}