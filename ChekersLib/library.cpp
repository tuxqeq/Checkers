#include "Game_JNIHandler.h"
#include <iostream>
#include <vector>
#include <jni.h>

const int BOARD_SIZE = 8;
enum Piece { EMPTY, BLACK, WHITE, BLACK_KING, WHITE_KING };

static inline std::vector<std::vector<Piece>> board(BOARD_SIZE, std::vector<Piece>(BOARD_SIZE, EMPTY));
static inline bool pieceSelected = false;
static inline int selectedX = -1, selectedY = -1;
static inline Piece currentTurn = WHITE;
static inline std::vector<std::pair<int, int>> lastMoveChanges;


static inline void initializeBoard() {
    for (int i = 0; i < BOARD_SIZE; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            board[i][j] = EMPTY;
        }
    }
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            if ((i + j) % 2 == 1) {
                lastMoveChanges.push_back({i, j});
                board[i][j] = BLACK;
            }
        }
    }
    for (int i = 5; i < BOARD_SIZE; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            if ((i + j) % 2 == 1) {
                lastMoveChanges.push_back({i, j});
                board[i][j] = WHITE;
            }
        }
    }
    currentTurn = WHITE;
    selectedX = -1;
    selectedY = -1;
    pieceSelected = false;
}


static inline int checkWinner(){
    int blackCount = 0;
    int whiteCount = 0;
    for (int i = 0; i < BOARD_SIZE; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            if (board[i][j] == BLACK or board[i][j] == BLACK_KING) blackCount++;
            if (board[i][j] == WHITE or board[i][j] == WHITE_KING) whiteCount++;
        }
    }
    if (blackCount == 0) return 0;
    if (whiteCount == 0) return 1;
    return -1;
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

    return result;
}

static inline bool isValidMove(int startX, int startY, int endX, int endY) {
    Piece piece = board[startX][startY];
    if (piece == EMPTY || board[endX][endY] != EMPTY) return false;
    if(endX > BOARD_SIZE or endY > BOARD_SIZE or endX < 0 or endY < 0) return false;

    if ((piece == BLACK || piece == BLACK_KING) && currentTurn != BLACK) return false;
    if ((piece == WHITE || piece == WHITE_KING) && currentTurn != WHITE) return false;

    int dx = endX - startX;
    int dy = endY - startY;

    if ((piece == WHITE && board[endX][endY] == EMPTY) && dx == -1 && abs(dy) == 1) return true;
    if ((piece == BLACK && board[endX][endY] == EMPTY) && dx == 1 && abs(dy) == 1) return true;

    bool isJump = (abs(dx) == 2 && abs(dy) == 2) && (piece == BLACK or piece == WHITE);

    if (isJump) {
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        Piece middlePiece = board[midX][midY];

        if ((piece == BLACK) && (middlePiece == WHITE || middlePiece == WHITE_KING)) return true;
        if ((piece == WHITE) && (middlePiece == BLACK || middlePiece == BLACK_KING)) return true;
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
                return false;
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
        if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE &&
                (((piece == BLACK || piece == BLACK_KING) && board[x + (direction.first/2)][y + (direction.second/2)] == WHITE
                || board[x + (direction.first/2)][y + (direction.second/2)] == WHITE_KING) ||
                ((piece == WHITE || piece == WHITE_KING) && board[x + (direction.first/2)][y + (direction.second/2)] == BLACK
                || board[x + (direction.first/2)][y + (direction.second/2)] == BLACK_KING))) {
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
                if (canCaptureAgain(i, j)) {
                    return true;
                }
            }
        }
    }
    return false;
}

static inline void capturePiece(int startX, int startY, int endX, int endY) {
    auto kingMv= kingMove(startX, startY, endX, endY);
    if (!kingMv.empty()) {
        for (const auto &pos : kingMv) {
            board[pos.first][pos.second] = EMPTY;
        }
    } else {
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        board[midX][midY] = EMPTY;
    }
}

static inline void switchTurn() {
    currentTurn = (currentTurn == BLACK) ? WHITE : BLACK;
}

static inline bool canMove(int x, int y) {
    Piece piece = board[x][y];
    if (piece == EMPTY) return false;

    std::vector<std::pair<int, int>> directions = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 2}, {2, -2}, {-2, 2}, {-2, -2}
    };


    for (const auto& direction : directions) {
        int newX = x + direction.first;
        int newY = y + direction.second;
        if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE) {
            if (isValidMove(x, y, newX, newY)) {
                return true;
            }
        }
    }
    return false;
}

static inline void handleClick(int x, int y) {
    lastMoveChanges.clear();  // Clear previous move changes

    bool captureAvailable = hasCaptureMove();

    if (!pieceSelected) {
        if (board[x][y] != EMPTY) {
            Piece piece = board[x][y];
            if (((piece == WHITE || piece == WHITE_KING) && currentTurn == WHITE ||
                 (piece == BLACK || piece == BLACK_KING) && currentTurn == BLACK) &&
                (!captureAvailable || canCaptureAgain(x, y)) &&
                canMove(x, y)) {
                selectedX = x;
                selectedY = y;
                pieceSelected = true;
            }
        }
    } else {
        bool isMoveCapture = (abs(x - selectedX) >= 2 && abs(y - selectedY) >= 2);

        if ((!captureAvailable || isMoveCapture) && isValidMove(selectedX, selectedY, x, y)) {
            lastMoveChanges.push_back({selectedX, selectedY});  // Track the start position
            if (isMoveCapture) {
                capturePiece(selectedX, selectedY, x, y);

                // Track each captured piece in `lastMoveChanges`
                auto kingMv = kingMove(selectedX, selectedY, x, y);
                if (!kingMv.empty()) {
                    for (const auto &pos : kingMv) {
                        lastMoveChanges.push_back(pos);  // Track each captured position
                    }
                } else {
                    int midX = (selectedX + x) / 2;
                    int midY = (selectedY + y) / 2;
                    lastMoveChanges.push_back({midX, midY});
                }
            }
            lastMoveChanges.push_back({x, y});  // Track the end position

            board[x][y] = board[selectedX][selectedY];
            board[selectedX][selectedY] = EMPTY;

            // Check if the piece should be promoted to king
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

static inline int getPieceAt(int x, int y) {
    return static_cast<int>(board[x][y]);
}

JNIEXPORT void JNICALL Java_Game_JNIHandler_initializeGame(JNIEnv *env, jobject obj) {
    initializeBoard();
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

JNIEXPORT jint JNICALL Java_Game_JNIHandler_getCurrentPlayer(JNIEnv *env, jobject obj){
    jint result = (currentTurn == BLACK) ? 0 : 1;
    return result;
}

JNIEXPORT jint JNICALL Java_Game_JNIHandler_getWinner(JNIEnv *env, jobject obj){
    jint result = checkWinner();
    return result;
}
