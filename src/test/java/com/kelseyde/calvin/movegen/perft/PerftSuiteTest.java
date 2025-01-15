package com.kelseyde.calvin.movegen.perft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.kelseyde.calvin.board.ChessVariant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class PerftSuiteTest {

    @Test
    @DisabledIfSystemProperty(named="perftDepth", matches = "0")
    void testPerftSuite() throws IOException {
		final int depth = Integer.getInteger("perftDepth", 2);
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/perft_suite.epd"));
		System.out.println("perft depth: "+depth+", "+lines.size()+" lines");
        lines.stream().parallel().forEach(line -> {
            String[] parts = line.split(";");
            String fen = parts[0];
            if (parts.length<=depth) {
            	return;
            }
            long expectedTotalMoves = Long.parseLong(parts[depth].split(" ")[1].trim());
            perftDepth(fen, depth, expectedTotalMoves, ChessVariant.STANDARD);
        });
    }

    @Test
    @DisabledIfSystemProperty(named="perftChess960Depth", matches = "0")
    void testChess960PerftSuite() throws IOException {
		final int depth = Integer.getInteger("perftChess960Depth", 2);
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/perft_chess960_suite.epd"));
		System.out.println("chess960 perft depth: "+depth+", "+lines.size()+" lines");
        lines.stream().parallel().forEach(line -> {
            String[] parts = line.split(";");
            String fen = parts[0];
            if (parts.length<=depth) {
            	return;
            }
            long expectedTotalMoves = Long.parseLong(parts[depth].split(" ")[1].trim());
            perftDepth(fen, depth, expectedTotalMoves, ChessVariant.CHESS960);
        });
    }

    private void perftDepth(String fen, int depth, long expectedTotalMoves, ChessVariant variant) {
        final PerftTest perft = new PerftTest() {
            @Override
            protected String getFen() {
                return fen;
            }

            @Override
            protected String getSubFolder() {
                return null;
            }
        };
        perft.setVariant(variant);
		perft.perft(depth, expectedTotalMoves);
    }

}
