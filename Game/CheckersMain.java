package Game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class CheckersMain {

    public static void main(String[] args) {
        // Initialize JNI handler and data model
        JNIHandler jniHandler = new JNIHandler();
        MyData dataModel = new MyData(jniHandler);
        CheckersBoard checkersBoard = new CheckersBoard();
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(640, 640));

        // Set up the main frame
        JFrame frame = new JFrame("Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        // Create and configure the JTable for displaying the game board
        JTable checkersTable = new JTable();
        checkersTable.setModel(dataModel);
        MyView view = new MyView(jniHandler);
        checkersTable.setDefaultRenderer(Integer.class, view);
        checkersTable.setRowHeight(80);
        checkersTable.setPreferredSize(new Dimension(640, 640));
        checkersTable.setGridColor(new Color(0, 0, 0, 0)); // Invisible grid
        checkersTable.setOpaque(false);

        checkersTable.setFocusable(true);

        // Add mouse listener to handle player clicks on the board
        CheckersMouseListener listener = new CheckersMouseListener(checkersTable, jniHandler, view);
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
        // Pass listener to view for selection tracking
        checkersBoard.setBounds(0,0,640,640);
        checkersTable.setBounds(0,0,640,640);

        // Add the table to the main frame
        layeredPane.add(checkersBoard, JLayeredPane.FRAME_CONTENT_LAYER);
        layeredPane.add(checkersTable, JLayeredPane.MODAL_LAYER);
        frame.setSize(640, 668);
        frame.add(layeredPane, BorderLayout.CENTER);
        frame.setVisible(true);

        // Initialize the game board via JNI
        jniHandler.initializeGame();
    }
}
