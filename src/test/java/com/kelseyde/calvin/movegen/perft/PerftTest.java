package com.kelseyde.calvin.movegen.perft;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.ChessVariant;
import com.kelseyde.calvin.utils.Perft;
import com.kelseyde.calvin.utils.notation.FEN;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public abstract class PerftTest {
    private ChessVariant variant;
    protected abstract String getFen();
    protected abstract String getSubFolder();
    
    protected PerftTest() {
        this.variant = ChessVariant.STANDARD;
    }

    protected void perft(int depth, long expectedTotalMoves) {
        Board board = FEN.toBoard(getFen());
        board.setVariant(variant);
        Instant start = Instant.now();
        final Perft perft = new Perft();
        Perft.Result result = perft.perft(board, depth);
        System.out.println("totalMoveCount: " + result.leafNodesCount());
        Instant end = Instant.now();
        Duration performance = Duration.between(start, end);

        float nps = (float) result.searchedNodesCount() / ((float) performance.toNanos() / 1000000);
        System.out.println("nps: " + nps);
        if (expectedTotalMoves == result.leafNodesCount() && getSubFolder() != null) {
            writeResults(depth, performance);
        }
        Assertions.assertEquals(expectedTotalMoves, result.leafNodesCount(),
                String.format("Fen: %s, Depth: %s, Expected: %s, Actual: %s", getFen(), depth, expectedTotalMoves, result.leafNodesCount()));
    }

    private void writeResults(int depth, Duration performance) {
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String line = String.format("%s,%s%n", timestamp, performance);
        String fileName = String.format("src/test/resources/perft/%s/perft_depth_%s.csv", getSubFolder(), depth);
        Path path = Paths.get(fileName);
        try {
            Files.writeString(path, line, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.printf("Error writing to %s! %s", fileName, e);
        }
    }

    public void setVariant(ChessVariant variant) {
        this.variant = variant;
    }
}
