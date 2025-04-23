package solver;
import java.util.*;

public class SudokuGraph {
    private final Map<Integer, Set<Integer>> adjacencyList;

    public SudokuGraph() {
        this.adjacencyList = new HashMap<>();
        buildGraph();
    }

    private void buildGraph() {
        for (int cell = 0; cell < 81; cell++) {
            Set<Integer> neighbors = new HashSet<>();
            int row = cell / 9;
            int col = cell % 9;

            for (int i = 0; i < 9; i++) {
                //Same row
                neighbors.add(row * 9 + i);
                //Same column
                neighbors.add(i * 9 + col);
            }

            //Same 3x3 box
            int boxRow = (row / 3) * 3;
            int boxCol = (col / 3) * 3;
            for (int r = boxRow; r < boxRow + 3; r++) {
                for (int c = boxCol; c < boxCol + 3; c++) {
                    neighbors.add(r * 9 + c);
                }
            }

            neighbors.remove(cell); //don't connect to itself
            adjacencyList.put(cell, neighbors);
        }
    }

    public Set<Integer> getNeighbors(int cellIndex) {
        return adjacencyList.getOrDefault(cellIndex, new HashSet<>());
    }

    public Map<Integer, Set<Integer>> getAdjacencyList() {
        return adjacencyList;
    }
}