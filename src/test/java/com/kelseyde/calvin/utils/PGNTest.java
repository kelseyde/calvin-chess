package com.kelseyde.calvin.utils;

import static org.junit.jupiter.api.Assertions.*;

import static com.kelseyde.calvin.utils.notation.PGN.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Move;
import com.kelseyde.calvin.utils.notation.FEN;
import com.kelseyde.calvin.utils.notation.PGN;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PGNTest {
    
    @Disabled
    @Test
    void test960() {
        //TODO requires FEN.toBoard(String, ChessVariant). Parsing FEN with FEN.toBoard(String) currently works but will probably not in a near future.
        fail("Not yet implemented");
    }

    @Test
    void testPGN() {
        // Lasker vs Thomas, 1912
        Board board = Board.from(FEN.STARTPOS);
        String moves = "d2d4 e7e6 g1f3 f7f5 b1c3 g8f6 c1g5 f8e7 g5f6 e7f6 e2e4 f5e4 c3e4 b7b6 f3e5 e8g8 f1d3"
                + " c8b7 d1h5 d8e7 h5h7 g8h7 e4f6 h7h6 e5g4 h6g5 h2h4 g5f4 g2g3 f4f3 d3e2 f3g2 h1h2 g2g1 e1d2";
        addMoves(board, moves.split(" "));

        String pgn = PGN.toPGN(board);
        
        // Check that no lines are bigger that 80 chars
        String[] lines = pgn.split("\n");
        for (String line : lines) {
            assertTrue(line.length() <= 80, "Line too long: " + line+" ("+line.length()+">80)");
        }
        
        Content parsed = parse(pgn);
        assertFalse(parsed.tagPairs.containsKey(VARIANT_TAG), "Variant tag found when not expected");
        assertFalse(parsed.tagPairs.containsKey(FEN_TAG), "FEN tag found when not expected");
        // Check that mandatory tags are there in the right order
        String expectedOrderedTags = EVENT_TAG+" "+SITE_TAG+" "+DATE_TAG+" "+ROUND_TAG+" "+WHITE_TAG+" "+BLACK_TAG+" "+RESULT_TAG;
        assertEquals(expectedOrderedTags, parsed.tagPairs.keySet().stream().collect(Collectors.joining(" ")));
        assertEquals(WHITE_WON, parsed.tagPairs.get(RESULT_TAG), "Wrong result tag");
        assertEquals("d4 e6 Nf3 f5 Nc3 Nf6 Bg5 Be7 Bxf6 Bxf6 e4 fxe4 Nxe4 b6 Ne5 O-O Bd3 Bb7 Qh5 Qe7 Qxh7+ Kxh7 Nxf6+ Kh6 Neg4+ Kg5 h4+ Kf4 g3+ Kf3 Be2+ Kg2 Rh2+ Kg1 Kd2#",
                parsed.moves().stream().collect(Collectors.joining(" ")));

        PGN pgn2 = new PGN(board);
        pgn2.setTag(WHITE_TAG, "Lasker");
        pgn2.setTag(BLACK_TAG, "Thomas");
        pgn2.setTag(DATE_TAG, "1912.??.??");
        pgn2.setTag("MyOwnTag", "MyOwnValue");
        parsed = parse(pgn2.toString());
        assertEquals(expectedOrderedTags+" MyOwnTag", parsed.tagPairs.keySet().stream().collect(Collectors.joining(" ")));
        assertEquals("Lasker", parsed.tagPairs.get(WHITE_TAG), "Wrong white tag");
        assertEquals("Thomas", parsed.tagPairs.get(BLACK_TAG), "Wrong black tag");
        assertEquals("1912.??.??", parsed.tagPairs.get(DATE_TAG), "Wrong date tag");
        assertEquals("MyOwnValue", parsed.tagPairs.get("MyOwnTag"), "Wrong custom tag");
    }

    private void addMoves(Board board, String[] uciMoves) {
        for (String mv:uciMoves) {
            board.makeMove(TestUtils.getLegalMove(board, Move.fromUCI(mv)));
        }
    }

    private record Content(Map<String, String> tagPairs, List<String> moves) {}
 
    private Content parse(String pgn) {
        final String[] lines = pgn.split("\n");
        final Map<String, String> tagPairs = new LinkedHashMap<>();
        final List<String> moves = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("[") && line.endsWith("]")) {
                line = line.substring(1, line.length() - 1);
                final int index = line.indexOf(' ');
                if (index == -1 || index == line.length() - 1) {
                    throw new IllegalArgumentException("Invalid tag: " + line);
                }
                final String name = line.substring(0, index);
                final String value = line.substring(index + 1);
                if (name.isBlank() || value.isBlank() || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
                    throw new IllegalArgumentException("Invalid tag: " + line);
                }
                tagPairs.put(name, value.substring(1, value.length() - 1));
            } else if (!line.isEmpty()) {
                // A move line
                parseMoves(moves, line.split(" "));
            }
        }
        return new Content(tagPairs, moves);
    }

    private void parseMoves(List<String> moves, String[] tokens) {
        for (String token : tokens) {
            if (!token.isEmpty() && !token.endsWith(".")) {
                moves.add(token);
            }
        }
    }
}