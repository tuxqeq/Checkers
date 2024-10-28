package Gpt;

public class JNIHandler {
    static { System.loadLibrary("ChekersLib"); }

    // Initializes the game board in C++
    public native void initializeGame();

    // Returns the entire board state as a 2D array
    public native int[][] getBoardState();

    // Returns the coordinates of the selected piece, or null if none is selected
    public native int[] getSelectedPiece();

    // Handles a click on the board
    public native void handleClick(int x, int y);

    // Gets the piece at specific coordinates
    public native int getPieceAt(int x, int y);
}
