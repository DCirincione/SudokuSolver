//generates sudoku puzzles of different difficulties
package solver;

import java.io.*;
import java.util.*;

//main class for sudoku generation logic
public class PuzzleGenerator {
    public static void main(String[] args) throws IOException {
        generateAndSavePuzzles("puzzles/easy.txt", 5, 35);
        generateAndSavePuzzles("puzzles/medium.txt", 5, 45);
        generateAndSavePuzzles("puzzles/hard.txt", 5, 55);
    }

    private static void generateAndSavePuzzles(String filename, int count, int removeCount) throws IOException {
        File file = new File(filename);
        file.getParentFile().mkdirs(); //ensure folder exists
        try (PrintWriter writer = new PrintWriter(file)) {
            for (int i = 0; i < count; i++) {
                int[][] board = generateFullBoard();
                removeCells(board, removeCount);
                writer.println(flattenBoard(board));
            }
        }
        System.out.println("Wrote " + count + " puzzles to " + filename);
    }

    //Board Generation

    //fills a board with a valid complete sudoku solution
    private static int[][] generateFullBoard() {
        int[][] board = new int[9][9];
        //initialize empty 9x9 board
        fillDiagonalBoxes(board);
        solveBoard(board);
        //return completed board
        return board;
    }

    private static void fillDiagonalBoxes(int[][] board) {
        for (int i = 0; i < 9; i += 3) {
            fillBox(board, i, i);
        }
    }

    private static void fillBox(int[][] board, int row, int col) {
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= 9; i++) nums.add(i);
        Collections.shuffle(nums);

        int idx = 0;
        for (int r = row; r < row + 3; r++) {
            for (int c = col; c < col + 3; c++) {
                board[r][c] = nums.get(idx++);
            }
        }
    }

    //recursive backtracking function to fill board
    private static boolean solveBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    List<Integer> nums = new ArrayList<>();
                    for (int i = 1; i <= 9; i++) nums.add(i);
                    Collections.shuffle(nums);

                    for (int num : nums) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveBoard(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false; //no valid number found
                }
            }
        }
        return true; //board is filled
    }

    //checks if placing a number follows sudoku rules
    private static boolean isSafe(int[][] board, int row, int col, int num) {
        return !inRow(board, row, num) &&
                !inCol(board, col, num) &&
                !inBox(board, row - row % 3, col - col % 3, num);
    }

    private static boolean inRow(int[][] board, int row, int num) {
        for (int col = 0; col < 9; col++)
            if (board[row][col] == num) return true;
        return false;
    }

    private static boolean inCol(int[][] board, int col, int num) {
        for (int row = 0; row < 9; row++)
            if (board[row][col] == num) return true;
        return false;
    }

    private static boolean inBox(int[][] board, int boxStartRow, int boxStartCol, int num) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                if (board[row + boxStartRow][col + boxStartCol] == num) return true;
        return false;
    }

    //Helpers

    //removes cells randomly from board to create a puzzle
    private static void removeCells(int[][] board, int count) {
        Random rand = new Random(); //random number generator
        int removed = 0; //count of removed cells

        while (removed < count) {
            int row = rand.nextInt(9); //pick random row
            int col = rand.nextInt(9); //pick random column
            if (board[row][col] != 0) { //if cell is not already empty
                board[row][col] = 0; //remove number from cell
                removed++; //increment removed counter
            }
        }
    }

    private static String flattenBoard(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board)
            for (int num : row)
                sb.append(num);
        return sb.toString();
    }
}