package com.kelseyde.calvin.board;

import com.kelseyde.calvin.movegen.MoveGenerator;

/** A utility class for checking draw conditions. */
public final class Draw {
    private Draw() {
        super();
    }

    /** Checks if the board is a draw due to any of the standard chess rules.
     * @return true if {@link #isThreefoldRepetition} or {@link #isFiftyMoveRule} or {@link #isInsufficientMaterial} or {@link #isStalemate} is true
     */
    public static boolean isDraw(Board board, MoveGenerator moveGenerator) {
        return isThreefoldRepetition(board) || isFiftyMoveRule(board) || isInsufficientMaterial(board) || isStalemate(board, moveGenerator);
    }

    /** Checks if the board can be considered as a draw by an engine.
     * <br>WARNING: This method does not check for a <a href="https://en.wikipedia.org/wiki/Stalemate">stalemate</a> and uses {@link #isDoubleRepetition}
     * and not {@link #isThreefoldRepetition}. So it can't be used to check for a draw in a position in the official game rules.
     * <br>Usually chess programs would check for a draw using {@link #isDraw(Board board, MoveGenerator)}.
     * @return true if {@link #isDoubleRepetition} or {@link #isFiftyMoveRule} or {@link #isInsufficientMaterial} is true
     * @see #isDraw(Board board, MoveGenerator)
     */
    public static boolean isEffectiveDraw(Board board) {
        return isDoubleRepetition(board) || isFiftyMoveRule(board) || isInsufficientMaterial(board);
    }

    /** Checks if the board is a draw due to <a href="https://en.wikipedia.org/wiki/Threefold_repetition">threefold repetition</a>.
     * @return true if the board is a draw due to threefold repetition
     */
    public static boolean isThreefoldRepetition(Board board) {
        int repetitionCount = 0;
        long zobrist = board.getState().getKey();
        BoardState[] states = board.getStates();
        // No need to check the positions after the last half move clock reset as they are not reproducible
        int lastReproductiblePly = board.getPly() - board.getState().getHalfMoveClock();
        for (int i = board.getPly() - 2; i >= lastReproductiblePly; i=i-2) {
            // decrement i by 2 as we can skip the positions where the player to move is not the current one
            if (states[i].getKey() == zobrist) {
                repetitionCount += 1;
            }
            if (repetitionCount >= 2) {
                return true;
            }
        }
        return false;
    }

    /** Checks if the current position was already encountered in the board's history.
     * <br>This is a simplified version of the threefold repetition test often used in engines to speed up the search at the price of some insignificant errors.
     * <br>Example with fen <i>1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5</i>:
     * <br>Here the best move is a mate in 1 (c3c2), the second best move's principal variation is check the king with the rook, opponent move is forced,
     * then move back the rook to its initial position, opponent is forced again, then ... play the best move. It results in a mate in 3.
     * <br>With two repetitions test, this mat is considered as a draw.
     * @return true if the current position was already encountered in the board's history
     */
    public static boolean isDoubleRepetition(Board board) {
        long zobrist = board.getState().getKey();
        BoardState[] states = board.getStates();
        // No need to check the positions after the last half move clock reset as they are not reproducible
        int lastReproductiblePly = board.getPly() - board.getState().getHalfMoveClock();
        for (int i = board.getPly() - 2; i >= lastReproductiblePly; i=i-2) {
            // decrement i by 2 as we can skip the positions where the player to move is not the current one
            if (states[i].getKey() == zobrist) {
                return true;
            }
        }
        return false;
    }

    /** Checks if the current position is a draw due to an <a href="https://en.wikipedia.org/wiki/Insufficient_material">insufficient material</a>
     * (also known as dead) position.
     * @return true if the current position is an insufficient material position
     */
    public static boolean isInsufficientMaterial(Board board) {
        if (board.getPawns() != 0 || board.getRooks() != 0 || board.getQueens() != 0) {
            return false;
        }
        long whitePieces = board.getKnights(true) | board.getBishops(true);
        long blackPieces = board.getKnights(false) |  board.getBishops(false);

        return (Bits.count(whitePieces) == 0 || Bits.count(whitePieces) == 1)
                && (Bits.count(blackPieces) == 0 || Bits.count(blackPieces) == 1);
    }

    /** Checks if the current position is a draw due to the <a href="https://en.wikipedia.org/wiki/Fifty-move_rule">fifty-move rule</a>.
     * @return true if the current position is a draw due to the fifty-move rule
     */
    public static boolean isFiftyMoveRule(Board board) {
        return board.getState().getHalfMoveClock() >= 100;
    }

    /** Checks if the board is a draw due to a <a href="https://en.wikipedia.org/wiki/Stalemate">stalemate</a>.
     * @return true if the board is a draw due to a stalemate
     */
    public static boolean isStalemate(Board board, MoveGenerator moveGenerator) {
        return !moveGenerator.isCheck(board, !board.isWhite()) && moveGenerator.generateMoves(board).isEmpty();
    }
}
