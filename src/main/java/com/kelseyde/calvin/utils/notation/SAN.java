package com.kelseyde.calvin.utils.notation;

import com.kelseyde.calvin.board.*;
import com.kelseyde.calvin.movegen.MoveGenerator;

import java.util.List;

/**
 * A move to <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Standard Algebraic Notation (SAN)</a> converter.
 */
public class SAN {

    private SAN() {
        super();
    }

    /**
     * Converts a move to its Standard Algebraic Notation (SAN)
     * @param move the move to convert
     * @param board the board on which the move is to be made.
     * @return the move in SAN notation.
     * <br>The SAN standard is relatively lax, some parts of the notation are optional (e.g. the <i>'e.p.'</i> when doing an en passant capture).
     * <br>This method returns the variant used in the PGN standard (no <i>'e.p.'</i> for en passant captures).
     */
    public static String fromMove(Move move, Board board) {
        Piece piece = board.pieceAt(move.from());
        Piece captured = board.pieceAt(move.to());

        if (move.isCastling()) {
            int delta = move.to() - move.from();
            return delta == 2 ? "O-O" : "O-O-O";
        }

        MoveGenerator moveGenerator = new MoveGenerator();
        final StringBuilder notation = new StringBuilder();
        if (piece != Piece.PAWN) {
            notation.append(piece.code().toUpperCase());
        }

        // Check if any ambiguity exists in notation (e.g. if e2 can be reached via Nfe2 and Nbe2)
        if (piece != Piece.PAWN && piece != Piece.KING) {
            List<Move> legalMoves = moveGenerator.generateMoves(board);

            for (Move legalMove : legalMoves) {
                if (legalMove.from() != move.from() && legalMove.to() == move.to() && board.pieceAt(legalMove.from()) == piece) {
                    int fromFileIndex = File.of(move.from());
                    int alternateFromFileIndex = File.of(legalMove.to());
                    int fromRankIndex = Rank.of(move.from());
                    int alternateFromRankIndex = Rank.of(legalMove.from());

                    if (fromFileIndex != alternateFromFileIndex) {
                        notation.append(File.toNotation(move.from()));
                        break;
                    }
                    else if (fromRankIndex != alternateFromRankIndex)
                    {
                        notation.append(Rank.toRankNotation(move.from()));
                        break;
                    }
                }
            }
        }

        if (captured != null || move.isEnPassant()) {
            // add 'x' to indicate capture
            if (piece == Piece.PAWN) {
                notation.append(File.toNotation(move.from()));
            }
            notation.append("x");
        }

        notation.append(File.toNotation(move.to()));
        notation.append(Rank.toRankNotation(move.to()));

        // Add promotion piece type
        if (move.promoPiece() != null) {
            Piece promotionPieceType = move.promoPiece();
            notation.append("=" + promotionPieceType.code().toUpperCase());
        }

        board.makeMove(move);
        if (moveGenerator.isCheck(board, board.isWhite())) {
            List<Move> legalMoves = moveGenerator.generateMoves(board);
            notation.append(legalMoves.isEmpty() ? "#" : "+");
        }
        board.unmakeMove();

        return notation.toString();
    }

}
