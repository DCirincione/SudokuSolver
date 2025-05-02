package solver;

import javax.swing.*;
import java.awt.*;
import java.awt.Toolkit;

public class CubeCanvas extends JPanel {

    private final CubeSudokuBoard board; // Leave this final only if board.copyFrom(...) works. If not, make it non-final and reassign in updateBoard.

    public CubeCanvas(CubeSudokuBoard board) {
        this.board = board;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(screenSize.width, screenSize.height));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        Color colorStart = new Color(240, 240, 240); // light grey
        Color colorEnd = new Color(200, 200, 200);   // medium light grey
        GradientPaint gradient = new GradientPaint(0, 0, colorStart, 0, height, colorEnd);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        drawCube(g2d);
    }

    private void drawCube(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);

        int faceSize = 200; // size of one Sudoku face
        int cellSize = faceSize / 9;
        int offsetX = (getWidth() - 1 * faceSize) / 2;
        int offsetY = (getHeight() - 3 * faceSize) / 2;

        drawFace(g2, 0, offsetX, offsetY, cellSize); // Top
        drawFace(g2, 1, offsetX, offsetY + faceSize, cellSize); // Front
        drawFace(g2, 2, offsetX - faceSize, offsetY + faceSize, cellSize); // Left
        drawFace(g2, 3, offsetX + faceSize, offsetY + faceSize, cellSize); // Right
        drawFace(g2, 4, offsetX, offsetY + 2 * faceSize, cellSize); // Bottom
    }

    private void drawFace(Graphics2D g2, int faceIndex, int startX, int startY, int cellSize) {
        // Light background for the Sudoku face area
        Color faceBackground = new Color(230, 230, 230); // subtle light gray
        g2.setColor(faceBackground);
        g2.fillRect(startX, startY, cellSize * 9, cellSize * 9);
        g2.setColor(Color.BLACK); // reset color for drawing grid

        g2.drawRect(startX, startY, cellSize * 9, cellSize * 9);

        Font font = new Font("SansSerif", Font.BOLD, 12);
        g2.setFont(font);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;
                g2.drawRect(x, y, cellSize, cellSize);

                int val = board.getCell(faceIndex, r, c);
                if (val != 0) {
                    // Set color based on whether the number is original or added during solving
                    if (board.isOriginal(faceIndex, r, c)) {
                        g2.setColor(Color.BLACK);
                    } else {
                        g2.setColor(Color.BLUE);
                    }
                    String text = Integer.toString(val);
                    FontMetrics metrics = g2.getFontMetrics(font);
                    int textX = x + (cellSize - metrics.stringWidth(text)) / 2;
                    int textY = y + ((cellSize - metrics.getHeight()) / 2) + metrics.getAscent();
                    g2.drawString(text, textX, textY);
                    // Reset color for borders and other drawing
                    g2.setColor(Color.BLACK);
                }
            }
        }
    }
    /**
     * Updates the board displayed in this canvas and repaints.
     * @param newBoard The new CubeSudokuBoard to display.
     */
    public void updateBoard(CubeSudokuBoard newBoard) {
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    board.setCell(f, r, c, newBoard.getCell(f, r, c));
                }
            }
        }
        repaint();
    }
}