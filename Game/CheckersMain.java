package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class CheckersMain implements BoardChangeListener {

    private JTable checkersTable;
    JNIHandler jniHandler = new JNIHandler();

    public static void main(String[] args) {
        new CheckersMain().startGame();
    }

    public void startGame() {

        MyData dataModel = new MyData(jniHandler);
        CheckersBoard checkersBoard = new CheckersBoard();
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(640, 645));
        JButton button = new JButton("Give Up");

        JFrame frame = new JFrame("Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        checkersTable = new JTable();
        checkersTable.setModel(dataModel);
        MyView view = new MyView(jniHandler);
        checkersTable.setDefaultRenderer(Integer.class, view);
        checkersTable.setRowHeight(80);
        checkersTable.setPreferredSize(new Dimension(640, 640));
        checkersTable.setGridColor(new Color(0, 0, 0, 0));
        checkersTable.setOpaque(false);
        checkersTable.setFocusable(true);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame,
                        (jniHandler.getCurrentPlayer() == 0 ? "Black" : "White") + " gave up\n" +
                                "Winner is " + (jniHandler.getCurrentPlayer() == 0 ? "White" : "Black"));
                jniHandler.initializeGame();
                onBoardChanged(); // Notify repaint after reset
            }
        });

        Controller listener = new Controller(checkersTable, jniHandler, view, this);
        checkersTable.addMouseListener(listener);
        checkersTable.addKeyListener(listener);

        checkersTable.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    listener.toggleMode();
                }
            }
        });

        checkersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listener.toggleModeMouse();
            }
        });

        checkersBoard.setBounds(0, 0, 640, 640);
        checkersTable.setBounds(0, 0, 640, 640);

        layeredPane.add(checkersBoard, JLayeredPane.FRAME_CONTENT_LAYER);
        layeredPane.add(checkersTable, JLayeredPane.MODAL_LAYER);
        frame.setSize(640, 668);
        frame.add(layeredPane, BorderLayout.CENTER);
        frame.add(button, BorderLayout.AFTER_LAST_LINE);
        frame.pack();
        frame.setVisible(true);

        jniHandler.initializeGame();
    }

    @Override
    public void onBoardChanged() {
        checkersTable.repaint();
    }
}