package solver;

import javax.swing.*;
import java.awt.*;

public class CubeSudokuGUI extends JFrame {
    private final JLabel[][][] cells; // cells[face][row][col]
    private static final String[] faceNames = {"Top", "Front", "Left", "Right", "Bottom"};

    public CubeSudokuGUI() {
        setTitle("Cube Sudoku Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        cells = new JLabel[5][9][9];

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        Font font = new Font("SansSerif", Font.BOLD, 16);

        // Initialize and arrange faces in "cube net" layout
        // Top face
        JPanel topFace = createFacePanel(0, font);
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(topFace, gbc);

        // Left, Front, Right faces
        JPanel leftFace = createFacePanel(2, font);
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(leftFace, gbc);

        JPanel frontFace = createFacePanel(1, font);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(frontFace, gbc);

        JPanel rightFace = createFacePanel(3, font);
        gbc.gridx = 2;
        gbc.gridy = 1;
        mainPanel.add(rightFace, gbc);

        // Bottom face
        JPanel bottomFace = createFacePanel(4, font);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(bottomFace, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }


    private JPanel createFacePanel(int faceIndex, Font font) {
        JPanel panel = new JPanel(new GridLayout(9, 9));
        panel.setBorder(BorderFactory.createTitledBorder(faceNames[faceIndex]));
        panel.setPreferredSize(new Dimension(300, 300)); // Set size for each face
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(font);
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[faceIndex][r][c] = label;
                panel.add(label);
            }
        }
        return panel;
    }

    public void updateCell(int face, int row, int col, int value, boolean isFixed) {
        if (face < 0 || face >= 5 || row < 0 || row >= 9 || col < 0 || col >= 9) return;
        SwingUtilities.invokeLater(() -> {
            cells[face][row][col].setText(value == 0 ? "" : String.valueOf(value));
            cells[face][row][col].setForeground(isFixed ? Color.BLACK : Color.BLUE);
        });
    }
}
