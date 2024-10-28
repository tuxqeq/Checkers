package Gpt;

import javax.swing.*;
import java.awt.*;

public class CheckersBoard extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < 640; i+=80) {
            for (int j = 0; j < 640; j+=80) {
                if(((i + j)/80)%2==1){
                    g.setColor(new Color(0,0,0,127));
                    g.fillRect(i, j, 80, 80);
                }
            }
        }
    }
}
