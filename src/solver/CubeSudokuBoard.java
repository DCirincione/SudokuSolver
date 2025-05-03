package solver;

public class CubeSudokuBoard { //represents a 3d sudoku board with 5 9x9 faces
    private int[][][] faces; //holds values for each cell on each face
    private boolean[][][] original; //tracks which cells were part of the original puzzle

    public CubeSudokuBoard() {
        faces = new int[5][9][9]; //initialize 5 faces, each 9x9
        original = new boolean[5][9][9]; //initialize original tracker
    }

    //set a value at a given face, row, col
    //only mark original elsewhere (not here)
    public void setCell(int face, int row, int col, int value) {
        faces[face][row][col] = value; //set cell value
        syncOverlappingCells(face, row, col, value); //update overlapping cells if needed
    }

    //get a value at a given face, row, col
    public int getCell(int face, int row, int col) {
        return faces[face][row][col]; //get cell value
    }

    //placeholder: handle overlaps to be fully implemented
    private void syncOverlappingCells(int face, int row, int col, int value) {
        //todo: handle cases where a cell affects multiple faces
    }

    //deep copy of the entire cube board
    public CubeSudokuBoard deepCopy() {
        CubeSudokuBoard copy = new CubeSudokuBoard(); //create new board
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                System.arraycopy(this.faces[f][r], 0, copy.faces[f][r], 0, 9); //copy face values
            }
        }
        copy.original = new boolean[5][9][9]; //copy original cell flags
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                System.arraycopy(this.original[f][r], 0, copy.original[f][r], 0, 9);
            }
        }
        return copy;
    }

    //mark all currently filled cells as original (given at the start)
    public void markOriginalCells() {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (faces[f][r][c] != 0) {
                        original[f][r][c] = true; //mark non-zero cells as original
                    }
                }
            }
        }
    }

    //returns whether a cell is original (given at the start)
    public boolean isOriginal(int face, int row, int col) {
        return original[face][row][col]; //return if cell is original
    }

    //explicitly mark a cell as original
    public void markOriginal(int face, int row, int col) {
        original[face][row][col] = true; //manually mark a cell as original
    }

    //explicitly unmark a cell as original
    public void unmarkOriginal(int face, int row, int col) {
        original[face][row][col] = false; //manually unmark a cell as original
    }

    //Copy the values from another CubeSudokuBoard into this one
    public void copyFrom(CubeSudokuBoard other) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    this.setCell(f, r, c, other.getCell(f, r, c)); //copy each cell from other board
                }
            }
        }
    }
}
