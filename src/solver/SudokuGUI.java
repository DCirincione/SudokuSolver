//imports
package solver;
import javax.swing.*;
import java.awt.*;

public class SudokuGUI extends JFrame {
    private final JLabel[][] cells = new JLabel[9][9];

    public SudokuGUI(String title, int x, int y) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocation(x, y); // position the window on screen
        setLayout(new GridLayout(9, 9));

        Font font = new Font("SansSerif", Font.BOLD, 24);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(font);
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[r][c] = label;
                add(label);
            }
        }

        setVisible(true);
    }

    public void updateCell(int row, int col, int value, boolean isFixed) {
        SwingUtilities.invokeLater(() -> {
            cells[row][col].setText(value == 0 ? "" : String.valueOf(value));
            cells[row][col].setForeground(isFixed ? Color.BLACK : Color.BLUE);
        });
    }

    public void loadInitialBoard(int[][] board) {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                updateCell(r, c, board[r][c], board[r][c] != 0);
    }
}