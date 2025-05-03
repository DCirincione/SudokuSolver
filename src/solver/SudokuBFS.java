package solver;
import solver.SudokuGraphNode;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import solver.CubeSudokuBoard;
import solver.CubeCanvas;
import solver.CubeSudokuGUI;

public class SudokuBFS {
    //graph representing adjacency of cells within a face for constraint checks
    private final SudokuGraph graph = new SudokuGraph();
    //list to store found solutions (usually one or few)
    private final List<CubeSudokuBoard> solutions = new ArrayList<>();
    //optional GUI for live updates during solving
    private final CubeSudokuGUI gui;

    //now default constructor; gui is optional and may be null
    public SudokuBFS() {
        this.gui = null;
    }

    //solve board using BFS with multithreading; returns list of solutions
    public List<CubeSudokuBoard> solve(CubeSudokuBoard board) {
        //Thread-safe queue holding graph nodes to explore
        ConcurrentLinkedQueue<SudokuGraphNode> queue = new ConcurrentLinkedQueue<>();
        SudokuGraphNode startNode = new SudokuGraphNode(board.deepCopy(), null, 0);
        queue.add(startNode);

        //preprocessing: Apply forced moves (single option fill) before starting BFS
        CubeSudokuBoard preprocessed = board.deepCopy();
        applySingleOptionFill(preprocessed);
        queue.clear();
        SudokuGraphNode preprocessedNode = new SudokuGraphNode(preprocessed, null, 0);
        queue.add(preprocessedNode);

        final int MAX_QUEUE_SIZE = 5000; //Max queue size before pruning

        //visited set to avoid revisiting same board states
        Set<String> visited = Collections.synchronizedSet(new HashSet<>());

        //create thread pool with 8 workers to speed up BFS
        ExecutorService executor = Executors.newFixedThreadPool(8);
        AtomicBoolean foundSolution = new AtomicBoolean(false); //Flag to stop when solution found

        //main BFS solving loop using multithreaded workers
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                while (!queue.isEmpty() && !foundSolution.get()) {
                    SudokuGraphNode currentNode = queue.poll();
                    if (currentNode == null) continue;
                    CubeSudokuBoard current = currentNode.getBoard();

                    //find next best empty cell with fewest legal options (MRV)
                    int[] emptyCell = findBestEmpty(current);
                    if (emptyCell == null) {
                        //no empty cells means solution found; add to solutions list
                        synchronized (solutions) {
                            solutions.add(current.deepCopy());
                            foundSolution.set(true);
                        }
                        return;
                    }

                    int face = emptyCell[0];
                    int row = emptyCell[1];
                    int col = emptyCell[2];

                    //collect all valid candidate numbers for this cell
                    List<Integer> candidates = new ArrayList<>();
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(current, face, row, col, num)) {
                            candidates.add(num);
                        }
                    }
                    //sort candidates by how many conflicts they cause (least conflicts first)
                    candidates.sort(Comparator.comparingInt(n -> countConstraints(current, face, row, col, n)));

                    //try each candidate number and enqueue resulting board if valid
                    for (int num : candidates) {
                        CubeSudokuBoard next = current.deepCopy();
                        next.setCell(face, row, col, num);

                        //apply forced moves after setting cell
                        applySingleOptionFill(next);

                        //only add next board if it still has valid domains (no dead ends)
                        if (hasValidDomains(next)) {
                            if (gui != null) {
                                //Update GUI with this move (non-blocking)
                                gui.updateCell(face, row, col, num, false);
                                try {
                                    Thread.sleep(0);
                                } catch (InterruptedException ignored) {}
                            }
                            SudokuGraphNode childNode = new SudokuGraphNode(next, currentNode, currentNode.getDepth() + 1);
                            String hash = childNode.getBoardHash();
                            if (!visited.contains(hash)) {
                                visited.add(hash);
                                queue.add(childNode);
                                //Prune queue if it grows too large to keep search manageable
                                if (queue.size() > MAX_QUEUE_SIZE) {
                                    //Pruning for SudokuGraphNode queue: extract boards, prune, rewrap
                                    List<SudokuGraphNode> nodes = new ArrayList<>(queue);
                                    nodes.sort(Comparator.comparingInt(nod -> countEmptyCells(nod.getBoard())));
                                    int keepSize = (int)(nodes.size() * 0.6);
                                    queue.clear();
                                    for (int j = 0; j < keepSize; j++) {
                                        queue.add(nodes.get(j));
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        //shutdown executor and wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ignored) {}
        }

        return solutions;
    }

    //visual BFS solver for live GUI step-by-step visualization; slower but interactive
    public List<CubeSudokuBoard> solveLive(CubeSudokuBoard board, CubeCanvas canvas) throws InterruptedException {
        List<CubeSudokuBoard> liveSolutions = new ArrayList<>();
        Queue<CubeSudokuBoard> queue = new LinkedList<>();
        queue.add(board.deepCopy());

        //simple BFS loop for live visualization
        while (!queue.isEmpty()) {
            CubeSudokuBoard current = queue.poll();

            //find first empty cell (no MRV heuristic here)
            int[] emptyCell = findEmpty(current);
            if (emptyCell == null) {
                //Solution found, add and stop
                liveSolutions.add(current.deepCopy());
                break;
            }

            int face = emptyCell[0];
            int row = emptyCell[1];
            int col = emptyCell[2];

            //try all candidates for this cell
            for (int num = 1; num <= 9; num++) {
                if (isValid(current, face, row, col, num)) {
                    CubeSudokuBoard nextBoard = current.deepCopy();
                    nextBoard.setCell(face, row, col, num);
                    queue.add(nextBoard);

                    //update original board and repaint canvas for live effect
                    board.setCell(face, row, col, num);
                    canvas.repaint();
                    Thread.sleep(25);
                }
            }
        }

        return liveSolutions;
    }

    //finds first empty cell (0); returns [face, row, col] or null if full
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

    //find next best empty cell with fewest legal options (MRV heuristic)
    private int[] findBestEmpty(CubeSudokuBoard board) {
        int[] best = null;
        int minOptions = Integer.MAX_VALUE;

        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0) {
                        int options = 0;
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(board, f, r, c, num)) options++;
                        }
                        if (options < minOptions) {
                            minOptions = options;
                            best = new int[]{f, r, c};
                        }
                    }
                }
            }
        }

        return best;
    }

    //check if all empty cells have valid domains; prune if any cell is unsolvable or too constrained
    private boolean hasValidDomains(CubeSudokuBoard board) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0) {
                        int options = 0;
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(board, f, r, c, num)) {
                                options++;
                                if (options > 1) break; //early exit if more than 1 option
                            }
                        }
                        if (options == 0) return false; //unsolvable spot
                    }
                }
            }
        }
        return true;
    }

    //uses graph to check if placing 'num' at (face, row, col) is valid considering neighbors and cross-face edges
    private boolean isValid(CubeSudokuBoard board, int face, int row, int col, int num) {
        int index = row * 9 + col;
        //check neighbors within same face for conflicts
        for (int neighbor : graph.getNeighbors(index)) {
            int r = neighbor / 9;
            int c = neighbor % 9;
            if (board.getCell(face, r, c) == num) return false;
        }
        //Cross-face overlap checks (simple cube edge connections)
        //Example of cross-face overlaps
        if (face == 0 && row == 8) { //Top face bottom edge connects to Middle face top row
            if (board.getCell(1, 0, col) == num) return false;
        }
        if (face == 1 && row == 0) { //Middle face top row connects to Top face bottom edge
            if (board.getCell(0, 8, col) == num) return false;
        }
        if (face == 1 && row == 8) { //Middle face bottom row connects to Bottom face top row
            if (board.getCell(4, 0, col) == num) return false;
        }
        if (face == 4 && row == 0) { //Bottom face top row connects to Middle face bottom edge
            if (board.getCell(1, 8, col) == num) return false;
        }
        if (face == 2 && col == 8) { //Left face right edge connects to Middle face left column
            if (board.getCell(1, row, 0) == num) return false;
        }
        if (face == 1 && col == 0) { //Middle face left column connects to Left face right edge
            if (board.getCell(2, row, 8) == num) return false;
        }
        if (face == 3 && col == 0) { //Right face left edge connects to Middle face right column
            if (board.getCell(1, row, 8) == num) return false;
        }
        if (face == 1 && col == 8) { //Middle face right column connects to Right face left edge
            if (board.getCell(3, row, 0) == num) return false;
        }
        return true;
    }

    //count how many conflicts placing 'num' at (face, row, col) would cause in other empty cells
    private int countConstraints(CubeSudokuBoard board, int face, int row, int col, int num) {
        int conflicts = 0;
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0 && !(f == face && r == row && c == col)) {
                        if (!isValid(board, f, r, c, num)) {
                            conflicts++;
                        }
                    }
                }
            }
        }
        return conflicts;
    }

    //Prune queue to keep only most promising boards (fewest empty cells)
    private void pruneQueue(ConcurrentLinkedQueue<CubeSudokuBoard> queue) {
        List<CubeSudokuBoard> boards = new ArrayList<>(queue);
        //cort boards by number of empty cells (ascending)
        boards.sort(Comparator.comparingInt(this::countEmptyCells));
        //ceep only most promising 60% boards
        int keepSize = (int)(boards.size() * 0.6);
        queue.clear();
        for (int i = 0; i < keepSize; i++) {
            queue.add(boards.get(i));
        }
    }

    //count how many empty cells a board has
    private int countEmptyCells(CubeSudokuBoard board) {
        int count = 0;
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0) count++;
                }
            }
        }
        return count;
    }

    //apply forced moves: fill cells that have only one valid option (one pass, non-recursive)
    private void applySingleOptionFill(CubeSudokuBoard board) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) == 0) {
                        List<Integer> options = new ArrayList<>();
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(board, f, r, c, num)) {
                                options.add(num);
                            }
                        }
                        if (options.size() == 1) {
                            board.setCell(f, r, c, options.get(0));
                        }
                    }
                }
            }
        }
    }
    //adaptive BFS for hybrid solving: explores until queue is empty or progress stalls, focusing on at least first face.
    public List<CubeSudokuBoard> solveAdaptive(CubeSudokuBoard startBoard, CubeCanvas canvas, CubeSudokuGUI gui) {
        List<CubeSudokuBoard> frontierBoards = new ArrayList<>();
        Queue<BFSNode> queue = new LinkedList<>();

        int initialIndex = findNextEmptyCellIndex(startBoard, 0);
        if (initialIndex == -1) {
            frontierBoards.add(startBoard.deepCopy());
            return frontierBoards;
        }

        queue.add(new BFSNode(startBoard.deepCopy(), initialIndex, 0));

        int maxFrontierSize = 50000;
        int bestFilled = countFilledCells(startBoard);
        int maxDepth = 20;
        int bestFace0Filled = countFaceFilledCells(startBoard, 0);

        while (!queue.isEmpty()) {
            System.out.println("Queue size: " + queue.size());
            BFSNode node = queue.poll();
            if (node == null) {
                System.out.println("Polled null node");
                continue;
            }
            System.out.println("Processing node at depth: " + node.depth);
            int[] cell = indexToFaceRowCol(node.cellIndex);
            int face = cell[0], row = cell[1], col = cell[2];

            boolean progressMade = false;

            for (int num = 1; num <= 9; num++) {
                if (isValid(node.board, face, row, col, num)) {
                    CubeSudokuBoard next = node.board.deepCopy();
                    next.setCell(face, row, col, num);

                    applySingleOptionFill(next);

                    if (!hasValidDomains(next)) continue;

                    int filled = countFilledCells(next);
                    int face0Filled = countFaceFilledCells(next, 0);
                    //new logic for stagnation/pruning
                    if (face0Filled > bestFace0Filled) {
                        bestFace0Filled = face0Filled;
                        node.lastFace0ProgressDepth = node.depth;
                    } else if (node.depth - node.lastFace0ProgressDepth > 4 && queue.size() > 100) {
                        //allow deeper search if queue is small, else skip stagnating paths
                        continue;
                    }

                    if (filled > bestFilled) bestFilled = filled;

                    int nextIndex = findNextEmptyCellIndex(next, node.cellIndex + 1);
                    //always add boards that reach depth 6 or more as frontier candidates
                    if (nextIndex != -1) {
                        queue.add(new BFSNode(next, nextIndex, node.depth + 1, node.lastFace0ProgressDepth));
                    }
                    //keep all boards that reach depth 6 or more as frontier candidates
                    if (node.depth >= 6 || nextIndex == -1) {
                        System.out.println("BFS: Added frontier board with " + countFilledCells(next) + " filled cells.");
                        frontierBoards.add(next);
                    }

                    if (canvas != null) {
                        copyBoard(next, startBoard);
                        canvas.repaint();
                    }

                    if (gui != null) {
                        gui.updateCell(face, row, col, num, false);
                    }

                    progressMade = true;
                }
            }

            if (!progressMade && queue.isEmpty()) {
                break;
            }
        }

        if (frontierBoards.isEmpty()) {
            frontierBoards.add(startBoard.deepCopy());
        }

        return frontierBoards;
    }

    //helper class for BFS node state
    private static class BFSNode {
        CubeSudokuBoard board;
        int cellIndex;
        int depth;
        int lastFace0ProgressDepth;

        BFSNode(CubeSudokuBoard board, int cellIndex, int depth) {
            this.board = board;
            this.cellIndex = cellIndex;
            this.depth = depth;
            this.lastFace0ProgressDepth = depth;
        }
        BFSNode(CubeSudokuBoard board, int cellIndex, int depth, int lastFace0ProgressDepth) {
            this.board = board;
            this.cellIndex = cellIndex;
            this.depth = depth;
            this.lastFace0ProgressDepth = lastFace0ProgressDepth;
        }
    }

    //convert 0–404 index to (face, row, col)
    private int[] indexToFaceRowCol(int index) {
        int face = index / 81;
        int offset = index % 81;
        int row = offset / 9;
        int col = offset % 9;
        return new int[]{face, row, col};
    }

    //find next empty cell index starting from 'start'
    private int findNextEmptyCellIndex(CubeSudokuBoard board, int start) {
        //look for next empty cell starting from given linear index
        //for loop controls depth of BFS search
        for (int i = start; i < 100; i++) { //anything beyond 110 and it becomes unstable and gets hit with major bottleneck
            int[] frc = indexToFaceRowCol(i);
            if (board.getCell(frc[0], frc[1], frc[2]) == 0) {
                //return index of first empty cell found
                return i;
            }
        }
        //return -1 if no empty cell is found
        return -1;
    }

    //helper to copy a board's cell values into another board
    private void copyBoard(CubeSudokuBoard source, CubeSudokuBoard dest) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    dest.setCell(f, r, c, source.getCell(f, r, c));
                }
            }
        }
    }

    //helper to count filled cells (for sorting promising boards)
    public int countFilledCells(CubeSudokuBoard board) {
        int count = 0;
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCell(f, r, c) != 0) count++;
                }
            }
        }
        return count;
    }

    //helper to count filled cells on a specific face
    private int countFaceFilledCells(CubeSudokuBoard board, int face) {
        int count = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getCell(face, r, c) != 0) count++;
            }
        }
        return count;
    }
}