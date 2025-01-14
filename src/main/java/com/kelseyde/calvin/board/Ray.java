package com.kelseyde.calvin.board;

public class Ray {

    /**
     * Calculates the ray (bitboard) between two squares on the chessboard.
     */
    public static long between(int from, int to) {
        if (!Square.isValid(from) || !Square.isValid(to) || (from == to)) {
            return 0L;
        }
        int offset = direction(from, to);
        if (offset == 0) return 0L;
        long ray = 0L;
        int sq = from + offset;
        while (Square.isValid(sq) && sq != to) {
            ray |= Bits.of(sq);
            sq += offset;
        }
        return ray;
    }

    /**
     * Determines the direction offset between two squares on the chessboard.
     */
    private static int direction(int from, int to) {
        int startRank = Rank.of(from);
        int endRank = Rank.of(to);
        int startFile = File.of(from);
        int endFile = File.of(to);
        if (startRank == endRank) {
            return from > to ? -1 : 1;
        } else if (startFile == endFile) {
            return from > to ? -8 : 8;
        } else if (Math.abs(startRank - endRank) == Math.abs(startFile - endFile)) {
            return from > to ? (from - to) % 9 == 0 ? -9 : -7 : (to - from) % 9 == 0 ? 9 : 7;
        } else if (startRank + startFile == endRank + endFile) {
            return from > to ? -9 : 9;
        }
        return 0;
    }

}
