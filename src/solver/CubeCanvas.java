package solver;

import javax.swing.*;
import java.awt.*;

public class CubeCanvas extends JPanel {

    private final CubeSudokuBoard board; // Leave this final only if board.copyFrom(...) works. If not, make it non-final and reassign in updateBoard.

    public CubeCanvas(CubeSudokuBoard board) {
        this.board = board;
        setPreferredSize(new Dimension(800, 800));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCube(g);
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
        g2.setColor(Color.BLACK);
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