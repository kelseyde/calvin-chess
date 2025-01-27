package com.kelseyde.calvin.board;

import com.kelseyde.calvin.movegen.MoveGenerator;

/** A utility class for checking draw conditions. */
public final class Draw {
    private Draw() {
        super();
    }

    /** Checks if the board is a draw due to any of the standard chess rules.
     * @return true if {@link #isThreefoldRepetition} or {@link #isFiftyMoveRule} or {@link #isInsufficientMaterialFIDERule(Board)} or {@link #isStalemate} is true
     */
    public static boolean isDraw(Board board, MoveGenerator moveGenerator) {
        return isThreefoldRepetition(board) || isFiftyMoveRule(board) || isInsufficientMaterialFIDERule(board) || isStalemate(board, moveGenerator);
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
        // Warning, ply may be less than half move clock if initialized with a FEN different from the start one
        int lastReproductiblePly = Math.max(board.getPly() - board.getState().getHalfMoveClock(), 0);
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
        // Warning, ply may be less than half move clock if initialized with a FEN different from the start one
        int lastReproductiblePly = Math.max(board.getPly() - board.getState().getHalfMoveClock(), 0);
        for (int i = board.getPly() - 2; i >= lastReproductiblePly; i=i-2) {
            // decrement i by 2 as we can skip the positions where the player to move is not the current one
            if (states[i].getKey() == zobrist) {
                return true;
            }
        }
        return false;
    }

    /** Checks if the current position where the material is insufficient to force a mate, without the collaboration of the opponent.
     * <br>This includes the FIDE rules (see {@link #isInsufficientMaterialFIDERule(Board board)}) situations and others, for instance
     * when both opponents have bishops of opposite colors, or one have two knights and the other only its king.<br>
     * @return true if the current position is an insufficient material position
     * @see <a href="https://rustic-chess.org/board_functionality/detecting_cant_force_mate.html">How to detect can't force_mate</a>
     */
    public static boolean isInsufficientMaterial(Board board) {
        if (board.getPawns() != 0 || board.getRooks() != 0 || board.getQueens() != 0) {
            // There's at least a rook, a queen or a pawn, it's not an insufficient material position
            return false;
        }
        // There's only light pieces and kings
        final int whiteBishops = Bits.count(board.getBishops(true));
        final int blackBishops = Bits.count(board.getBishops(false));
        final int whiteKnights = Bits.count(board.getKnights(true));
        final int blackKnights = Bits.count(board.getKnights(false));
        final int whiteLightPieces = whiteBishops + whiteKnights;
        final int blackLightPieces = blackBishops + blackKnights;
        if (whiteLightPieces <= 1 && blackLightPieces <= 1) {
            // None have two light pieces, it's an insufficient material position (typically, knight vs knight is considered as an insufficient material position)
            return true;
        }
        // There's at least one player with two or more light pieces
        // First check if we are in a bishops vs lonely king situation
        if (whiteKnights + blackKnights == 0 && (whiteBishops==0 || blackBishops==0)) {
            // None have knights one have all bishops the other has a king
            final long bishops = board.getBishops();
            final long white = bishops & Square.WHITE;
            // This is a draw if and only if all bishops are on same color cells
            return bishops==white || white==0;
        }
        // There's at least one knight on the board and a player has two light pieces,
        // If there's more than two light pieces, it's not an insufficient material position.
        if (whiteLightPieces + blackLightPieces > 2) {
            // There's more than two light pieces and at least one knight
            return false;
        }
        // One player has two light pieces, the other has none
        // Two knights vs lonely king is considered as an insufficient material position, knight+bishop vs lonely king is not
        return (whiteLightPieces>0 && whiteBishops==0) || (blackLightPieces>0 && blackBishops==0);
    }

    /** Checks if the current position is a draw due to the <a href="https://en.wikipedia.org/wiki/Insufficient_material">insufficient material FIDE rule</a>.
     * @return true if the current position is a draw due to the insufficient material FIDE rule
     * @see <a href="https://rustic-chess.org/board_functionality/detecting_fide_draws.html">How to detect draw according to the FIDE rules</a>
     */
    public static boolean isInsufficientMaterialFIDERule(Board board) {
        if (board.getPawns() != 0 || board.getRooks() != 0 || board.getQueens() != 0) {
            // There's at least a rook, a queen or a pawn, it's not an insufficient material position
            return false;
        }
        // There's only light pieces and kings
        final int whiteBishops = Bits.count(board.getBishops(true));
        final int blackBishops = Bits.count(board.getBishops(false));
        final int whiteKnights = Bits.count(board.getKnights(true));
        final int blackKnights = Bits.count(board.getKnights(false));
        final int lightPieces = whiteBishops + blackBishops + whiteKnights + blackKnights;
        if (lightPieces <2) {
            // There's 0 or 1 light piece remaining, its a draw
            return true;
        }
        if (whiteKnights + blackKnights >0) {
            // If there's at least one knight and another piece (because lightPieces >=2), draw can't be claimed
            return false;
        }
        // There's only bishops and kings
        if (board.hasBishopPair(true) || board.hasBishopPair(false)) {
            // At least one player has a bishop pair, draw can't be claimed
            return false;
        }
        // There's only bishops and kings and no bishop pair, if bishops are all on squares of the same color, it's a draw.
        final long bishops = board.getBishops();
        final long whiteSquaresWithBishops = bishops & Square.WHITE;
        return whiteSquaresWithBishops==0 || whiteSquaresWithBishops==bishops;
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
        return !moveGenerator.isCheck(board, board.isWhite()) && moveGenerator.generateMoves(board).isEmpty();
    }
}
