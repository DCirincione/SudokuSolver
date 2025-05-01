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

        // Run DLS in parallel on top frontier boards using ExecutorService
        int attemptLimit = Math.min(frontier.size(), 5000);  // Try top 10 boards at most
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<List<CubeSudokuBoard>> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < attemptLimit; i++) {
            CubeSudokuBoard candidate = frontier.get(i);
            completionService.submit(() -> dlsSolver.solve(candidate, 500000));
        }

        try {
            for (int i = 0; i < attemptLimit; i++) {
                Future<List<CubeSudokuBoard>> future = completionService.take();
                List<CubeSudokuBoard> result = future.get();
                if (result != null && !result.isEmpty()) {
                    CubeSudokuBoard solvedBoard = result.get(0);
                    SwingUtilities.invokeLater(() -> {
                        copyBoard(solvedBoard, board);
                        canvas.updateBoard(solvedBoard);
                        canvas.repaint();
                    });
                    System.out.println("Hybrid: BFS+DLS solved the puzzle.");
                    executor.shutdownNow(); // stop all remaining tasks
                    return;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow(); // ensure shutdown if no solution
        }

        System.out.println("Hybrid: No solutions found from DLS.");
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
