package com.kelseyde.calvin.board;

import static org.junit.jupiter.api.Assertions.*;

import static com.kelseyde.calvin.board.Square.*;

import org.junit.jupiter.api.Test;

class SquareTest {

    @Test
    void testBlackAndWhite() {
        
        assertEquals(0, WHITE & BLACK);
        assertEquals(ALL, WHITE | BLACK);
        
        assertFalse(isWhite("a1"));
        assertTrue(isWhite("h1"));
        assertTrue(isWhite("a2"));
        assertFalse(isWhite("d2"));
        assertTrue(isWhite("g6"));
        assertFalse(isWhite("h8"));
    }
    
    private boolean isWhite(String notation) {
        long bit = Bits.of(fromNotation(notation));
        return (bit & WHITE) == bit;
    }

    @Test
    void testAlgebraicNotation() {
        assertEquals(0, fromNotation("a1"));
        assertEquals(63, fromNotation("h8"));
        assertThrows(IllegalArgumentException.class, () -> fromNotation("i1"));
        assertThrows(IllegalArgumentException.class, () -> fromNotation("a9"));
        assertThrows(IllegalArgumentException.class, () -> fromNotation("a"));
        assertThrows(IllegalArgumentException.class, () -> fromNotation("ab"));
        assertThrows(IllegalArgumentException.class, () -> fromNotation("1a"));
    }

}
