package Game;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class MyView extends JPanel implements TableCellRenderer {
    private int cellValue;
    //private int selectedRow = -1;
    //private int selectedCol = -1;
    private JNIHandler jni;
    private boolean isSelected = false;
    //private CheckersMouseListener mouseListener;

    public MyView(JNIHandler jni) {
        this.jni = jni;
        setOpaque(false);
    }

    // Set the mouse listener to get the selected cell

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Integer) {
            cellValue = (Integer) value;
        }

        //System.out.println(Arrays.toString(jni.getSelectedPiece()) + " " + row + " " + column);
        //System.out.println();
        if ((jni.getSelectedPiece()[0] == row && jni.getSelectedPiece()[1] == column))this.isSelected = true;
        else this.isSelected = false;
        //System.out.println(isSelected);

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw pieces based on cell value
        switch (cellValue) {
            case 1: // Black piece
                if (isSelected) {
                    g.setColor(new Color(50, 0, 0, 255));
                    g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                    isSelected = false;
                }else {
                    g.setColor(Color.BLACK);
                    g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                }
                break;
            case 2:// White piece
                if (isSelected) {
                    g.setColor(new Color(127, 100, 100, 255));
                    g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                    isSelected = false;
                } else {
                g.setColor(Color.WHITE);
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                }
                break;
            case 3: // Black king piece
                g.setColor(Color.BLACK);
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                g.setColor(Color.YELLOW);
                g.drawOval(10, 10, getWidth() - 20, getHeight() - 20);
                break;
            case 4: // white_king piece
                g.setColor(Color.WHITE);
                g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
                g.setColor(Color.YELLOW);
                g.drawOval(10, 10, getWidth() - 20, getHeight() - 20);
                break;
            default:
                // No piece
                break;
        }
    }
}

