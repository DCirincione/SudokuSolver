package solver;
import java.io.*;
import java.util.*;

public class PuzzleLoader {
    public static List<int[][]> loadPuzzles(String filename) throws IOException {
        List<int[][]> puzzles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() != 81) continue;
                int[][] board = new int[9][9];
                for (int i = 0; i < 81; i++) {
                    board[i / 9][i % 9] = line.charAt(i) == '0' ? 0 : Character.getNumericValue(line.charAt(i));
                }
                puzzles.add(board);
            }
        }
        return puzzles;
    }
}