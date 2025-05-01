package solver;
import java.io.*;
import java.util.*;

public class PuzzleLoader {

    public static List<CubeSudokuBoard> loadCubePuzzles(String filename) throws IOException {
        List<CubeSudokuBoard> puzzles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Face 0:")) continue;

                CubeSudokuBoard cube = new CubeSudokuBoard();
                for (int face = 0; face < 5; face++) {
                    // Read face label line (e.g., "Face 0:")
                    if (face != 0) {
                        line = reader.readLine();
                        if (line == null || !line.startsWith("Face " + face + ":")) {
                            throw new IOException("Unexpected format: Missing Face " + face);
                        }
                    }

                    // Read 9 lines of numbers
                    for (int row = 0; row < 9; row++) {
                        line = reader.readLine();
                        if (line == null || line.length() < 9) {
                            throw new IOException("Unexpected format: Incomplete row at Face " + face);
                        }
                        String[] nums = line.trim().split("\\s+");
                        for (int col = 0; col < 9; col++) {
                            int val = Integer.parseInt(nums[col]);
                            cube.setCell(face, row, col, val);
                        }
                    }
                }

                puzzles.add(cube);
            }
        }
        return puzzles;
    }
}