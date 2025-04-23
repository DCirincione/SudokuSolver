//Daniel Cirincione
//13 April, 2025
//CSC301 Program 3: Sudoku Solver

//imports
import solver.*;
import java.util.List;

//main class
public class Main {
    public static void main(String[] args) throws Exception {
        //Load Puzzles
        List<int[][]> puzzles = PuzzleLoader.loadPuzzles("puzzles/easy.txt");
        int[][] puzzle = puzzles.get(0);

        //Create GUI for BFS
        SudokuGUI guiBFS = new SudokuGUI("BFS Solver", 200, 225);
        guiBFS.loadInitialBoard(puzzle);

        //Create GUI for DLS
        SudokuGUI guiDLS = new SudokuGUI("DLS Solver", 750, 225);
        guiDLS.loadInitialBoard(puzzle);

        //Give both GUIs time to load
        Thread.sleep(1000);

        //Run BFS Solver
        SudokuBFS bfsSolver = new SudokuBFS(guiBFS);
        long bfsStart = System.nanoTime();
        List<int[][]> bfsSolutions = bfsSolver.solve(puzzle);
        long bfsEnd = System.nanoTime();
        System.out.println("BFS found " + bfsSolutions.size() + " solution(s).");
        System.out.printf("BFS Time: %.2f ms%n", (bfsEnd - bfsStart) / 1_000_000.0);

        //Run DLS Solver
        SudokuDLS dlsSolver = new SudokuDLS(guiDLS);
        long dlsStart = System.nanoTime();
        List<int[][]> dlsSolutions = dlsSolver.solve(puzzle, 81); //depth limit of 81 (max)
        long dlsEnd = System.nanoTime();
        System.out.println("DLS found " + dlsSolutions.size() + " solution(s).");
        System.out.printf("DLS Time: %.2f ms%n", (dlsEnd - dlsStart) / 1_000_000.0);
    }
}