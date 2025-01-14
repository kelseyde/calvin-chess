package com.kelseyde.calvin.board;

public class Draw {

    public static boolean isEffectiveDraw(Board board) {
        return isDoubleRepetition(board) || isFiftyMoveRule(board) || isInsufficientMaterial(board);
    }

    public static boolean isThreefoldRepetition(Board board) {

        int repetitionCount = 0;
        long zobrist = board.getState().getKey();
        BoardState[] states = board.getStates();
        for (int i = board.getPly() - 1; i >= 0; i--) {
            if (states[i].getKey() == zobrist) {
                repetitionCount += 1;
            }
            if (repetitionCount >= 2) {
                return true;
            }
        }

        return false;

    }

    public static boolean isDoubleRepetition(Board board) {

        long zobrist = board.getState().getKey();
        BoardState[] states = board.getStates();
        for (int i = board.getPly() - 1; i >= 0; i--) {
            if (states[i].getKey() == zobrist) {
                return true;
            }
        }
        return false;

    }

    public static boolean isInsufficientMaterial(Board board) {
        if (board.getPawns() != 0 || board.getRooks() != 0 || board.getQueens() != 0) {
            return false;
        }
        long whitePieces = board.getKnights(true) | board.getBishops(true);
        long blackPieces = board.getKnights(false) |  board.getBishops(false);

        return (Bits.count(whitePieces) == 0 || Bits.count(whitePieces) == 1)
                && (Bits.count(blackPieces) == 0 || Bits.count(blackPieces) == 1);
    }

    public static boolean isFiftyMoveRule(Board board) {
        return board.getState().getHalfMoveClock() >= 100;
    }

}
