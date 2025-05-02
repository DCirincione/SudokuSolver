package solver;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HybridSolver {
    private final CubeSudokuBoard board;
    private final CubeCanvas canvas;
    private final int bfsDepth;
    private final SudokuBFS bfsSolver;
    private final SudokuDLS dlsSolver;

    public HybridSolver(CubeSudokuBoard board, CubeCanvas canvas, int bfsDepth) {
        this.board = board;
        this.canvas = canvas;
        this.bfsDepth = bfsDepth;
        this.bfsSolver = new SudokuBFS();
        this.dlsSolver = new SudokuDLS(canvas);
    }

    public void solve() {
        System.out.println("Hybrid: Running adaptive BFS...");
        List<CubeSudokuBoard> frontier = bfsSolver.solveAdaptive(
            board.deepCopy(), canvas, null
        );

        if (frontier == null || frontier.isEmpty()) {
            System.out.println("Hybrid: No frontier generated. Aborting hybrid search.");
            return;
        }
        System.out.println("Hybrid: BFS generated " + frontier.size() + " partial boards.");

        // Sort frontier boards by most filled (descending)
        frontier.sort((a, b) -> Integer.compare(countFilled(b), countFilled(a)));

        // Pick the best board (most filled) and run DLS on it
        CubeSudokuBoard best = frontier.get(0);
        // Animate step-by-step application of best board to actual board
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    int from = best.getCell(f, r, c);
                    int to = board.getCell(f, r, c);
                    if (to == 0 && from != 0) {
                        board.setCell(f, r, c, from);
                        if (canvas != null) {
                            canvas.repaint();
                        }
                        try {
                            Thread.sleep(45); //adjust time for printing of BFS in hybrid search
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        // Directly run DLS on the displayed board, so animation occurs in-place
        System.out.println("Hybrid: Starting DLS on top frontier board...");
        dlsSolver.solve(board, 500000); // internally animates on board
        System.out.println("Hybrid: BFS+DLS finished solving.");
        return;
    }

    private int countFilled(CubeSudokuBoard b) {
        int count = 0;
        for (int f = 0; f < 5; f++)
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (b.getCell(f, r, c) != 0) count++;
        return count;
    }

    private void copyBoard(CubeSudokuBoard src, CubeSudokuBoard dst) {
        for (int f = 0; f < 5; f++)
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    dst.setCell(f, r, c, src.getCell(f, r, c));
    }
}
