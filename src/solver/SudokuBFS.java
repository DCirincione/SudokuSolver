package solver;
import java.util.*;

public class SudokuBFS {
    private final SudokuGraph graph = new SudokuGraph();
    private final List<int[][]> solutions = new ArrayList<>();
    private final SudokuGUI gui;

    //Place this constructor here, under the class declaration
    public SudokuBFS(SudokuGUI gui) {
        this.gui = gui;
        }

    public List<int[][]> solve(int[][] board) {
        Queue<int[][]> queue = new LinkedList<>();
        queue.add(deepCopy(board));

        while (!queue.isEmpty()) {
            int[][] current = queue.poll();

            int[] emptyCell = findEmpty(current);
            if (emptyCell == null) {
                //No empty cells = solution
                solutions.add(deepCopy(current));
                continue; //BFS can keep searching for more
            }

            int row = emptyCell[0];
            int col = emptyCell[1];
            int index = row * 9 + col;

            for (int num = 1; num <= 9; num++) {
                if (isValid(current, index, num)) {
                    int[][] next = deepCopy(current);
                    next[row][col] = num;
                    //GUI implementation
                    gui.updateCell(row, col, num, false);
                    try { Thread.sleep(30); } catch (InterruptedException ignored) {}
                    queue.add(next);
                }
            }
        }

        return solutions;
    }

    //Finds the first empty cell (0); returns [row, col] or null if full
    private int[] findEmpty(int[][] board) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (board[r][c] == 0)
                    return new int[]{r, c};
        return null;
    }

    //Uses graph to check if placing 'num' at 'index' is valid
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

    //Deep copy to avoid mutating the board
    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++)
            copy[i] = Arrays.copyOf(original[i], 9);
        return copy;
    }
}