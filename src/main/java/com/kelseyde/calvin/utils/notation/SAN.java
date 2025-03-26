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
        final Piece piece = board.pieceAt(move.from());
        final Piece captured = board.pieceAt(move.to());

        if (move.isCastling()) {
            return move.to() > move.from() ? "O-O" : "O-O-O";
        }

        final MoveGenerator moveGenerator = new MoveGenerator();
        final StringBuilder notation = new StringBuilder();

        if (piece != Piece.PAWN) {
            notation.append(piece.code().toUpperCase());
        }

        addDisambiguation(notation, board, move, moveGenerator);

        if (captured != null || move.isEnPassant()) {
            if (piece == Piece.PAWN) {
                notation.append(File.toNotation(move.from()));
            }
            // add 'x' to indicate capture
            notation.append("x");
        }

        notation.append(Square.toNotation(move.to()));

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

    /**
     * Checks if any ambiguity exists in notation and adds disambiguation if needed (e.g. if e2 can be reached via Nfe2 and Nbe2)
     * @param notation the notation to add disambiguation to
     * @param board the board on which the move is played
     * @param move the move to add disambiguation for
     * @param moveGenerator the move generator to use for generating moves
     */
    private static void addDisambiguation(StringBuilder notation, Board board, Move move,MoveGenerator moveGenerator) {
        final Piece piece = board.pieceAt(move.from());
        if (piece != Piece.PAWN && piece != Piece.KING) {
            final List<Move> candidates = moveGenerator.generateMoves(board).stream().filter(m -> m.to() == move.to() && board.pieceAt(m.from()) == piece).toList();
            
            if (candidates.size() > 1) {
                // Disambiguation is required
                final boolean fileIsEnough = candidates.stream().filter(m -> File.of(m.from()) == File.of(move.from())).count() == 1;
                if (fileIsEnough) {
                    notation.append(File.toNotation(move.from()));
                } else {
                    final boolean rankIsEnough = candidates.stream().filter(m -> Rank.of(m.from()) == Rank.of(move.from())).count() == 1;
                    if (rankIsEnough) {
                        notation.append(Rank.toRankNotation(move.from()));
                    } else {
                        notation.append(Square.toNotation(move.from()));
                    }
                }
            }
        }
    }

}
