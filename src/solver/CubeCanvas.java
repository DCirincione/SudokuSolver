/**
 * CubeCanvas is a custom JPanel that renders a 3D-like cube layout
 * representing five faces of a 3D Sudoku puzzle using gradient background.
 * Supports dynamic board updates and distinguishes original vs. filled-in values.
 */

package solver;

import javax.swing.*;
import java.awt.*;
import java.awt.Toolkit;

public class CubeCanvas extends JPanel {

    private final CubeSudokuBoard board; //holds current state of puzzle to be drawn

    public CubeCanvas(CubeSudokuBoard board) {
        this.board = board;
        //set panel size to full screen and background to white
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

        //draw background gradient from light grey to medium grey
        Color colorStart = new Color(240, 240, 240); //light grey
        Color colorEnd = new Color(200, 200, 200);   //medium light grey
        GradientPaint gradient = new GradientPaint(0, 0, colorStart, 0, height, colorEnd);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        //then draw cube layout
        drawCube(g2d);
    }

    private void drawCube(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);

        int faceSize = 200; //size of one Sudoku face
        int cellSize = faceSize / 9;
        int offsetX = (getWidth() - 1 * faceSize) / 2;
        int offsetY = (getHeight() - 3 * faceSize) / 2;

        //draw all five faces of cube in top-down layout (top, front, left, right, bottom)
        drawFace(g2, 0, offsetX, offsetY, cellSize); //Top
        drawFace(g2, 1, offsetX, offsetY + faceSize, cellSize); //Front
        drawFace(g2, 2, offsetX - faceSize, offsetY + faceSize, cellSize); //Left
        drawFace(g2, 3, offsetX + faceSize, offsetY + faceSize, cellSize); //Right
        drawFace(g2, 4, offsetX, offsetY + 2 * faceSize, cellSize); //Bottom
    }

    private void drawFace(Graphics2D g2, int faceIndex, int startX, int startY, int cellSize) {
        //fill background for sudoku face with light gray
        Color faceBackground = new Color(230, 230, 230); //subtle light gray
        g2.setColor(faceBackground);
        g2.fillRect(startX, startY, cellSize * 9, cellSize * 9);
        g2.setColor(Color.BLACK); //reset color for drawing grid

        g2.drawRect(startX, startY, cellSize * 9, cellSize * 9);

        Font font = new Font("SansSerif", Font.BOLD, 12);
        g2.setFont(font);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                //draw individual cell rectangle and fill value if it's non-zero
                //black for original values, blue for solver-filled values
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;
                g2.drawRect(x, y, cellSize, cellSize);

                int val = board.getCell(faceIndex, r, c);
                if (val != 0) {
                    //set color based on whether number is original or added during solving
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
                    //reset color for borders and other drawing
                    g2.setColor(Color.BLACK);
                }
            }
        }
    }
    /**
     * Updates board displayed in this canvas and repaints.
     * @param newBoard new CubeSudokuBoard to display.
     */
    public void updateBoard(CubeSudokuBoard newBoard) {
        //copy all cell values from newBoard to this board
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    board.setCell(f, r, c, newBoard.getCell(f, r, c));
                }
            }
        }
        //copy original status flags from newBoard to this board
        for (int f = 0; f < 5; f++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (newBoard.isOriginal(f, r, c)) {
                        board.markOriginal(f, r, c);
                    } else {
                        board.unmarkOriginal(f, r, c);
                    }
                }
            }
        }
        repaint();
    }
}