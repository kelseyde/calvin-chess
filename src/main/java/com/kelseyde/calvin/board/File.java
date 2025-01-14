package com.kelseyde.calvin.board;

import java.util.Map;

public class File {

    public static final long A = 0b0000000100000001000000010000000100000001000000010000000100000001L;
    public static final long B = 0b0000001000000010000000100000001000000010000000100000001000000010L;
    public static final long C = 0b0000010000000100000001000000010000000100000001000000010000000100L;
    public static final long D = 0b0000100000001000000010000000100000001000000010000000100000001000L;
    public static final long E = 0b0001000000010000000100000001000000010000000100000001000000010000L;
    public static final long F = 0b0010000000100000001000000010000000100000001000000010000000100000L;
    public static final long G = 0b0100000001000000010000000100000001000000010000000100000001000000L;
    public static final long H = 0b1000000010000000100000001000000010000000100000001000000010000000L;

    public static final Map<Integer, String> FILE_CHAR_MAP = Map.of(
            0, "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f", 6, "g", 7, "h"
    );

    public static int of(int sq) {
        return sq & 7;
    }

    public static long toBitboard(int file) {
        return 0x0101010101010101L << file;
    }

    public static String toNotation(int sq) {
        return FILE_CHAR_MAP.get(of(sq));
    }

    public static int fromNotation(char file) {
        return FILE_CHAR_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(Character.toString(file)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

}
