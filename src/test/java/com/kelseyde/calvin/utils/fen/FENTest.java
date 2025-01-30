package com.kelseyde.calvin.utils.fen;

import static org.junit.jupiter.api.Assertions.*;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.utils.notation.FEN;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class FENTest {

    @Test
    void testStartingPosition() {

        Board fenBoard = FEN.toBoard(FEN.STARTPOS);
        Board newBoard = Board.from(FEN.STARTPOS);
        assertEquals(newBoard.getPawns(true), fenBoard.getPawns(true));
        assertEquals(newBoard.getKnights(true), fenBoard.getKnights(true));
        assertEquals(newBoard.getBishops(true), fenBoard.getBishops(true));
        assertEquals(newBoard.getRooks(true), fenBoard.getRooks(true));
        assertEquals(newBoard.getQueens(true), fenBoard.getQueens(true));
        assertEquals(newBoard.getKing(true), fenBoard.getKing(true));
        assertEquals(newBoard.getPawns(false), fenBoard.getPawns(false));
        assertEquals(newBoard.getKnights(false), fenBoard.getKnights(false));
        assertEquals(newBoard.getBishops(false), fenBoard.getBishops(false));
        assertEquals(newBoard.getRooks(false), fenBoard.getRooks(false));
        assertEquals(newBoard.getQueens(false), fenBoard.getQueens(false));
        assertEquals(newBoard.getKing(false), fenBoard.getKing(false));

        assertEquals(newBoard.getWhitePieces(), fenBoard.getWhitePieces());
        assertEquals(newBoard.getBlackPieces(), fenBoard.getBlackPieces());
        assertEquals(newBoard.getOccupied(), fenBoard.getOccupied());

        assertEquals(newBoard.isWhite(), fenBoard.isWhite());
        assertEquals(newBoard.getState(), fenBoard.getState());
        assertEquals(Arrays.asList(newBoard.getStates()), Arrays.asList(fenBoard.getStates()));
        assertEquals(Arrays.asList(newBoard.getMoves()), Arrays.asList(fenBoard.getMoves()));

    }
    
    @Test
    void testWrongFEN() {
//        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard(null));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard(""));
        // Missing pieces 
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbn/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        assertThrows(IllegalArgumentException.class, () -> FEN.toBoard("rnbqkbnr/pppppppp/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        
    }

}