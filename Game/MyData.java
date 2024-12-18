package Game;

import javax.swing.table.AbstractTableModel;

public class MyData extends AbstractTableModel {
    private JNIHandler jni;

    public MyData(JNIHandler jni) {
        this.jni = jni;
    }

    @Override
    public int getRowCount() {
        return 8;
    }

    @Override
    public int getColumnCount() {
        return 8;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return jni.getPieceAt(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
    }
}

