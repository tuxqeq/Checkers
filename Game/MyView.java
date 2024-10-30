package Game;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class MyView extends JPanel implements TableCellRenderer {
    private int cellValue;
    private JNIHandler jni;
    private boolean isSelected = false;

    public MyView(JNIHandler jni) {
        this.jni = jni;
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Integer) {
            cellValue = (Integer) value;
        }
        if ((jni.getSelectedPiece()[0] == row && jni.getSelectedPiece()[1] == column)) this.isSelected = true;
        else this.isSelected = false;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw pieces based on cell value
        switch (cellValue) {
            case 1: // Black piece
                    g.setColor(Color.BLACK);
                    g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                break;
            case 2:// White piece
                    g.setColor(Color.WHITE);
                    g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                break;
            case 3: // Black king piece
                g.setColor(Color.YELLOW);
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                g.setColor(Color.BLACK);
                g.fillOval(13, 13, getWidth() - 26, getHeight() - 26);
                break;
            case 4: // White king piece
                g.setColor(new Color(120, 70, 0 , 255));
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                g.setColor(Color.WHITE);
                g.fillOval(13, 13, getWidth() - 26, getHeight() - 26);
                break;
            default:
                // No piece
                break;
        }
        if (isSelected) {
            g.setColor(new Color(150, 0, 0, 100));
            g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
            isSelected = false;
        }

    }
}

