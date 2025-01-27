package com.kelseyde.calvin.board;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.kelseyde.calvin.movegen.MoveGenerator;
import com.kelseyde.calvin.utils.TestUtils;
import com.kelseyde.calvin.utils.notation.FEN;

class DrawTest {
	private MoveGenerator mg = new MoveGenerator();
    
    @Test
    void testRepetitions() {
        // No draw at all
        String fen ="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String[] moves = "d2d4 d7d5 g1f3 g8f6 g2g3 c7c6 f1g2 h7h6 e1g1 g7g6".split(" ");
        Board board = build(fen, moves);
        assertFalse(Draw.isDoubleRepetition(board));
        assertFalse(Draw.isThreefoldRepetition(board));
        assertFalse(Draw.isInsufficientMaterial(board));
        assertFalse(Draw.isStalemate(board, mg));
        assertFalse(Draw.isEffectiveDraw(board));
        assertFalse(Draw.isDraw(board, mg));
        
        // Two repetitions
        fen ="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        moves = "b1c3 b8c6 c3b1 c6b8".split(" ");
        board = build(fen, moves);
        assertTrue(Draw.isDoubleRepetition(board));
        assertFalse(Draw.isThreefoldRepetition(board));
        assertFalse(Draw.isInsufficientMaterial(board));
        assertFalse(Draw.isStalemate(board, mg));
        assertTrue(Draw.isEffectiveDraw(board));
        assertFalse(Draw.isDraw(board, mg));
        
        // Three repetitions
        fen ="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        moves = "b1c3 b8c6 g1f3 g8f6 f3g1 f6g8 c3b1 c6b8 b1c3 b8c6 c3b1 c6b8".split(" ");
        board = build(fen, moves);
        assertTrue(Draw.isDoubleRepetition(board));
        assertTrue(Draw.isThreefoldRepetition(board));
        assertFalse(Draw.isInsufficientMaterial(board));
        assertFalse(Draw.isStalemate(board, mg));
        assertTrue(Draw.isEffectiveDraw(board));
        assertTrue(Draw.isDraw(board, mg));
    }
    
    @Test
    void testStaleMate() {
        // staleMate
        Board board = FEN.toBoard("7k/3Q4/8/8/2B5/8/8/4K3 b - - 0 1");
        assertFalse(Draw.isDoubleRepetition(board));
        assertFalse(Draw.isThreefoldRepetition(board));
        assertFalse(Draw.isInsufficientMaterial(board));
        assertTrue(Draw.isStalemate(board, mg));
        assertFalse(Draw.isEffectiveDraw(board));
        assertTrue(Draw.isDraw(board, mg));
        
        // Black seem stale mated ... but white to move.
        board = FEN.toBoard("7k/3Q4/8/8/2B5/8/8/4K3 w - - 0 1");
        assertFalse(Draw.isDoubleRepetition(board));
        assertFalse(Draw.isThreefoldRepetition(board));
        assertFalse(Draw.isInsufficientMaterial(board));
        assertFalse(Draw.isStalemate(board, mg));
        assertFalse(Draw.isEffectiveDraw(board));
        assertFalse(Draw.isDraw(board, mg));

        // Mate.
        board = FEN.toBoard("6Qk/8/8/8/2B5/8/8/4K3 b - - 0 1");
        assertFalse(Draw.isDoubleRepetition(board));
        assertFalse(Draw.isThreefoldRepetition(board));
        assertFalse(Draw.isInsufficientMaterial(board));
        assertFalse(Draw.isStalemate(board, mg));
        assertFalse(Draw.isEffectiveDraw(board));
        assertFalse(Draw.isDraw(board, mg));
    }
    
    @Test
    void testInsufficientMaterial() {
        // Draw positions
        // Nothing but kings
        assertDraw(FEN.toBoard("8/7k/8/8/8/8/8/3K4 b - - 0 1"));
        // King vs bishop
        assertDraw(FEN.toBoard("7k/8/8/8/2B5/8/8/4K3 b - - 0 1"));
        // King vs knight
        assertDraw(FEN.toBoard("8/7k/8/8/8/6n1/8/3K4 b - - 0 1"));
        // Bishop vs bishop of the same color
        assertDraw (FEN.toBoard("8/3K4/k7/8/3b4/8/3B4/8 b - - 0 1"));
        // Three bishops of the same cell's color vs king
        assertDraw(FEN.toBoard("8/3K2B1/k7/8/8/8/3B4/8 b - - 0 1"));

        // Not draw positions
        // Two bishops of different cell's colors vs king
        assertNotDraw(FEN.toBoard("8/3K4/k5B1/8/8/8/3B4/8 b - - 0 1"));
        // King vs pawn
        assertNotDraw(FEN.toBoard("7k/8/8/8/2P5/8/8/4K3 b - - 0 1"));
        // King vs rook
        assertNotDraw(FEN.toBoard("8/5k2/8/8/8/1K6/5r2/8 w - - 0 1"));
        // king vs queen
        assertNotDraw(FEN.toBoard("1k6/8/5q2/2K5/8/8/8/8 w - - 0 1"));
        // Three knights
        assertNotDraw(FEN.toBoard("3K4/8/1k6/3nnn2/8/8/8/8 b - - 0 1"));
        // King vs knight and bishop
        assertNotDraw(FEN.toBoard("8/3K4/k7/3N4/8/2B5/8/8 w - - 0 1"));

        // Draw if opponent does not want to loose (see https://rustic-chess.org/board_functionality/detecting_fide_draws.html#draw-by-insufficient-material-rule)
        // King vs two knights (see https://en.wikipedia.org/wiki/Two_knights_endgame)
        assertDrawWithOpponentHelp(FEN.toBoard("6k1/8/3N1NK1/8/8/8/8/8 w - - 1 2"));
        // Bishop vs bishop of opposite color
        assertDrawWithOpponentHelp(FEN.toBoard("8/3K4/k7/8/3b4/8/4B3/8 b - - 0 1"));
        // knight vs knight
        assertDrawWithOpponentHelp(FEN.toBoard("8/8/8/5k2/2n5/7N/2K5/8 b - - 0 1"));
        // Bishop vs knight
        assertDrawWithOpponentHelp(FEN.toBoard("7k/1n6/8/8/2B5/8/8/4K3 b - - 0 1"));
        // King vs 2 knights
        assertDrawWithOpponentHelp(FEN.toBoard("8/1n6/4n3/8/8/2k5/8/4K3 b - - 0 1"));
    }
    
    private void assertDraw(Board board) {
        assertBool(board, "isInsufficientMaterial", Draw::isInsufficientMaterial, true);
        assertBool(board, "isInsufficientMaterialFIDERule", Draw::isInsufficientMaterialFIDERule, true);
        assertBool(board, "isEffectiveDraw", Draw::isEffectiveDraw, true);
        assertBool(board, "isDraw", b -> Draw.isDraw(b, mg), true);
    }
    
    private void assertDrawWithOpponentHelp(Board board) {
        assertBool(board, "isInsufficientMaterial", Draw::isInsufficientMaterial, true);
        assertBool(board, "isInsufficientMaterialFIDERule", Draw::isInsufficientMaterialFIDERule, false);
        assertBool(board, "isEffectiveDraw", Draw::isEffectiveDraw, true);
        assertBool(board, "isDraw", b -> Draw.isDraw(b, mg), false);
    }
    
    private void assertNotDraw(Board board) {
        assertBool(board, "isInsufficientMaterial", Draw::isInsufficientMaterial, false);
        assertBool(board, "isInsufficientMaterialFIDERule", Draw::isInsufficientMaterialFIDERule, false);
        assertBool(board, "isEffectiveDraw", Draw::isEffectiveDraw, false);
        assertBool(board, "isDraw", b -> Draw.isDraw(b, mg), false);
    }
    
    private void assertBool(Board board, String methodName, Predicate<Board> predicate, boolean expected) {
        assertEquals(expected, predicate.test(board), "Problem at Draw."+methodName+" for FEN "+FEN.toFEN(board));
    }
    
    @Test
    void fiftyMoveRuleTest() {
        Board board = FEN.toBoard("8/8/4k3/4p3/4KP2/8/8/8 b - - 99 148");
        assertFalse(Draw.isFiftyMoveRule(board));
        assertFalse(Draw.isEffectiveDraw(board));
        assertFalse(Draw.isDraw(board, mg));
        board = FEN.toBoard("8/8/3k4/4p3/4KP2/8/8/8 w - - 100 149");
        assertTrue(Draw.isFiftyMoveRule(board));
        assertTrue(Draw.isEffectiveDraw(board));
        assertTrue(Draw.isDraw(board, mg));
    }
    
    private Board build(String fen, String[] moves) {
        final Board board = Board.from(fen);
        Arrays.stream(moves).forEach(m -> play(board, m));
        return board;
    }

    private void play(Board board, String uci) {
        Move mv = TestUtils.getLegalMove(board, Move.fromUCI(uci));
        assertTrue(mg.isLegal(board, mv));
        board.makeMove(mv);
    }

}
