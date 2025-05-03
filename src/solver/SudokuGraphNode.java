/**
 * Represents a node in the Sudoku state graph used for BFS search.
 * Each node stores a snapshot of the CubeSudokuBoard, a reference to
 * its parent node (to trace the solution path), and its depth level
 * from the root.
 */

package solver;

public class SudokuGraphNode {
    private final CubeSudokuBoard board;
    private final SudokuGraphNode parent;
    private final int depth;

    public SudokuGraphNode(CubeSudokuBoard board, SudokuGraphNode parent, int depth) {
        this.board = board;
        this.parent = parent;
        this.depth = depth;
    }

    public CubeSudokuBoard getBoard() {
        return board;
    }

    public SudokuGraphNode getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public String getBoardHash() {
        StringBuilder sb = new StringBuilder(); //create a string builder to store board state
        for (int f = 0; f < 5; f++) { //loop through each face
            for (int r = 0; r < 9; r++) { //loop through each row
                for (int c = 0; c < 9; c++) { //loop through each column
                    sb.append(board.getCell(f, r, c)); //append value at this cell to string builder
                }
            }
        }
        return sb.toString(); //return full board hash as a string
    }
}