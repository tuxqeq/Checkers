#include "Game_JNIHandler.h"
#include <iostream>
#include <vector>
#include <jni.h>

const int BOARD_SIZE = 8;
//RED = BLACK, BLACK = WHITE
enum Piece { EMPTY, BLACK, WHITE, BLACK_KING, WHITE_KING };

// Static variables to hold game state
static inline std::vector<std::vector<Piece>> board(BOARD_SIZE, std::vector<Piece>(BOARD_SIZE, EMPTY));
static inline bool pieceSelected = false;
static inline int selectedX = -1, selectedY = -1;
static inline Piece currentTurn = WHITE;  // Track the current player's turn

// Initialize the game board
static inline void initializeBoard() {
    // Place red pieces (white checkers)
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            if ((i + j) % 2 == 1) {
                board[i][j] = BLACK;
            }
        }
    }
    // Place black pieces
    for (int i = 5; i < BOARD_SIZE; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            if ((i + j) % 2 == 1) {
                board[i][j] = WHITE;
            }
        }
    }
    board[5][4] = WHITE_KING;
    currentTurn = WHITE;
}

static inline auto kingMove(int startX, int startY, int endX, int endY){
    int dx = endX - startX;
    int dy = endY - startY;
    std::vector<std::pair<int, int>> result;
    int x = startX;
    int y = startY;
    while (x != endX and y != endY){
        if(board[x][y] != EMPTY){
            result.push_back(std::make_pair(x, y));
        }
        if (dx > 0 and dy > 0) {
            ++x;
            ++y;
        }else if (dx > 0 and dy < 0) {
            ++x;
            --y;
        }else if (dx < 0 and dy > 0) {
            --x;
            ++y;
        }else if (dx < 0 and dy < 0) {
            --x;
            --y;
        }
    }
    return result;
}
// Check if the move is valid, allowing backward jumps but restricting regular checkers from moving backward without capturing
static inline bool isValidMove(int startX, int startY, int endX, int endY) {
    Piece piece = board[startX][startY];
    if (piece == EMPTY || board[endX][endY] != EMPTY) return false;

    // Ensure the move is made by the correct player
    if ((piece == BLACK || piece == BLACK_KING) and currentTurn != BLACK) return false;
    if ((piece == WHITE || piece == WHITE_KING) and currentTurn != WHITE) return false;

    int dx = endX - startX;
    int dy = endY - startY;

    if ((piece == WHITE and board[endX][endY] == EMPTY) and dx == -1 and abs(dy) == 1) return true;
    if ((piece == BLACK and board[endX][endY] == EMPTY) and dx == 1 and abs(dy) == 1) return true;
    // Check if this is a capturing (jump) move
    bool isJump = (abs(dx) == 2 and abs(dy) == 2) and (piece == BLACK or piece == WHITE);

    // Capture move (two cells diagonally with opponent's piece in between)
    if (isJump) {
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        Piece middlePiece = board[midX][midY];

        // Check if the middle piece is an opponent's piece
        if ((piece == BLACK) and (middlePiece == WHITE || middlePiece == WHITE_KING)) return true;
        if ((piece == WHITE) and (middlePiece == BLACK || middlePiece == BLACK_KING)) return true;
    }
    auto kingMv = kingMove(startX, startY, endX, endY);

    if ((piece == BLACK_KING || piece == WHITE_KING) && abs(dx) == abs(dy)) {
        auto kingMv = kingMove(startX, startY, endX, endY);
        int opponentCount = 0;
        for (const auto &pos : kingMv) {
            Piece middlePiece = board[pos.first][pos.second];
            if ((piece == BLACK_KING && (middlePiece == WHITE || middlePiece == WHITE_KING)) ||
                (piece == WHITE_KING && (middlePiece == BLACK || middlePiece == BLACK_KING))) {
                opponentCount++;
            }
        }
        return opponentCount == 1;
    }

    return false;
}


// Capture the opponent's piece
static inline void capturePiece(int startX, int startY, int endX, int endY) {
    auto kingMv = kingMove(startX, startY, endX, endY);
    if (board[startX][startY] == BLACK or board[startX][startY] == WHITE){
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        board[midX][midY] = EMPTY;
    }
    if ((board[startX][startY] == BLACK_KING || board[startX][startY] == WHITE_KING) && !kingMv.empty()) {
        for (const auto &pos : kingMv) {
            board[pos.first][pos.second] = EMPTY;
        }
    } // Remove the captured piece
}

// Switch turn after a valid move
static inline void switchTurn() {
    currentTurn = (currentTurn == BLACK) ? WHITE : BLACK;
}

// Handle user click, selecting or moving a piece
static inline void handleClick(int x, int y) {
    if (!pieceSelected) {
        if (board[x][y] != EMPTY) {
            Piece piece = board[x][y];
            // Only allow selecting pieces that belong to the current player
            if ((piece == WHITE || piece == WHITE_KING) && currentTurn == WHITE) {
                selectedX = x;
                selectedY = y;
                pieceSelected = true;
            }
            else if ((piece == BLACK || piece == BLACK_KING) && currentTurn == BLACK) {
                selectedX = x;
                selectedY = y;
                pieceSelected = true;
            }
        }
    } else {
        if (isValidMove(selectedX, selectedY, x, y)) {
            if (abs(x - selectedX) == 2 && abs(y - selectedY) == 2) {
                capturePiece(selectedX, selectedY, x, y);  // Capture move
            }
            board[x][y] = board[selectedX][selectedY];
            board[selectedX][selectedY] = EMPTY;

            // Promote to king if reaching the opposite side
            if ((x == BOARD_SIZE-1 && board[x][y] == BLACK) || (x == 0 && board[x][y] == WHITE)) {
                board[x][y] = (board[x][y] == BLACK) ? BLACK_KING : WHITE_KING;
            }

            // Switch turns after a successful move
            switchTurn();
        }
        pieceSelected = false;
    }
}

// Get the piece at a specific cell
static inline int getPieceAt(int x, int y) {
    return static_cast<int>(board[x][y]);
}

JNIEXPORT void JNICALL Java_Game_JNIHandler_initializeGame(JNIEnv *env, jobject obj) {
    initializeBoard();  // Initialize the game board
}

JNIEXPORT jintArray JNICALL Java_Game_JNIHandler_getSelectedPiece(JNIEnv *env, jobject obj) {
    jintArray result = env->NewIntArray(2);

    if (pieceSelected) {
        jint selected[2] = { selectedX, selectedY };
        env->SetIntArrayRegion(result, 0, 2, selected);
    } else {
        jint selected[2] = { -1, -1 };
        env->SetIntArrayRegion(result, 0, 2, selected);
    }
    return result;
}

JNIEXPORT void JNICALL Java_Game_JNIHandler_handleClick(JNIEnv *env, jobject obj, jint x, jint y) {
    handleClick(x, y);
}

JNIEXPORT jint JNICALL Java_Game_JNIHandler_getPieceAt(JNIEnv *env, jobject obj, jint x, jint y) {
    return getPieceAt(x, y);
}
