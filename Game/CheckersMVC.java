package Game;

import javax.swing.*;
import java.awt.*;

public class CheckersMVC extends JTable implements RepaintEventListener {
    public JNIHandler jni;
    public JTable jTable;
    public MyData dataModel;

    public CheckersMVC(JNIHandler jni) {
        jni = new JNIHandler();
        dataModel = new MyData(jni);

        jTable = new JTable();
        jTable.setOpaque(false);
        jTable.setModel(dataModel);
        jTable.setDefaultRenderer(Integer.class, new MyView(jni));
        jTable.setPreferredSize(new Dimension(640, 640));
        add(jTable);
        jTable.setGridColor(new Color(0, 0, 0, 0));
        setPreferredSize(new Dimension(640, 640));
        //setFocusable(false);

    }

    @Override
    public void onMouseCLick(RepaintEvent e) {
        jTable.repaint();
    }
}
