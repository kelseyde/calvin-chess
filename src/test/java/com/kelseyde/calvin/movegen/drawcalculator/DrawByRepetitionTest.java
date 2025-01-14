package com.kelseyde.calvin.movegen.drawcalculator;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Draw;
import com.kelseyde.calvin.utils.TestUtils;
import com.kelseyde.calvin.utils.notation.FEN;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DrawByRepetitionTest {

    @Test
    public void testSimpleDrawByRepetition() {

        Board board = Board.from(FEN.STARTPOS);
        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));

        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));

        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        Assertions.assertTrue(Draw.isThreefoldRepetition(board));

    }

    @Test
    public void testNotRepetitionIfCastlingRightsAreDifferent() {

        Board board = Board.from(FEN.STARTPOS);
        board.makeMove(TestUtils.getLegalMove(board, "e2", "e4"));
        board.makeMove(TestUtils.getLegalMove(board, "e7", "e5"));

        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e2"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e7"));

        // king can castle, moves instead
        board.makeMove(TestUtils.getLegalMove(board, "e1", "f1"));
        board.makeMove(TestUtils.getLegalMove(board, "e8", "f8"));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e1"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e8"));
        Assertions.assertFalse(Draw.isEffectiveDraw(board));

        // position of pieces repeated twice, but now no castling rights
        board.makeMove(TestUtils.getLegalMove(board, "e1", "f1"));
        board.makeMove(TestUtils.getLegalMove(board, "e8", "f8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e1"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        // position of pieces repeated thrice, but on the first occurrence castling rights were different
        Assertions.assertFalse(Draw.isThreefoldRepetition(board));


    }

    @Test
    public void testRepetitionDependingOnCastlingRights() {

        Board board = Board.from(FEN.STARTPOS);
        board.makeMove(TestUtils.getLegalMove(board, "e2", "e4"));
        board.makeMove(TestUtils.getLegalMove(board, "e7", "e5"));

        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e2"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e7"));

        // king can castle, moves instead
        board.makeMove(TestUtils.getLegalMove(board, "e1", "f1"));
        board.makeMove(TestUtils.getLegalMove(board, "e8", "f8"));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e1"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e8"));
        Assertions.assertFalse(Draw.isEffectiveDraw(board));

        // position of pieces repeated twice, but different castling rights
        board.makeMove(TestUtils.getLegalMove(board, "e1", "f1"));
        board.makeMove(TestUtils.getLegalMove(board, "e8", "f8"));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e1"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        // position of pieces repeated thrice, castling rights repeated 2 times
        board.makeMove(TestUtils.getLegalMove(board, "e1", "f1"));
        board.makeMove(TestUtils.getLegalMove(board, "e8", "f8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "f1", "e1"));
        board.makeMove(TestUtils.getLegalMove(board, "f8", "e8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        // position of pieces repeated four times, three times with same castling rights
        Assertions.assertTrue(Draw.isThreefoldRepetition(board));


    }

    @Test
    public void testNotRepetitionIfEnPassantRightsAreDifferent() {

        Board board = Board.from(FEN.STARTPOS);
        board.makeMove(TestUtils.getLegalMove(board, "e2", "e4"));
        board.makeMove(TestUtils.getLegalMove(board, "e7", "e6"));

        board.makeMove(TestUtils.getLegalMove(board, "e4", "e5"));
        board.makeMove(TestUtils.getLegalMove(board, "d7", "d5"));
        // white can en passant on d6
        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));
        Assertions.assertFalse(Draw.isEffectiveDraw(board));

        // position of pieces repeated twice, but en passant rights different
        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        // position of pieces repeated thrice, but en passant rights different
        Assertions.assertFalse(Draw.isThreefoldRepetition(board));


    }

    @Test
    public void testRepetitionDependingOnEnPassantRights() {

        Board board = Board.from(FEN.STARTPOS);
        board.makeMove(TestUtils.getLegalMove(board, "e2", "e4"));
        board.makeMove(TestUtils.getLegalMove(board, "e7", "e6"));

        board.makeMove(TestUtils.getLegalMove(board, "e4", "e5"));
        board.makeMove(TestUtils.getLegalMove(board, "d7", "d5"));

        // white can en passant on d6
        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        // position of pieces repeated twice, but en passant rights different
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));
        Assertions.assertFalse(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        board.makeMove(TestUtils.getLegalMove(board, "g8", "f6"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "f3", "g1"));
        // position of pieces repeated thrice, but en passant rights different
        board.makeMove(TestUtils.getLegalMove(board, "f6", "g8"));
        Assertions.assertTrue(Draw.isEffectiveDraw(board));

        board.makeMove(TestUtils.getLegalMove(board, "g1", "f3"));
        Assertions.assertTrue(Draw.isThreefoldRepetition(board));


    }

}
