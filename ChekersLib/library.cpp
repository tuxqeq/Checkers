#include "Game_JNIHandler.h"
#include <iostream>
#include <vector>
#include <jni.h>

const int BOARD_SIZE = 8;
enum Piece { EMPTY, BLACK, WHITE, BLACK_KING, WHITE_KING };

// Static variables to hold game state
static inline std::vector<std::vector<Piece>> board(BOARD_SIZE, std::vector<Piece>(BOARD_SIZE, EMPTY));
static inline bool pieceSelected = false;
static inline int selectedX = -1, selectedY = -1;
static inline Piece currentTurn = WHITE;  // Track the current player's turn

// Initialize the game board
static inline void initializeBoard() {
    // Place white checkers
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
    currentTurn = WHITE;
}



static inline auto kingMove(int startX, int startY, int endX, int endY) {
    int dx = (endX > startX) ? 1 : -1;
    int dy = (endY > startY) ? 1 : -1;
    std::vector<std::pair<int, int>> result;

    int x = startX + dx;
    int y = startY + dy;

    while (x != endX && y != endY) {
        if (board[x][y] != EMPTY) {
            result.push_back({x, y});
            if (result.size() > 1) return result;
        }
        x += dx;
        y += dy;
    }

    // Valid only if exactly one opponent piece was found
    return result;
}

// Check if the move is valid, allowing backward jumps but restricting regular checkers from moving backward without capturing
static inline bool isValidMove(int startX, int startY, int endX, int endY) {
    Piece piece = board[startX][startY];
    if (piece == EMPTY || board[endX][endY] != EMPTY) return false;
    if(endX > BOARD_SIZE or endY > BOARD_SIZE or endX < 0 or endY < 0) return false;

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
            } else if (middlePiece != EMPTY) {
                return false; // Other pieces blocking path make the move invalid
            }
        }
        if (opponentCount == 1 or opponentCount == 0) {
            return true;
        }

    }
    return false;
}

static inline bool canCaptureAgain(int x, int y) {
    Piece piece = board[x][y];
    std::vector<std::pair<int, int>> directions = { {2, 2}, {2, -2}, {-2, 2}, {-2, -2} };

    for (const auto& direction : directions) {
        int newX = x + direction.first;
        int newY = y + direction.second;
        if (newX >= 0 and newX < BOARD_SIZE and newY >= 0 and newY < BOARD_SIZE and
                (((piece == BLACK || piece == BLACK_KING) and board[x + (direction.first/2)][y + (direction.second/2)] == WHITE
                or board[x + (direction.first/2)][y + (direction.second/2)] == WHITE_KING) ||
                ((piece == WHITE || piece == WHITE_KING) and board[x + (direction.first/2)][y + (direction.second/2)] == BLACK
                or board[x + (direction.first/2)][y + (direction.second/2)] == BLACK_KING))) {
            if (isValidMove(x, y, newX, newY)) {
                return true;
            }
        }
    }
    return false;
}

static inline bool hasCaptureMove() {
    for (int i = 0; i < BOARD_SIZE; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            Piece piece = board[i][j];
            if ((piece == BLACK || piece == BLACK_KING) && currentTurn == BLACK ||
                (piece == WHITE || piece == WHITE_KING) && currentTurn == WHITE) {
                // Check if the piece has a capture move
                if (canCaptureAgain(i, j)) {
                    return true;
                }
            }
        }
    }
    return false;
}

// Capture the opponent's piece
static inline void capturePiece(int startX, int startY, int endX, int endY) {
    auto kingMv= kingMove(startX, startY, endX, endY);
    if (!kingMv.empty()) {
        for (const auto &pos : kingMv) {
            board[pos.first][pos.second] = EMPTY;
        }
    } else {
        // Normal capture for regular pieces
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        board[midX][midY] = EMPTY;
    }
}



// Switch turn after a valid move
static inline void switchTurn() {
    currentTurn = (currentTurn == BLACK) ? WHITE : BLACK;
}

static inline bool canMove(int x, int y) {
    Piece piece = board[x][y];
    if (piece == EMPTY) return false;

    std::vector<std::pair<int, int>> directions = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},  // Regular moves for all pieces
            {2, 2}, {2, -2}, {-2, 2}, {-2, -2}   // Capture moves for all pieces
    };

    // For each direction, check if there is a valid move
    for (const auto& direction : directions) {
        int newX = x + direction.first;
        int newY = y + direction.second;
        if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE) {
            if (isValidMove(x, y, newX, newY)) {
                return true;  // Return true as soon as we find one valid move
            }
        }
    }
    return false;  // No valid moves found
}
// Handle user click, selecting or moving a piece
static inline void handleClick(int x, int y) {
    bool captureAvailable = hasCaptureMove();  // Check if any capture is available for the current player

    if (!pieceSelected) {
        if (board[x][y] != EMPTY) {
            Piece piece = board[x][y];
            // Allow selection if it's the player's turn, has moves, and if captures are available, only allow pieces that can capture
            if (((piece == WHITE || piece == WHITE_KING) && currentTurn == WHITE ||
                 (piece == BLACK || piece == BLACK_KING) && currentTurn == BLACK) &&
                (!captureAvailable || canCaptureAgain(x, y)) &&
                canMove(x, y)) {  // Only allow selecting pieces that can move
                selectedX = x;
                selectedY = y;
                pieceSelected = true;
            }
        }
    } else {
        bool isMoveCapture = (abs(x - selectedX) >= 2 && abs(y - selectedY) >= 2);

        // Allow move only if it's a capture when capture is mandatory
        if ((!captureAvailable || isMoveCapture) && isValidMove(selectedX, selectedY, x, y)) {
            if (isMoveCapture) {
                capturePiece(selectedX, selectedY, x, y);
            }
            board[x][y] = board[selectedX][selectedY];
            board[selectedX][selectedY] = EMPTY;

            // Promote to King if reaching the opposite side
            if ((x == BOARD_SIZE - 1 && board[x][y] == BLACK) || (x == 0 && board[x][y] == WHITE)) {
                board[x][y] = (board[x][y] == BLACK) ? BLACK_KING : WHITE_KING;
            }

            if (isMoveCapture && canCaptureAgain(x, y)) {
                selectedX = x;
                selectedY = y;
            } else {
                pieceSelected = false;
                switchTurn();
            }
        }
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
