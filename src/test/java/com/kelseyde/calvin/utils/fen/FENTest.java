package com.kelseyde.calvin.utils.fen;

import static org.junit.jupiter.api.Assertions.*;

import com.fathzer.chess.utils.test.helper.fen.FENComparator;
import com.kelseyde.calvin.board.Bits;
import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.BoardState;
import com.kelseyde.calvin.board.Castling;
import com.kelseyde.calvin.board.ChessVariant;
import com.kelseyde.calvin.board.Piece;
import com.kelseyde.calvin.board.Square;
import com.kelseyde.calvin.utils.notation.FEN;
import org.junit.jupiter.api.Test;

class FENTest {

    @Test
    void testStartingPosition() {
        Board fenBoard = FEN.toBoard(FEN.STARTPOS);
        assertEquals(0b0000000000000000000000000000000000000000000000001111111100000000L, fenBoard.getPawns(true));
        assertEquals(0b0000000000000000000000000000000000000000000000000000000001000010L, fenBoard.getKnights(true));
        assertEquals(0b0000000000000000000000000000000000000000000000000000000000100100L, fenBoard.getBishops(true));
        assertEquals(0b0000000000000000000000000000000000000000000000000000000010000001L, fenBoard.getRooks(true));
        assertEquals(0b0000000000000000000000000000000000000000000000000000000000001000L, fenBoard.getQueens(true));
        assertEquals(0b0000000000000000000000000000000000000000000000000000000000010000L, fenBoard.getKing(true));
        assertEquals(0b0000000011111111000000000000000000000000000000000000000000000000L, fenBoard.getPawns(false));
        assertEquals(0b0100001000000000000000000000000000000000000000000000000000000000L, fenBoard.getKnights(false));
        assertEquals(0b0010010000000000000000000000000000000000000000000000000000000000L, fenBoard.getBishops(false));
        assertEquals(0b1000000100000000000000000000000000000000000000000000000000000000L, fenBoard.getRooks(false));
        assertEquals(0b0000100000000000000000000000000000000000000000000000000000000000L, fenBoard.getQueens(false));
        assertEquals(0b0001000000000000000000000000000000000000000000000000000000000000L, fenBoard.getKing(false));

        assertEquals(0b0000000000000000000000000000000000000000000000001111111111111111L, fenBoard.getWhitePieces());
        assertEquals(0b1111111111111111000000000000000000000000000000000000000000000000L, fenBoard.getBlackPieces());
        assertEquals(0b1111111111111111000000000000000000000000000000001111111111111111L, fenBoard.getOccupied());

        assertTrue(fenBoard.isWhite());
        BoardState state = fenBoard.getState();
        assertEquals(0, state.getHalfMoveClock());
        assertEquals(-1, state.getEnPassantFile());
        final int rights = state.rights;
        assertTrue(Castling.kingsideAllowed(rights, true));
        assertTrue(Castling.kingsideAllowed(rights, false));
        assertTrue(Castling.queensideAllowed(rights, true));
        assertTrue(Castling.queensideAllowed(rights, true));
    }
    
    @Test
    void testStandardChess() {
        final Board board = FEN.toBoard("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6");
        final BoardState state = board.getState();
        // Default half move clock is 0
        assertEquals(0, state.getHalfMoveClock());
        // e4 should be a white queen
        int e4 = Square.fromNotation("e4");
        assertEquals(Piece.QUEEN, board.pieceAt(e4));
        assertTrue(Bits.contains(board.getWhitePieces(), e4));
        assertTrue(Bits.contains(board.getQueens(), e4));
        // Check castlings
        int rights = state.getRights();
        assertTrue(Castling.kingsideAllowed(rights, true));
        assertFalse(Castling.kingsideAllowed(rights, false));
        assertTrue(Castling.queensideAllowed(rights, true));
        assertTrue(Castling.queensideAllowed(rights, true));
        // Check en passant
        assertEquals(0, state.getEnPassantFile());
    }
    
    private void assertChess960FenEquals(String expected, String actual) {
        final boolean ok =new FENComparator().withStrictCastling(false).withStrictMoveNumber(false).areEqual(expected, actual);
        assertTrue(ok, expected + " is not a FEN equivalent of " + actual);
    }
    
    @Test
    void testChess960() {
        // Warning, all move counters should be ignored in this test because Board does not support move counter different from the previous move count
        final var fen = "nbbqrknr/pppppppp/8/8/8/8/PPPPPPPP/NBBQRKNR w KQkq - 0 1";
        var board = FEN.toBoard(fen, ChessVariant.CHESS960);
        // Check from position of rooks in castling rights
        assertEquals("h1", Square.toNotation(Castling.getRook(board.getState().getRights(), true, true)));
        assertEquals("e1", Square.toNotation(Castling.getRook(board.getState().getRights(), false, true)));
        assertEquals("h8", Square.toNotation(Castling.getRook(board.getState().getRights(), true, false)));
        assertEquals("e8", Square.toNotation(Castling.getRook(board.getState().getRights(), false, false)));
        
        assertChess960FenEquals(fen, FEN.toFEN(board));
        
        final String fenWithInnerRook = "rn2k1r1/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BNK1RR w Gkq - 4 4";
        board = FEN.toBoard(fenWithInnerRook, ChessVariant.CHESS960);
        int rights = board.getState().getRights();
        assertTrue(Castling.kingsideAllowed(rights, true));     
        assertFalse(Castling.queensideAllowed(rights, true));     
        assertTrue(Castling.kingsideAllowed(rights, false));     
        assertTrue(Castling.queensideAllowed(rights, false));
        
        assertEquals(Square.fromNotation("g1"), Castling.getRook(rights, true, true));
        assertEquals(Square.fromNotation("g8"), Castling.getRook(rights, true, false));
        assertEquals(Square.fromNotation("a8"), Castling.getRook(rights, false, false));

        assertChess960FenEquals(fenWithInnerRook, FEN.toFEN(board));
        
        final String otherFenWithInnerRook = "1r2k1r1/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/RR2K3 w Bkq - 4 1";
        board = FEN.toBoard(otherFenWithInnerRook, ChessVariant.CHESS960);
        assertChess960FenEquals(otherFenWithInnerRook, FEN.toFEN(board));
        
        // There's no rook at the specified rank
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r2k3/ppp1ppr1/3p2pp/5bn1/P7/2N2B2/1PPPPP2/RR2K3 w Bqg - 4 1", ChessVariant.CHESS960));
        // Illegal castling : King is not at its staring rank
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r4r1/pppkpp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/RR2K3 w Bqg - 4 1", ChessVariant.CHESS960));
        // Illegal castling : King is not at its starting file (that can't be 'a' or 'h')
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r4rk/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/1R2K1R1 w KQq - 4 1", ChessVariant.CHESS960));
        // Illegal castling : King is not between its rooks start position
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r3rk1/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/1R2KR2 w k - 4 1", ChessVariant.CHESS960));
        // Illegal castling : Both color has castling rights but kings are not on the same file
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r2kr2/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/1R1K1R2 w KQkq - 4 1", ChessVariant.CHESS960));
        // Illegal castling : Both color has castling rights but rooks are not on the same file
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r2k1r1/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/1R2KR2 w Kk - 4 1", ChessVariant.CHESS960));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1r2kr2/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/R3KR2 w Qq - 4 1", ChessVariant.CHESS960));
        // Two queen side castling defined
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rr2k3/1pppqppp/2nb1n1b/pP6/4Q3/2NBBP1N/P1PPP1P1/RR2K3 w BQbq - 0 1", ChessVariant.CHESS960));
    }
    
    @Test
    void testWrongFEN() {
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard(null));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard(""));
        
        // Problems in pieces
        // Missing a file
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbn/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        // Missing a rank
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        // Empty a rank
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp//8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        // Missing the pieces
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("w KQkq - 0 1"));
        // Illegal piece code
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnTqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        
        // Problems in color to play
        // Missing color to play
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR KQkq - 0 1"));
        // Invalid color
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq - 0 1"));
        
        // Illegal castling rights
        // Missing rook
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp/8/8/P7/R7/1PPPPPPP/1NBQKBNR w QK - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp/8/8/8/7R/PPPPPPPP/RNBQKBN1 w K - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbn1/pppppppp/7r/8/8/8/PPPPPPPP/RNBQKBNR w k - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1nbqkbnr/pppppppp/r7/8/8/8/PPPPPPPP/RNBQKBNR w q - 0 1"));
        // Wrong rook color
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1nbqkbnr/1ppppppp/8/p7/P7/2R5/1PPPPPPP/rNBQKBNR w QK - 0 1"));
        // King is not on its starting position
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnb1kbnr/pppp1ppp/4p3/6q1/8/2P5/PPQPPPPP/RNBK1BNR w K - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnb1kbnr/pppp1ppp/4p3/6q1/8/2P5/PPQPPPPP/RNBK1BNR w Q - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbk1bnr/pppp1ppp/4p3/6q1/8/2P5/PPQPPPPP/RNB1KBNR w k - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbk1bnr/pppp1ppp/4p3/6q1/8/2P5/PPQPPPPP/RNB1KBNR w q - 0 1"));
        
        // Illegal en passant (no pawn there)
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("r1b1k2r/1pppqppp/2n2n1b/1P6/p3Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1"));
        // Illegal en passant (not the right color to play)
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R b KQq a6"));
        // Illegal en passant (no the right rank)
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a5 0 1"));
        
        // Two kings of same color
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("r1b1k2r/1pppqppp/2nk1n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w - - 0 1"));
        // No king for one color
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("r1b4r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w - - 0 1"));
        
        // Castling with shredder notation different from a or h files
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("1rb1k1r1/1pppqppp/2n2n1b/pP6/4Q3/2NB1P1N/P1PPP1P1/1RB1K1R1 w KQkq - 0 1"));
    }
}