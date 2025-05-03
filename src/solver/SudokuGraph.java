/**
 * SudokuGraph represents static constraint relationships between cells
 * in a standard 9x9 Sudoku grid. It builds an adjacency list where each cell
 * is connected to all other cells in same row, column, and 3x3 subgrid.
 *
 * This structure is useful for checking Sudoku rules (e.g., avoiding duplicate
 * numbers in related cells) and can support constraint propagation or validation
 * logic during puzzle generation or solving.
 *
 * Note: This class does NOT represent transitions between full board states,
 * and is not a graph in traversal/search sense (e.g., for BFS or A* search).
 */

package solver;
import java.util.*;

public class SudokuGraph {
    private final Map<Integer, Set<Integer>> adjacencyList;
    //maps each cell index to a set of connected cell indices

    public SudokuGraph() {
        //constructor builds static constraint graph
        this.adjacencyList = new HashMap<>();
        //initialize adjacency list
        buildGraph();
    }

    private void buildGraph() {
        //iterate through all 81 cells in sudoku board
        for (int cell = 0; cell < 81; cell++) {
            Set<Integer> neighbors = new HashSet<>();
            int row = cell / 9;
            int col = cell % 9;

            //connect cells in same row and column
            for (int i = 0; i < 9; i++) {
                //Same row
                neighbors.add(row * 9 + i);
                //Same column
                neighbors.add(i * 9 + col);
            }

            //connect cells in same 3x3 box
            int boxRow = (row / 3) * 3;
            int boxCol = (col / 3) * 3;
            for (int r = boxRow; r < boxRow + 3; r++) {
                for (int c = boxCol; c < boxCol + 3; c++) {
                    neighbors.add(r * 9 + c);
                }
            }

            neighbors.remove(cell);
            //remove self to avoid self-loop
            adjacencyList.put(cell, neighbors);
            //store neighbors for current cell
        }
    }

    public Set<Integer> getNeighbors(int cellIndex) {
        Set<Integer> neighbors = adjacencyList.getOrDefault(cellIndex, new HashSet<>());
        //return set of connected neighbors or empty if not found
        return neighbors;
    }

    public Map<Integer, Set<Integer>> getAdjacencyList() {
        //return full adjacency list
        return adjacencyList;
    }
}