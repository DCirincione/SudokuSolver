package solver;

import javax.swing.*;
import java.awt.*;

public class CubeSudokuGUI extends JFrame { //displays a 3d cube sudoku using labeled panels for each face
    private final JLabel[][][] cells; //stores label references for each face, row, and column cell
    private static final String[] faceNames = {"Top", "Front", "Left", "Right", "Bottom"}; //names used for panel borders

    public CubeSudokuGUI() {
        setTitle("Cube Sudoku Solver"); //set window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //allow window to close application
        setLayout(new BorderLayout()); //use border layout for placing main panel
        cells = new JLabel[5][9][9];

        JPanel mainPanel = new JPanel(); //panel holding all face panels
        mainPanel.setLayout(new GridBagLayout()); //use gridbag for cube face layout
        GridBagConstraints gbc = new GridBagConstraints(); //constraints for placing faces
        gbc.insets = new Insets(10, 10, 10, 10); //spacing between face panels
        gbc.fill = GridBagConstraints.BOTH; //expand panels to fill space
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; //equal horizontal and vertical scaling
        Font font = new Font("SansSerif", Font.BOLD, 16); //shared font for cell labels

        //top face
        JPanel topFace = createFacePanel(0, font);
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(topFace, gbc);

        //left, front, right faces
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

        //bottom face
        JPanel bottomFace = createFacePanel(4, font);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(bottomFace, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true); //show window
    }


    private JPanel createFacePanel(int faceIndex, Font font) {
        JPanel panel = new JPanel(new GridLayout(9, 9)); //9x9 grid for one face
        panel.setBorder(BorderFactory.createTitledBorder(faceNames[faceIndex])); //label with face name
        panel.setPreferredSize(new Dimension(300, 300)); //size for face panel
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JLabel label = new JLabel("", SwingConstants.CENTER); //center-aligned label for cell
                label.setFont(font); //apply shared font
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK)); //black border around each cell
                cells[faceIndex][r][c] = label; //store label reference
                panel.add(label); //add label to panel
            }
        }
        return panel;
    }

    public void updateCell(int face, int row, int col, int value, boolean isFixed) {
        if (face < 0 || face >= 5 || row < 0 || row >= 9 || col < 0 || col >= 9) return;
        SwingUtilities.invokeLater(() -> {
            //update label value and color on GUI thread
            cells[face][row][col].setText(value == 0 ? "" : String.valueOf(value)); //show value or blank
            cells[face][row][col].setForeground(isFixed ? Color.BLACK : Color.BLUE); //black for original, blue for solver
        });
    }
}
