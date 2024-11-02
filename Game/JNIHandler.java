package Game;

public class JNIHandler {
    static { System.loadLibrary("ChekersLib"); }

    public native void initializeGame();
    public native int[] getSelectedPiece();
    public native void handleClick(int x, int y);
    public native int getPieceAt(int x, int y);
    public native int getCurrentPlayer();
    public native int getWinner();
}
