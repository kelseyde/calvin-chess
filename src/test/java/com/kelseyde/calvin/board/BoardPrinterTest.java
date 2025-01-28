package com.kelseyde.calvin.board;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.kelseyde.calvin.utils.notation.FEN;

import java.util.ArrayList;
import java.util.List;

class BoardPrinterTest {
    @Test
    void testPrintBitBoard() {
        final List<String> result = new ArrayList<>();
        final BoardPrinter printer = new BoardPrinter(result::add);
        printer.print(Bits.of(Square.of(0, 0)));
        assertEquals(18, result.size());
        // Test borders
        for (int i=0;i<17;i=i+2) {
            assertEquals(BoardPrinter.BORDER, result.get(i));
        }
        assertEquals("  a   b   c   d   e   f   g   h", result.get(result.size()-1));
        // Test lines that corresponds to the bitboard
        for (int i=1;i<=13;i=i+4) {
            assertEquals("|   |   |   |   |   |   |   |   | "+Integer.toString((17-i)/2), result.get(i));
        }
        assertEquals("| 1 |   |   |   |   |   |   |   | 1", result.get(15));
        
        // Test no coordinates
        result.clear();
        printer.withCoordinates(false).print(Square.ALL);
        assertEquals(17, result.size());
        // Test borders
        for (int i=0;i<17;i=i+2) {
            assertEquals(BoardPrinter.BORDER, result.get(i));
        }
        // Test there's no coordinates and lines starts right
        for (int i=1;i<17;i=i+2) {
            assertTrue(result.get(i).startsWith("| 1 | 1 |"));
            assertEquals(BoardPrinter.BORDER.length(), result.get(i).length());
        }
    }
    
    @Test
    void testPrintBoard() {
        final List<String> result = new ArrayList<>();
        final BoardPrinter printer = new BoardPrinter(result::add).withCoordinates(false);
        Board board = Board.from(FEN.STARTPOS);
        printer.print(board);
        assertEquals(17, result.size());
        // Test borders
        for (int i=0;i<17;i=i+2) {
            assertEquals(BoardPrinter.BORDER, result.get(i));
        }
        // Test contents
        assertEquals("| r | n | b | q | k | b | n | r |", result.get(1));
        assertEquals("| p | p | p | p | p | p | p | p |", result.get(3));
        for (int i=5;i<=11;i=i+2) {
            assertEquals("|   |   |   |   |   |   |   |   |", result.get(i));
        }
        assertEquals("| P | P | P | P | P | P | P | P |", result.get(13));
        assertEquals("| R | N | B | Q | K | B | N | R |", result.get(15));
    }
    
    @Test
    void testWrongArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BoardPrinter(null));
    }

}
