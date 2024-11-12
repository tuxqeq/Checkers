package Game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class Controller extends MouseAdapter implements KeyListener {
    private JTable jTable;
    private JNIHandler jni;
    private int selectedRow = 0;
    private int selectedCol = 0;
    private boolean keyboardMode = false;
    private MyView view;
    private BoardChangeListener boardChangeListener;

    public Controller(JTable jTable, JNIHandler jni, MyView view, BoardChangeListener boardChangeListener) {
        this.jTable = jTable;
        this.jni = jni;
        this.view = view;
        this.boardChangeListener = boardChangeListener;
    }

    public void toggleModeMouse() {
        if (keyboardMode) {
            keyboardMode = !keyboardMode;
            System.out.println("Mode switched to " + "Mouse");
            view.setSelectedCell(-1, -1);
            notifyBoardChange();
        }
    }

    public void toggleMode() {
        keyboardMode = !keyboardMode;
        view.setSelectedCell(-1, -1);
        if (keyboardMode) {
            selectedCol = selectedRow = 0;
            view.setSelectedCell(selectedRow, selectedCol);
            notifyBoardChange();
        }
        System.out.println("Mode switched to " + (keyboardMode ? "Keyboard" : "Mouse"));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (keyboardMode) return;

        int row = e.getY() / jTable.getRowHeight();
        int col = e.getX() / jTable.getColumnModel().getColumn(0).getWidth();

        jni.handleClick(row, col);
        checkWinner();
        notifyBoardChange();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!keyboardMode) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectedRow = Math.max(0, selectedRow - 1);
                break;
            case KeyEvent.VK_DOWN:
                selectedRow = Math.min(jTable.getRowCount() - 1, selectedRow + 1);
                break;
            case KeyEvent.VK_LEFT:
                selectedCol = Math.max(0, selectedCol - 1);
                break;
            case KeyEvent.VK_RIGHT:
                selectedCol = Math.min(jTable.getColumnCount() - 1, selectedCol + 1);
                break;
            case KeyEvent.VK_ENTER:
                jni.handleClick(selectedRow, selectedCol);
                checkWinner();
                break;
        }
        view.setSelectedCell(selectedRow, selectedCol);
        notifyBoardChange();
    }

    private void checkWinner() {
        if (jni.getWinner() != -1) {
            JOptionPane.showMessageDialog(null, "Winner is " + (jni.getWinner() == 0 ? "White" : "Black"));
            jni.initializeGame();
            notifyBoardChange();
        }
    }

    private void notifyBoardChange() {
        if (boardChangeListener != null) {
            boardChangeListener.onBoardChanged();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}