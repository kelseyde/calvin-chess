package com.kelseyde.calvin.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;

import com.fathzer.chess.utils.model.Variant;
import com.fathzer.chess.utils.test.PGNTest;
import com.fathzer.chess.utils.test.helper.fen.FENComparator;
import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Move;
import com.kelseyde.calvin.test.ChessTestUtils.CalvinBoard;
import com.kelseyde.calvin.utils.TestUtils;
import com.kelseyde.calvin.utils.notation.FEN;
import com.kelseyde.calvin.utils.notation.PGN;

import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S3577")
class CustomizedPGNTest extends PGNTest<CalvinBoard, Move> {
    
    @Test
    void setTagPairsTest() {
        Board board = Board.from(FEN.STARTPOS);
        addMoves(board, FATAL_ATTRACTION_MOVES.split(" "));
        PGN pgn2 = new PGN(board);
        pgn2.setTag(WHITE_TAG, "Lasker");
        pgn2.setTag(BLACK_TAG, "Thomas");
        pgn2.setTag(DATE_TAG, "1912.??.??");
        pgn2.setTag("MyOwnTag", "MyOwnValue");
        var parsed = parse(pgn2.toString());
        assertEquals(SEVEN_TAG_ROSTER_KEYS+" MyOwnTag", parsed.tagPairs().keySet().stream().collect(Collectors.joining(" ")));
        assertEquals("Lasker", parsed.tagPairs().get(WHITE_TAG), "Wrong white tag");
        assertEquals("Thomas", parsed.tagPairs().get(BLACK_TAG), "Wrong black tag");
        assertEquals("1912.??.??", parsed.tagPairs().get(DATE_TAG), "Wrong date tag");
        assertEquals("MyOwnValue", parsed.tagPairs().get("MyOwnTag"), "Wrong custom tag");
    }

    private void addMoves(Board board, String[] uciMoves) {
        for (String mv:uciMoves) {
            board.makeMove(TestUtils.getLegalMove(board, Move.fromUCI(mv)));
        }
    }

    @Override
    protected void assertFen(Variant variant, String expectedFEN, String actualFEN) {
        if (variant == Variant.CHESS960) {
            // Do lenient castling rights check if variant is CHESS960
            if (!new FENComparator().withStrictCastling(false).areEqual(expectedFEN, actualFEN)) {
                wrongTag(FEN_TAG);
            }
        } else {
            super.assertFen(variant, expectedFEN, actualFEN);
        }
    }
}