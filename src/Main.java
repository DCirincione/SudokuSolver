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

        //create top panel for puzzle selection and reset
        JPanel topPanel = new JPanel(new BorderLayout());
        String[] difficulties = {"easy", "medium", "hard"};
        JComboBox<String> difficultySelector = new JComboBox<>(difficulties);
        JButton resetButton = new JButton("Reset");

        topPanel.add(difficultySelector, BorderLayout.WEST);
        topPanel.add(resetButton, BorderLayout.EAST);

        CubeSudokuBoard originalPuzzle = puzzle.deepCopy();

        //create and display main GUI frame for Cube Sudoku solver
        JFrame frame = new JFrame("3D Cube Sudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.add(topPanel, BorderLayout.NORTH);

        CubeCanvas canvas = new CubeCanvas(puzzle); //visual component to display puzzle
        frame.add(canvas, BorderLayout.CENTER);

        //panel to hold solver control buttons
        JPanel buttonPanel = new JPanel();
        JButton bfsButton = new JButton("Solve with BFS and DLS Hybrid"); //Breadth-first search and DLS solver button
        JButton dlsButton = new JButton("Solve with DLS"); //depth-limited search solver button

        //add buttons to choose search method
        buttonPanel.add(bfsButton);
        buttonPanel.add(dlsButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null); //center on screen
        frame.setVisible(true);

        resetButton.addActionListener(e -> {
            CubeSudokuBoard resetCopy = originalPuzzle.deepCopy();  //make a deep copy to avoid modifying original puzzle
            canvas.updateBoard(resetCopy);                         //update canvas with new (reset) board
            canvas.repaint();                                      //trigger a repaint to visually reflect reset
        });

//add an action listener to difficulty selector dropdown
        difficultySelector.addActionListener(e -> {
            //Get selected difficulty as a string (e.g., "easy", "medium", "hard")
            String selected = (String) difficultySelector.getSelectedItem();
            try {
                //load puzzles from corresponding text file
                List<CubeSudokuBoard> newPuzzles = PuzzleLoader.loadCubePuzzles("puzzles/" + selected + ".txt");

                //If at least one puzzle was loaded
                if (!newPuzzles.isEmpty()) {
                    //Take first puzzle from list
                    CubeSudokuBoard newPuzzle = newPuzzles.get(0);

                    //mark original (non-editable) cells
                    newPuzzle.markOriginalCells();

                    //Update internal reference to original puzzle
                    originalPuzzle.copyFrom(newPuzzle);

                    //update GUI canvas with a deep copy of new puzzle
                    canvas.updateBoard(newPuzzle.deepCopy());
                    canvas.repaint(); //Redraw canvas to reflect changes
                } else {
                    //Handle case where no puzzles were found in file
                    System.out.println("No puzzles found for: " + selected);
                }
            } catch (java.io.IOException ex) {
                //Handle file I/O errors
                ex.printStackTrace();
                System.out.println("Failed to load puzzle file: " + selected);
            }
        });

        //attach BFS+DLS hybrid solver to Solve with BFS button. Runs solver in a new thread to keep GUI responsive
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

        //attach DLS solver to Solve with DLS button. Runs solver in a new thread to keep GUI responsive and updates canvas after solving
        dlsButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SudokuDLS dlsSolver = new SudokuDLS(canvas); //pass canvas to update GUI during solving
                    List<CubeSudokuBoard> dlsSolutions = dlsSolver.solve(puzzle, 50000); //depth limit large
                    if (!dlsSolutions.isEmpty()) {
                        System.out.println("DLS solving complete.");
                        CubeSudokuBoard solvedBoard = dlsSolutions.get(0);
                        canvas.updateBoard(solvedBoard);
                        canvas.repaint();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }

    //Checks if updated board is too similar to original board
//used to determine whether BFS algorithm has made meaningful progress
    private static boolean boardsAreTooSimilar(CubeSudokuBoard original, CubeSudokuBoard updated) {
        int diffCount = 0;

        //loop over all 5 faces of cube
        for (int f = 0; f < 5; f++) {
            //loop over all rows and columns in each face (9x9 grid)
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    //Compare cell values between original and updated boards
                    if (original.getCell(f, r, c) != updated.getCell(f, r, c)) {
                        diffCount++;
                    }
                }
            }
        }

        //if fewer than 5 cells have changed, boards are considered too similar and likely indicate that BFS is not progressing enough
        return diffCount < 5;
    }
}