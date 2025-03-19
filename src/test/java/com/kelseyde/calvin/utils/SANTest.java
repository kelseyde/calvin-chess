package com.kelseyde.calvin.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.ChessVariant;
import com.kelseyde.calvin.board.Move;
import com.kelseyde.calvin.utils.notation.FEN;
import com.kelseyde.calvin.utils.notation.SAN;

class SANTest {
    @Test
    void test() {
        Board board = FEN.toBoard("rnbqkbnr/pppp1ppp/8/4p3/3P3P/8/PPP1PPP1/RNBQKBNR b KQkq d3 0 2");
        assertEquals("exd4", SAN.fromMove(move(board, "e5d4"), board));
        
        board = FEN.toBoard("r1b1k2r/ppp2ppp/5n2/7P/Pq1n4/6P1/1P1Q1P2/1R2KBNR b Kkq - 1 13");
        assertEquals("Qxd2+", SAN.fromMove(move(board, "b4d2"), board));
        
        board = FEN.toBoard("r5k1/pp3ppp/2p2n2/P5PP/KP3P2/2r5/8/1bq5 b - - 0 28");
        assertEquals("Qa3#", SAN.fromMove(move(board, "c1a3"), board));
        
        board = FEN.toBoard("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
        assertEquals("O-O", SAN.fromMove(move(board, "e8g8"), board));
        board = FEN.toBoard("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
        assertEquals("O-O-O", SAN.fromMove(move(board, "e8c8"), board));
        
        board = FEN.toBoard("rnbqkbnr/p1pppppp/8/PpP5/8/8/1P1PPPPP/RNBQKBNR w KQkq b6 0 1");
        assertEquals("axb6", SAN.fromMove(move(board, "a5b6"), board));
        
        board = FEN.toBoard("2kr3r/Ppp1pppp/3p4/8/2P5/1P3K2/2PP2PP/R1B1Q3 w - - 0 1");
        assertEquals("a8=Q+",SAN.fromMove(move(board, "a7a8q"), board));
        
        board = FEN.toBoard("4k3/8/8/8/8/8/r5q1/4K3 b - - 0 1");
        assertEquals("Rd2",SAN.fromMove(move(board, "a2d2"), board),"Problem with DRAW");
        
        
        // Castling in chess960
        board = FEN.toBoard("4k3/8/8/8/8/8/r5q1/4K3 b - - 0 1");
        board.setVariant(ChessVariant.CHESS960);
        
        // Disambiguation
        board = FEN.toBoard("2kr3r/pppppppp/8/R7/2P1Q2Q/1P3K2/2PP2PP/RNB4Q w - - 0 1");
        assertEquals("R1a3", SAN.fromMove(move(board, "a1a3"), board));
        Move mv = move(board, "h4e1");
//        assertEquals("Qh4e1", SAN.fromMove(mv, board));
        board.makeMove(mv);
        mv = move(board, "d8f8");
        assertEquals("Rdf8", SAN.fromMove(mv, board));
        
        // Illegal moves
        final Board board2 = board.copy();
        final Move move2 = Move.fromUCI("f3g2");
//        System.out.println(SAN.fromMove (move2, board2));
//        assertThrows(IllegalMoveException.class, () -> SAN.fromMove (move2, board2));
        
        final Board board3 = FEN.toBoard("2kr3r/Rppppppp/8/8/2P1Q2Q/1P3K2/2PP2PP/RNB4Q w - - 0 1");
        final Move move3 = Move.fromUCI("a7a8q");
//        assertThrows(IllegalMoveException.class, () -> SAN.fromMove(move3, board3));
    }

    private Move move(Board board, String uciMove) {
        return TestUtils.getLegalMove(board, Move.fromUCI(uciMove));
    }
}
