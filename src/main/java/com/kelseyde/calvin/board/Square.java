package com.kelseyde.calvin.board;

import java.util.List;

public class Square {

    public static final int COUNT = 64;
    public static final long ALL = ~0L;
    public static final long NONE = 0L;

    public static int of(int rank, int file) {
        return (rank << 3) + file;
    }

    public static int flipRank(int sq) {
        return sq ^ 56;
    }

    public static int flipFile(int sq) {
        return sq ^ 7;
    }

    public static boolean isValid(int sq) {
        return sq >= 0 && sq < Square.COUNT;
    }

    public static String toNotation(int sq) {
        return File.toNotation(sq) + Rank.toRankNotation(sq);
    }

    public static int fromNotation(String algebraic) {
        int xOffset = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h').indexOf(algebraic.charAt(0));
        int yAxis = (Integer.parseInt(Character.valueOf(algebraic.charAt(1)).toString()) - 1) * 8;
        return yAxis + xOffset;
    }

}
