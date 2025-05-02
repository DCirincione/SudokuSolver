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
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    sb.append(board.getCell(f, r, c));
                }
            }
        }
        return sb.toString();
    }
}