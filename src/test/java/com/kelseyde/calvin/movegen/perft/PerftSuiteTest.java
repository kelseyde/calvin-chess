package com.kelseyde.calvin.movegen.perft;

import org.junit.jupiter.api.Test;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.utils.Perft;
import com.kelseyde.calvin.utils.Perft.Result;
import com.kelseyde.calvin.utils.Perft.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerftSuiteTest {

    @Test
    void bulkTest() {
    	final Board board = Board.from("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    	Result perft = new Perft().perft(board, 3);
    	final long count = perft.leafNodesCount();
    	perft = new Perft().perft(board, 3, Type.BULK);
    	assertEquals(count, perft.leafNodesCount());
    }

    @Test
    void wrongDepthTest() {
    	final Board board = Board.from("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    	final Perft p = new Perft();
    	assertThrows(IllegalArgumentException.class, () -> p.perft(board, -1));
    }
}
