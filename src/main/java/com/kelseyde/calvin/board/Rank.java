package com.kelseyde.calvin.board;

import java.util.Map;

public class Rank {

    public static final long FIRST = 0b0000000000000000000000000000000000000000000000000000000011111111L;
    public static final long SECOND = 0b0000000000000000000000000000000000000000000000001111111100000000L;
    public static final long THIRD = 0b0000000000000000000000000000000000000000111111110000000000000000L;
    public static final long FOURTH = 0b0000000000000000000000000000000011111111000000000000000000000000L;
    public static final long FIFTH = 0b0000000000000000000000001111111100000000000000000000000000000000L;
    public static final long SIXTH = 0b0000000000000000111111110000000000000000000000000000000000000000L;
    public static final long SEVENTH = 0b0000000011111111000000000000000000000000000000000000000000000000L;
    public static final long EIGHTH = 0b1111111100000000000000000000000000000000000000000000000000000000L;

    public static final Map<Integer, String> RANK_CHAR_MAP = Map.of(
            0, "1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6, "7", 7, "8"
    );

    public static int of(int sq) {
        return sq >>> 3;
    }

    public static String toRankNotation(int sq) {
        return RANK_CHAR_MAP.get(of(sq));
    }

}
