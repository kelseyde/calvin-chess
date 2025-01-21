package com.kelseyde.calvin.movegen.perft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.ChessVariant;
import com.kelseyde.calvin.utils.Perft;
import com.kelseyde.calvin.utils.Perft.Result;
import com.kelseyde.calvin.utils.Perft.Type;
import com.kelseyde.calvin.utils.notation.FEN;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class PerftSuiteTest {

    @Test
    @DisabledIfSystemProperty(named="perftDepth", matches = "0")
    void testPerftSuite() throws IOException {
    	doTestSuite("perftDepth", 2, Files.readAllLines(Paths.get("src/test/resources/perft_suite.epd")), ChessVariant.STANDARD);
    }

    @Test
    @DisabledIfSystemProperty(named="perftChess960Depth", matches = "0")
    void testChess960PerftSuite() throws IOException {
    	doTestSuite("perftChess960Depth", 2, Files.readAllLines(Paths.get("src/test/resources/perft_chess960_suite.epd")), ChessVariant.CHESS960);
    }
    
    private void doTestSuite(String depthProperty, int defaultDepth, List<String> testLines, ChessVariant variant) {
		final int depth = Integer.getInteger(depthProperty, defaultDepth);
        if (depth!=defaultDepth) {
        	System.out.println(depthProperty+": "+depth+", "+testLines.size()+" lines");
        }
        testLines.stream().parallel().forEach(line -> {
            String[] parts = line.split(";");
            String fen = parts[0];
            if (parts.length<=depth) {
            	return;
            }
            long expectedTotalMoves = Long.parseLong(parts[depth].split(" ")[1].trim());
            final Board board = FEN.toBoard(fen);
            board.setVariant(variant);
            final Result result = new Perft().perft(board, depth);
            assertEquals(expectedTotalMoves, result.leafNodesCount(), String.format("Fen: %s, Depth: %s, Expected: %s, Actual: %s", fen, depth, expectedTotalMoves, result.leafNodesCount()));
        });
    }

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
