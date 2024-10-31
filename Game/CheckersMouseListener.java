package Game;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;

public class CheckersMouseListener extends MouseAdapter {
    private JTable jTable;
    private JNIHandler jni;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<RepaintEventListener> listeners = new ArrayList<>();

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


        RepaintEvent event = new RepaintEvent(this);
        for (RepaintEventListener listener : listeners) {
            listener.onMouseCLick(event);
        }

        jTable.repaint(); //TODO event handling
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }
}
