package com.kelseyde.calvin.board;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.kelseyde.calvin.utils.notation.FEN;

class BoardBuilderTest {

    @Test
    void test() {
        Board board = BoardBuilder.newStandard();
        assertEquals(FEN.STARTPOS, FEN.toFEN(board));
    }

}
