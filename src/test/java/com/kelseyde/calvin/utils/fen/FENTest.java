package com.kelseyde.calvin.utils.fen;

import static org.junit.jupiter.api.Assertions.*;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.BoardState;
import com.kelseyde.calvin.board.Castling;
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
        assertEquals(0, state.halfMoveClock);
        assertEquals(-1, state.enPassantFile);
        final int rights = state.rights;
        assertTrue(Castling.kingsideAllowed(rights, true));
        assertTrue(Castling.kingsideAllowed(rights, false));
        assertTrue(Castling.queensideAllowed(rights, true));
        assertTrue(Castling.queensideAllowed(rights, true));
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
        
    }

}