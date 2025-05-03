//This file only contains GUI for old 2D model.
//Go to CubeSudokuGUI for 3D GUI changes

//imports
package solver;
import javax.swing.*;
import java.awt.*;

public class SudokuGUI extends JFrame {
    private final JLabel[][] cells = new JLabel[9][9]; //each cell in 9x9 sudoku grid

    public SudokuGUI(String title, int x, int y) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocation(x, y); //place window at specified screen coordinates
        setLayout(new GridLayout(9, 9)); //use grid layout to mimic sudoku board

        Font font = new Font("SansSerif", Font.BOLD, 24);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(font);
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK)); //draw black border around each cell
                cells[r][c] = label; //store label in grid for updates
                add(label);
            }
        }

        setVisible(true); //display window
    }

    public void updateCell(int row, int col, int value, boolean isFixed) {
        SwingUtilities.invokeLater(() -> {
            cells[row][col].setText(value == 0 ? "" : String.valueOf(value)); //show number unless it's 0 (empty cell)
            cells[row][col].setForeground(isFixed ? Color.BLACK : Color.BLUE); //fixed cells are black, others are blue
        });
    }

    public void loadInitialBoard(int[][] board) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                updateCell(r, c, board[r][c], board[r][c] != 0); //mark nonzero cells as fixed
    }
}