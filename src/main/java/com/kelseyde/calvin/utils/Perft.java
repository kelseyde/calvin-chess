package com.kelseyde.calvin.utils;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.Move;
import com.kelseyde.calvin.movegen.MoveGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * href="https://www.chessprogramming.org/Perft">Perft, ('Performance Test')</a> is a Performance Test is a debugging function
 * that walks the move generation tree of strictly legal moves to count all the leaf nodes of a certain depth,
 * which can be compared to predetermined values and used to isolate bugs.
 */
public class Perft {

    /**
     * The results of a Perft (Performance Test) calculation.
     */
    public static class Result {
        private long searchedNodesCount;
        private long leafNodesCount;
        private final Map<Move, Long> nodesPerMove;

        private Result() {
             this.nodesPerMove = new HashMap<>();
        }
        /** Gets the number of leaf nodes
         * @return a long
         */
        public long leafNodesCount() {
            return leafNodesCount;
        }

        /** Gets the number of nodes for which the move generation has been done
         * @return a long
         */
        public long searchedNodesCount() {
            return searchedNodesCount;
        }

        /** Gets the number of nodes per move at first depth
         * @return a map of moves to the number of nodes
         */
        public Map<Move, Long> divide() {
            return nodesPerMove;
        }
    }
    
    /** Performs a Perft (Performance Test) calculation.
     * @return a non null result
     */
    public Result perft(Board board, int depth) {
        final MoveGenerator movegen = new MoveGenerator();
        final Result result = new Result();
        result.leafNodesCount = perft(board, movegen, result, depth, depth);
        return result;
    }

    private long perft(Board board, MoveGenerator movegen, Result result, int depth, int originalDepth) {
        result.searchedNodesCount++;
        List<Move> moves = movegen.generateMoves(board);
        if (depth == 1) {
            return moves.size();
        }
        long leafNodesCount = 0;
        for (Move move : moves) {
            board.makeMove(move);
            long moveCount = perft(board, movegen, result, depth - 1, originalDepth);
            if (depth == originalDepth) {
                result.nodesPerMove.put(move, moveCount);
            }
            leafNodesCount += moveCount;
            board.unmakeMove();
        }
        return leafNodesCount;
    }
}
