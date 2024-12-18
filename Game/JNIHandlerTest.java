package Game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JNIHandlerTest {

    private JNIHandler jniHandler;

    static {
        System.loadLibrary("ChekersLib");
    }

    @BeforeEach
    public void setUp() {
        jniHandler = new JNIHandler();
        jniHandler.initializeGame(); // Set up a fresh game before each test
    }

    @Test
    public void testInitializeGame() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int piece = jniHandler.getPieceAt(i, j);

                if (i < 3 && (i + j) % 2 == 1) {
                    assertEquals(1, piece, "Expected a black piece at position (" + i + ", " + j + ")");
                } else if (i >= 5 && (i + j) % 2 == 1) {
                    assertEquals(2, piece, "Expected a white piece at position (" + i + ", " + j + ")");
                } else {
                    assertEquals(0, piece, "Expected an empty square at position (" + i + ", " + j + ")");
                }
            }
        }
    }

    @Test
    public void testGetCurrentPlayer() {
        assertEquals(1, jniHandler.getCurrentPlayer(), "White should start the game");
    }

    @Test
    public void testPieceSelection() {
        jniHandler.handleClick(5, 2); // Select a white piece
        int[] selectedPiece = jniHandler.getSelectedPiece();
        assertArrayEquals(new int[]{5, 2}, selectedPiece, "Selected piece should match coordinates (5, 1)");
    }

    @Test
    public void testValidMove() {
        jniHandler.handleClick(5, 2); // Select white piece at (5, 1)
        jniHandler.handleClick(4, 3); // Move it to (4, 2)
        assertEquals(2, jniHandler.getPieceAt(4, 3), "Piece should have moved to (4, 2)");
        assertEquals(0, jniHandler.getPieceAt(5, 2), "Old position should be empty");
    }

    @Test
    public void testCaptureMove() {
        jniHandler.handleClick(5, 2); // Select white piece at (5, 1)
        jniHandler.handleClick(4, 3); // Move it to (4, 2)

        jniHandler.handleClick(2, 5); // Select black piece at (2, 3)
        jniHandler.handleClick(3, 4); // Move black piece to (3, 4)

        jniHandler.handleClick(4, 3); // Select white piece at (4, 2)
        jniHandler.handleClick(2, 5); // Capture move over black piece

        assertEquals(2, jniHandler.getPieceAt(2, 5), "White piece should be at new position after capture");
        assertEquals(0, jniHandler.getPieceAt(3, 4), "Captured piece's position should be empty");
    }

    @Test
    public void testSwitchTurns() {
        jniHandler.handleClick(5, 2); // White's move
        jniHandler.handleClick(4, 3);
        assertEquals(0, jniHandler.getCurrentPlayer(), "After white's move, it should be black's turn");

        jniHandler.handleClick(2, 5); // Black's move
        jniHandler.handleClick(3, 4);
        assertEquals(1, jniHandler.getCurrentPlayer(), "After black's move, it should be white's turn");
    }

    @Test
    public void testPieceAtPosition() {
        assertEquals(1, jniHandler.getPieceAt(0, 1), "Expected black piece at (0, 1)");
        assertEquals(1, jniHandler.getPieceAt(2, 1), "Expected black piece at (2, 1)");
        assertEquals(2, jniHandler.getPieceAt(7, 6), "Expected white piece at (7, 6)");
        assertEquals(0, jniHandler.getPieceAt(4, 4), "Expected empty square at (4, 4)");
    }

    @Test
    public void testInvalidMove() {
        jniHandler.handleClick(5, 2); // Select white piece at (5, 1)
        jniHandler.handleClick(6, 2); // Invalid move (wrong direction for white piece)
        assertEquals(2, jniHandler.getPieceAt(5, 2), "Piece should not have moved from (5, 1)");
        assertEquals(0, jniHandler.getPieceAt(6, 2), "Target position (6, 2) should still be empty");
    }

    @Test
    public void testSelection(){
        jniHandler.handleClick(5, 2); // Select white piece at (5, 1)
        jniHandler.handleClick(5, 4); //Trying to select another piece
        assertArrayEquals(new int[]{5, 2}, jniHandler.getSelectedPiece(), "Selected piece should match coordinates (5, 2)");
    }

    @Test
    public void testMultiJump() {
        // Simulate setup for a multi-jump situation
        jniHandler.handleClick(5, 2); // Move white piece
        jniHandler.handleClick(4, 3);

        jniHandler.handleClick(2, 3); // Move black piece to setup capture
        jniHandler.handleClick(3, 4);

        jniHandler.handleClick(6, 1); // Move white piece
        jniHandler.handleClick(5, 2);

        jniHandler.handleClick(2, 5);
        jniHandler.handleClick(3, 6);

        jniHandler.handleClick(4, 3); // White captures the first black piece
        jniHandler.handleClick(2, 5);

        assertEquals(2, jniHandler.getPieceAt(2, 5), "White piece should have jumped to (2, 3)");
        assertEquals(0, jniHandler.getPieceAt(3, 4), "Captured piece at (3, 4) should be empty");
        assertArrayEquals(new int[]{2, 5}, jniHandler.getSelectedPiece(), "Selected piece should match coordinates (2, 5)");

        jniHandler.handleClick(4, 7); // Multi-jump to capture second black piece

        assertEquals(2, jniHandler.getPieceAt(4, 7), "White piece should have jumped to (1, 5)");
        assertEquals(0, jniHandler.getPieceAt(3, 6), "Captured piece at (2, 4) should be empty");
    }



    @Test
    public void testMandatoryCapture() {
        // Set up scenario where a capture is mandatory
        jniHandler.handleClick(5, 2); // Move white piece
        jniHandler.handleClick(4, 3);

        jniHandler.handleClick(2, 5); // Move black piece to be captured
        jniHandler.handleClick(3, 4);

        jniHandler.handleClick(5, 6); // Trying to select another piece

        assertArrayEquals(new int[]{-1, -1}, jniHandler.getSelectedPiece(), "The piece at (5, 6) should not be selected");

        jniHandler.handleClick(4, 3);
        jniHandler.handleClick(2, 5); // Try moving without capture

        assertEquals(2, jniHandler.getPieceAt(2, 5), "White piece should stay at (4, 2) since capture is mandatory");
    }

    @Test
    public void testWhitePromotionToKing() {
        int[][] moves = {
                {5, 4}, {4, 5}, {2, 7}, {3, 6},
                {4, 5}, {2, 7}, {2, 5}, {3, 4},
                {5, 2}, {4, 1}, {1, 4}, {2, 5},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {2, 7}, {0, 5}
        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(4, jniHandler.getPieceAt(0, 5), "White piece should be promoted to White King at (0, 5)");
    }

    @Test
    public void testWhiteKingCheckingOver2() {
        int[][] moves = {
                {5, 4}, {4, 5}, {2, 7}, {3, 6},
                {4, 5}, {2, 7}, {2, 5}, {3, 4},
                {5, 2}, {4, 1}, {1, 4}, {2, 5},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {2, 7}, {0, 5}, {2, 5}, {3, 6},
                {0,5}, {3, 2}
        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(4, jniHandler.getPieceAt(0, 5), "White King should still be at (0, 5)");
    }

    @Test
    public void testWhiteKingChecking() {
        int[][] moves = {
                {5, 4}, {4, 5}, {2, 7}, {3, 6},
                {4, 5}, {2, 7}, {2, 5}, {3, 4},
                {5, 2}, {4, 1}, {1, 4}, {2, 5},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {2, 7}, {0, 5}, {2, 5}, {3, 6},
                {7, 4}, {6, 3}, {1, 4}, {2, 5},
                {0, 5}, {3, 2}

        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(4, jniHandler.getPieceAt(3, 2), "White King should be at (3, 2)");
        assertEquals(0, jniHandler.getPieceAt(2, 3), "White king checked the black piece at (2, 3)");
    }

    @Test
    public void testBlackPromotionToKing() {
        int[][] moves = {
                {5, 6}, {4, 7}, {2, 3}, {3, 2},
                {5, 0}, {4, 1}, {3, 2}, {5, 0},
                {5, 2}, {4, 3}, {1, 4}, {2, 3},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {7, 2}, {6, 3}, {5, 0}, {7, 2}
        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(3, jniHandler.getPieceAt(7, 2), "Black piece should be promoted to Black King at (7, 2)");
    }

    @Test
    public void testBlackKingCheckingOver2() {
        int[][] moves = {
                {5, 6}, {4, 7}, {2, 3}, {3, 2},
                {5, 0}, {4, 1}, {3, 2}, {5, 0},
                {5, 2}, {4, 3}, {1, 4}, {2, 3},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {7, 2}, {6, 3}, {5, 0}, {7, 2},
                {5, 2}, {4, 1}, {7, 2}, {4, 5}
        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(3, jniHandler.getPieceAt(7, 2), "Black King should still be at (7, 2)");
    }

    @Test
    public void testBlackKingChecking() {
        int[][] moves = {
                {5, 6}, {4, 7}, {2, 3}, {3, 2},
                {5, 0}, {4, 1}, {3, 2}, {5, 0},
                {5, 2}, {4, 3}, {1, 4}, {2, 3},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {7, 2}, {6, 3}, {5, 0}, {7, 2},
                {5, 2}, {4, 1}, {2, 1}, {3, 0},
                {6, 3}, {5, 2}, {7, 2}, {3, 6}

        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(3, jniHandler.getPieceAt(3, 6), "Black King should be at (3, 6)");
        assertEquals(0, jniHandler.getPieceAt(5, 4), "Black king checked the white piece at (5, 4)");
    }

    @Test
    public void testWhiteWin(){
        int[][] moves = {
                {5, 6}, {4, 7}, {2, 3}, {3, 4},
                {6, 7}, {5, 6}, {2, 1}, {3, 2},
                {5, 2}, {4, 1}, {1, 2}, {1, 2},
                {2, 5}, {2, 5}, {1, 2}, {2, 5},
                {2, 1}, {4, 1}, {2, 3}, {4, 5},
                {5, 0}, {5, 0}, {1, 4}, {2, 3},
                {5, 0}, {4, 1}, {2, 5}, {3, 4},
                {6, 3}, {5, 2}, {0, 5}, {1, 4},
                {5, 4}, {5, 4}, {7, 4}, {4, 3},
                {2, 3}, {3, 2}, {4, 1}, {2, 3},
                {0, 5}, {2, 7}, {3, 6}, {4, 7},
                {2, 5}, {0, 3}, {1, 4}, {2, 5},
                {0, 3}, {2, 1}, {3, 2}, {4, 5},
                {2, 3}, {4, 1}, {0, 1}, {1, 2},
                {0, 3}, {3, 0}, {1, 0}, {2, 1},
                {3, 0}, {1, 2}, {1, 6}, {2, 5},
                {0, 5}, {2, 7}, {0, 7}, {1, 6},
                {2, 7}, {0, 5}, {2, 5}, {3, 4},
                {4, 3}, {2, 5}
        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(0, jniHandler.getWinner(), "White should be the winner");
    }

    @Test
    public void testBlackWin(){
        int[][] moves = {
                {5, 6}, {4, 7}, {2, 3}, {3, 2},
                {5, 0}, {4, 1}, {3, 2}, {5, 0},
                {5, 2}, {4, 3}, {2, 1}, {3, 0},
                {6, 3}, {5, 2}, {4, 7}, {1, 2},
                {2, 3}, {7, 2}, {6, 3}, {1, 0},
                {5, 0}, {7, 2}, {5, 2}, {4, 1},
                {3, 0}, {5, 2}, {3, 4}, {5, 4},
                {4, 5}, {7, 2}, {5, 4}, {3, 6},
                {6, 5}, {5, 4}, {3, 6}, {7, 2},
                {4, 7}, {3, 6}, {2, 5}, {4, 7},
                {6, 7}, {5, 6}, {4, 7}, {6, 5},
                {7, 6}, {5, 4}, {7, 2}, {4, 5},
                {7, 0}, {6, 1}, {3, 4}, {4, 3},
                {7, 4}, {6, 3}, {4, 5}, {7, 2},
                {5, 0}
        };
        for (int[] move : moves) {
            jniHandler.handleClick(move[0], move[1]);
        }

        assertEquals(1, jniHandler.getWinner(), "White should be the winner");
    }


}