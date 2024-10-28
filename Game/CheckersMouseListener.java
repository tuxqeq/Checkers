package Game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;

public class CheckersMouseListener extends MouseAdapter {
    private JTable jTable;
    private JNIHandler jni;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public CheckersMouseListener(JTable jTable, JNIHandler jni) {
        this.jTable = jTable;
        this.jni = jni;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int row = e.getY() / jTable.getRowHeight();
        int col = e.getX() / jTable.getColumnModel().getColumn(0).getWidth();

        selectedRow = row;
        selectedCol = col;

        jni.handleClick(row, col);

        jTable.repaint(); //TODO event handling
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }
}
