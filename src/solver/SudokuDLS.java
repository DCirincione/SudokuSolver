package solver;
import java.util.*;

public class SudokuDLS {

    //SudokuGraph represents adjacency relations for Sudoku constraints on a face
    private final SudokuGraph graph = new SudokuGraph();
    //List to hold all found solutions (usually one for Cube Sudoku)
    private final List<CubeSudokuBoard> solutions = new ArrayList<>();
    //GUI component for visualization; null means no GUI mode
    private final CubeSudokuGUI gui = null;
    //Original board copy to preserve input state
    private CubeSudokuBoard originalBoard;
    //Maximum allowed depth for depth-limited search
    private int maxDepth;
    //Optional canvas for repainting during solving visualization
    private CubeCanvas canvas;

    //now default constructor; gui is optional, canvas null means no visualization
    public SudokuDLS() {
        this.canvas = null;
    }

    //constructor allowing a CubeCanvas for visualization updates during solving
    public SudokuDLS(CubeCanvas canvas) {
        this.canvas = canvas;
    }

    //solve Cube Sudoku board using DLS with constraint propagation
    public List<CubeSudokuBoard> solve(CubeSudokuBoard board, int maxDepth) {
        this.maxDepth = maxDepth;
        this.originalBoard = board.deepCopy();
        solutions.clear();

        //initialize domains for all empty cells: possible numbers that can be placed
        Map<String, Set<Integer>> domains = new HashMap<>();
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0) {
                        Set<Integer> options = new HashSet<>();
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(board, f, r, c, num)) {
                                options.add(num);
                            }
                        }
                        domains.put(f + "," + r + "," + c, options);
                    }
                }
            }
        }

        //start depth-limited DFS with constraint propagation
        dfs(board, domains, 0);
        return solutions;
    }

    //Depth-limited search with forward checking (constraint propagation)
    private void dfs(CubeSudokuBoard board, Map<String, Set<Integer>> domains, int depth) {
        //cut off search if depth limit exceeded
        if (depth > maxDepth) return;
        //stop if a solution has been found
        if (!solutions.isEmpty()) return;

        String bestCell = null;
        int minOptions = Integer.MAX_VALUE;

        //select unassigned cell with fewest remaining values (MRV heuristic)
        for (Map.Entry<String, Set<Integer>> entry : domains.entrySet()) {
            int f = getFace(entry.getKey());
            int r = getRow(entry.getKey());
            int c = getCol(entry.getKey());
            if (board.getCell(f, r, c) == 0) {
                int options = entry.getValue().size();
                if (options < minOptions) {
                    minOptions = options;
                    bestCell = entry.getKey();
                }
            }
        }

        //if no unassigned cells remain, solution found; add a deep copy to solutions
        if (bestCell == null) {
            solutions.add(board.deepCopy());
            return;
        }

        //try each possible number for selected cell
        List<Integer> numbers = new ArrayList<>(domains.get(bestCell));
        for (int num : numbers) {
            int f = getFace(bestCell);
            int r = getRow(bestCell);
            int c = getCol(bestCell);
            if (isValid(board, f, r, c, num)) {
                //assign num to cell
                board.setCell(f, r, c, num);

                //if visualization is enabled, repaint canvas and briefly pause
                if (canvas != null) {
                    canvas.repaint();
                    try {
                        Thread.sleep(15); //adjust this time for animation speed
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                //Deep copy domains before recursion to maintain state isolation
                Map<String, Set<Integer>> newDomains = deepCopyDomains(domains);
                //remove assigned cell from domains
                newDomains.remove(bestCell);

                //forward checking: propagate constraints to neighbors
                for (String key : newDomains.keySet()) {
                    if (affects(bestCell, key)) {
                        newDomains.get(key).remove(num);
                        //if any domain is emptied, backtrack early
                        if (newDomains.get(key).isEmpty()) {
                            board.setCell(f, r, c, 0);
                            return; //backtrack early due to failure
                        }
                    }
                }

                //recurse deeper with updated board and domains
                dfs(board, newDomains, depth + 1);
                //if solution found, stop searching
                if (!solutions.isEmpty()) return;

                //undo assignment (backtrack)
                board.setCell(f, r, c, 0);
            }
        }
    }

    //returns true if assignment at cell 'a' affects cell 'b' (same face and same row, col, or box)
    private boolean affects(String a, String b) {
        return getFace(a) == getFace(b) && (
                getRow(a) == getRow(b) ||
                getCol(a) == getCol(b) ||
                (getRow(a)/3 == getRow(b)/3 && getCol(a)/3 == getCol(b)/3)
        );
    }

    //parse face index from key string "f,r,c"
    private int getFace(String key) {
        return Integer.parseInt(key.split(",")[0]);
    }
    //parse row index from key string "f,r,c"
    private int getRow(String key) {
        return Integer.parseInt(key.split(",")[1]);
    }
    //parse column index from key string "f,r,c"
    private int getCol(String key) {
        return Integer.parseInt(key.split(",")[2]);
    }

    //Deep copy domain state before recursion to preserve immutability
    private Map<String, Set<Integer>> deepCopyDomains(Map<String, Set<Integer>> original) {
        Map<String, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    //heuristic to find best empty cell: fewest options and highest degree (not used currently)
    private int[] findBestEmpty(CubeSudokuBoard board) {
        int minOptions = Integer.MAX_VALUE;
        int maxDegree = -1;
        int[] bestCell = null;
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0) {
                        int options = 0;
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(board, f, r, c, num)) {
                                options++;
                            }
                        }
                        if (options > 0) {
                            int degree = countEmptyNeighbors(board, f, r, c);
                            if (options < minOptions || (options == minOptions && degree > maxDegree)) {
                                minOptions = options;
                                maxDegree = degree;
                                bestCell = new int[]{f, r, c};
                            }
                        }
                    }
                }
            }
        }
        return bestCell;
    }

    //Count number of empty neighboring cells on same face (degree heuristic)
    private int countEmptyNeighbors(CubeSudokuBoard board, int face, int row, int col) {
        int count = 0;
        for (int neighbor : graph.getNeighbors(row * 9 + col)) {
            int r = neighbor / 9;
            int c = neighbor % 9;
            if (board.getCell(face, r, c) == 0) {
                count++;
            }
        }
        return count;
    }

    //find any empty cell, used in simpler solving strategies (not currently used)
    private int[] findEmpty(CubeSudokuBoard board) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0)
                        return new int[]{f, r, c};
                }
            }
        }
        return null;
    }

    //check if placing num at (face,row,col) is valid considering Sudoku and cube constraints
    private boolean isValid(CubeSudokuBoard board, int face, int row, int col, int num) {
        int index = row * 9 + col;
        //check all neighbors on same face to avoid duplicates
        for (int neighbor : graph.getNeighbors(index)) {
            int r = neighbor / 9;
            int c = neighbor % 9;
            if (board.getCell(face, r, c) == num) return false;
        }

        //Check cross-face edge overlaps (cube adjacency constraints)
        if (face == 0 && row == 8) { //Top -> Middle
            if (board.getCell(1, 0, col) == num) return false;
        }
        if (face == 1 && row == 0) { //Middle -> Top
            if (board.getCell(0, 8, col) == num) return false;
        }
        if (face == 1 && row == 8) { //Middle -> Bottom
            if (board.getCell(4, 0, col) == num) return false;
        }
        if (face == 4 && row == 0) { //Bottom -> Middle
            if (board.getCell(1, 8, col) == num) return false;
        }
        if (face == 2 && col == 8) { //Left -> Middle
            if (board.getCell(1, row, 0) == num) return false;
        }
        if (face == 1 && col == 0) { //Middle -> Left
            if (board.getCell(2, row, 8) == num) return false;
        }
        if (face == 3 && col == 0) { //Right -> Middle
            if (board.getCell(1, row, 8) == num) return false;
        }
        if (face == 1 && col == 8) { //Middle -> Right
            if (board.getCell(3, row, 0) == num) return false;
        }

        //Check cross-face corner overlaps (cube corner adjacency)
        //top Face corners
        if (face == 0 && row == 8 && col == 0) { //Top left -> Left top + Middle top-left
            if (board.getCell(2, 0, 8) == num || board.getCell(1, 0, 0) == num) return false;
        }
        if (face == 0 && row == 8 && col == 8) { //Top right -> Right top + Middle top-right
            if (board.getCell(3, 0, 0) == num || board.getCell(1, 0, 8) == num) return false;
        }

        //bottom Face corners
        if (face == 4 && row == 0 && col == 0) { //Bottom left -> Left bottom + Middle bottom-left
            if (board.getCell(2, 8, 8) == num || board.getCell(1, 8, 0) == num) return false;
        }
        if (face == 4 && row == 0 && col == 8) { //Bottom right -> Right bottom + Middle bottom-right
            if (board.getCell(3, 8, 0) == num || board.getCell(1, 8, 8) == num) return false;
        }

        //Left Face corners
        if (face == 2 && row == 0 && col == 8) { //Top of Left -> Top bottom-left
            if (board.getCell(0, 8, 0) == num || board.getCell(1, 0, 0) == num) return false;
        }
        if (face == 2 && row == 8 && col == 8) { //Bottom of Left -> Bottom top-left
            if (board.getCell(4, 0, 0) == num || board.getCell(1, 8, 0) == num) return false;
        }

        //Right Face corners
        if (face == 3 && row == 0 && col == 0) { //Top of Right -> Top bottom-right
            if (board.getCell(0, 8, 8) == num || board.getCell(1, 0, 8) == num) return false;
        }
        if (face == 3 && row == 8 && col == 0) { //Bottom of Right -> Bottom top-right
            if (board.getCell(4, 0, 8) == num || board.getCell(1, 8, 8) == num) return false;
        }

        //Check 3x3 box within same face for duplicates
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if ((r != row || c != col) && board.getCell(face, r, c) == num) {
                    return false;
                }
            }
        }
        return true;
    }
}