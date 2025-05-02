package solver;

public class CubeSudokuBoard {
    // 5 faces: 0=Top, 1=Front, 2=Left, 3=Right, 4=Bottom
    private int[][][] faces;
    // Tracks which cells were original (given at the start)
    private boolean[][][] original;

    public CubeSudokuBoard() {
        faces = new int[5][9][9]; // 5 faces, 9x9 each
        original = new boolean[5][9][9];
    }

    // Set a value at a given face, row, col
    // Only mark original elsewhere (not here)
    public void setCell(int face, int row, int col, int value) {
        faces[face][row][col] = value;
        syncOverlappingCells(face, row, col, value);
    }

    // Get a value at a given face, row, col
    public int getCell(int face, int row, int col) {
        return faces[face][row][col];
    }

    // Placeholder: Handle overlaps (to be fully implemented)
    private void syncOverlappingCells(int face, int row, int col, int value) {
        // TODO: Implement synchronization between faces when overlaps exist
        // (Example: corners shared between Top, Front, and Left faces)
    }

    // Deep copy of the entire cube board
    public CubeSudokuBoard deepCopy() {
        CubeSudokuBoard copy = new CubeSudokuBoard();
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                System.arraycopy(this.faces[f][r], 0, copy.faces[f][r], 0, 9);
            }
        }
        copy.original = new boolean[5][9][9];
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                System.arraycopy(this.original[f][r], 0, copy.original[f][r], 0, 9);
            }
        }
        return copy;
    }

    // Mark all currently filled cells as original (given at the start)
    public void markOriginalCells() {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (faces[f][r][c] != 0) {
                        original[f][r][c] = true;
                    }
                }
            }
        }
    }

    // Returns whether a cell is original (given at the start)
    public boolean isOriginal(int face, int row, int col) {
        return original[face][row][col];
    }

    // Explicitly mark a cell as original
    public void markOriginal(int face, int row, int col) {
        original[face][row][col] = true;
    }

    // Explicitly unmark a cell as original
    public void unmarkOriginal(int face, int row, int col) {
        original[face][row][col] = false;
    }

    // Copy the values from another CubeSudokuBoard into this one
    public void copyFrom(CubeSudokuBoard other) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    this.setCell(f, r, c, other.getCell(f, r, c));
                }
            }
        }
    }
}
