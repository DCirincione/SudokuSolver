//Daniel Cirincione
//13 April, 2025
//CSC301 Program 3: Sudoku Solver

//imports for solver classes, GUI components, layout, and list handling
import solver.*;
import java.awt.BorderLayout;
import javax.swing.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        //load Cube Sudoku puzzles from file
        List<CubeSudokuBoard> puzzles = PuzzleLoader.loadCubePuzzles("puzzles/hard.txt");

        //check if any puzzles were loaded. if none, exit early
        if (puzzles.isEmpty()) {
            System.out.println("No puzzles found!");
            return;
        }

        CubeSudokuBoard puzzle = puzzles.get(0); //load first puzzle from list
        puzzle.markOriginalCells(); //mark original clues to distinguish from user/solver entries

        //create and display main GUI frame for Cube Sudoku solver
        JFrame frame = new JFrame("3D Cube Sudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        CubeCanvas canvas = new CubeCanvas(puzzle); //visual component to display puzzle
        frame.add(canvas, BorderLayout.CENTER);

        //panel to hold solver control buttons
        JPanel buttonPanel = new JPanel();
        JButton bfsButton = new JButton("Solve with BFS and DLS Hybrid"); //breadth-first search and dls solver button
        JButton dlsButton = new JButton("Solve with DLS"); //depth-limited search solver button

        //add buttons to chose search method
        buttonPanel.add(bfsButton);
        buttonPanel.add(dlsButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null); //center on screen
        frame.setVisible(true);

        //attach BFS+DLS hybrid solver to Solve with BFS button. runs solver in a new thread to keep GUI responsive
        bfsButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    int bfsDepthLimit = 5;
                    HybridSolver hybrid = new HybridSolver(puzzle, canvas, bfsDepthLimit);
                    hybrid.solve();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        //attach DLS solver to Solve with DLS button. runs solver in a new thread to keep GUI responsive
        dlsButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SudokuDLS dlsSolver = new SudokuDLS(canvas); //pass canvas to update GUI during solving
                    List<CubeSudokuBoard> dlsSolutions = dlsSolver.solve(puzzle, 50000); //depth limit large
                    if (!dlsSolutions.isEmpty()) {
                        System.out.println("DLS solving complete.");
                        CubeSudokuBoard solvedBoard = dlsSolutions.get(0);
                        SudokuDLS validator = new SudokuDLS(null); //no canvas needed for validation only
                        //validate solved board after DLS completion
                        canvas.updateBoard(solvedBoard);
                        canvas.repaint();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }
    //helper method to copy board state from source to destination. deep copy of all faces and cells
    private static void copyBoard(CubeSudokuBoard source, CubeSudokuBoard destination) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    destination.setCell(f, r, c, source.getCell(f, r, c));
                }
            }
        }
    }

    private static boolean boardsAreTooSimilar(CubeSudokuBoard original, CubeSudokuBoard updated) {
        int diffCount = 0;
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (original.getCell(f, r, c) != updated.getCell(f, r, c)) {
                        diffCount++;
                    }
                }
            }
        }
        return diffCount < 5; // fewer than 5 changes = not enough BFS progress
    }
}