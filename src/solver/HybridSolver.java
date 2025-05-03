package solver;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HybridSolver {
    //CubeSudoku puzzle board to be solved
    private final CubeSudokuBoard board;
    //Canvas object for rendering board updates (used for animation)
    private final CubeCanvas canvas;
    //BFS maximum search depth for partial solution generation
    private final int bfsDepth;
    //BFS solver for generating partially filled boards
    private final SudokuBFS bfsSolver;
    //DLS solver for completing board from a partial state
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
        //run adaptive BFS to generate list of partially completed boards
        List<CubeSudokuBoard> frontier = bfsSolver.solveAdaptive(
            board.deepCopy(), canvas, null
        );

        //abort if no useful partial solutions found
        if (frontier == null || frontier.isEmpty()) {
            System.out.println("Hybrid: No frontier generated. Aborting hybrid search.");
            return;
        }
        System.out.println("Hybrid: BFS generated " + frontier.size() + " partial boards.");

        //sort frontier by number of filled cells (descending order)
        frontier.sort((a, b) -> Integer.compare(countFilled(b), countFilled(a)));

        //animate transition from original board to best partial board
        CubeSudokuBoard best = frontier.get(0);
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

        //start DLS from best frontier board to complete puzzle
        System.out.println("Hybrid: Starting DLS on top frontier board...");
        dlsSolver.solve(board, 500000); //internally animates on board
        System.out.println("Hybrid: BFS+DLS finished solving.");
        return;
    }

    //helper method to count number of filled cells in a board
    private int countFilled(CubeSudokuBoard b) {
        int count = 0;
        for (int f = 0; f < 5; f++)
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (b.getCell(f, r, c) != 0) count++;
        return count;
    }
}
