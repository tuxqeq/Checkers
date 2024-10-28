import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class Checkers extends JPanel {

    static {
        System.loadLibrary("untitled");
    }

    private native void initializeGame();
    private native int[][] getBoardState();
    private native void handleClick(int x, int y);
    private native int[] getSelectedPiece();
    //private native int[] getValidMoves();


    public Checkers() {
        initializeGame();
        setPreferredSize(new Dimension(640, 640));
        setOpaque(false);
        setFocusable(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int boardX = (7 - e.getY() / 80);
                int boardY = (e.getX() / 80);
                handleClick(boardX, boardY);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int[][] boardState = getBoardState();
        int[] selectedPiece = getSelectedPiece();

        drawBoard(g, boardState, selectedPiece);
    }

    private void drawBoard(Graphics g, int[][] boardState, int[] selectedPiece) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boolean isSelected = (selectedPiece[0] == i && selectedPiece[1] == j);
                drawCell(g, 7 - i, j, boardState[i][j], isSelected);
            }
        }
    }

    private void drawCell(Graphics g, int row, int col, int piece, boolean isSelected) {
        int x = col;
        int y = row;

        if ((x + y) % 2 == 0) {
            g.setColor(Color.GRAY);
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRect(x * 80, y * 80, 80, 80);

        if (piece == 1) {
            if (isSelected) {
                g.setColor(new Color(127, 100, 100, 255));
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillOval(x * 80 + 10, y * 80 + 10, 60, 60);
        } else if (piece == 2) {
            if (isSelected) {
                g.setColor(new Color(50, 0, 0, 255));
            } else {
                g.setColor(Color.BLACK);
            }
            g.fillOval(x * 80 + 10, y * 80 + 10, 60, 60);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Checkers");
        Checkers checkersPanel = new Checkers();
        frame.add(checkersPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}