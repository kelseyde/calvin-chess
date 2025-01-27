package com.kelseyde.calvin.board;

import java.util.List;

/** A utility class about squares on a chess board. */
public final class Square {
    private Square() {
        super();
    }

    /** The number of squares on a chess board. */
    public static final int COUNT = 64;
    /** A bitboard with all squares set. */
    public static final long ALL = ~0L;
    /** A bitboard with no squares set. */
    public static final long NONE = 0L;
    /** A bitboard with all black squares set. */
    public static final long BLACK = 0xAA55AA55AA55AA55L;
    /** A bitboard with all white squares set. */
    public static final long WHITE = 0x55AA55AA55AA55AAL;

    /** Returns the bitbord of a square given its rank and file.
     * @param rank the rank of the square
     * @param file the file of the square
     * @return the bitboard of the square
    */
    public static int of(int rank, int file) {
        return (rank << 3) + file;
    }

    /** Flips the rank of a square (flip vertically) <a href="https://www.chessprogramming.org/Flipping_Mirroring_and_Rotating">on the board</a>.
     * <br><b>Warning</b>: this method makes no validation of the square index, passing an invalid index may produce unexpected results.
     * @param sq the square index to flip
     * @return the flipped square index
    */
    public static int flipRank(int sq) {
        return sq ^ 56;
    }

    /** Flips the file of a square (flip horizontally) <a href="https://www.chessprogramming.org/Flipping_Mirroring_and_Rotating">on the board</a>.
     * <br><b>Warning</b>: this method makes no validation of the square index, passing an invalid index may produce unexpected results.
     * @param sq the square index to flip
     * @return the flipped square index
    */
    public static int flipFile(int sq) {
        return sq ^ 7;
    }

    /** Checks if a square is valid index.
     * @param sq the square index to check
     * @return true if the square is valid index, false otherwise
    */
    public static boolean isValid(int sq) {
        return sq >= 0 && sq < Square.COUNT;
    }

    /** Converts a square index to its <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)#Naming_the_squares">algebraic notation</a>.
     * @param sq the square index to convert
     * @return the algebraic notation of the square
     * @throws IllegalArgumentException if the square index is invalid
     * @see #isValid(int)
    */
    public static String toNotation(int sq) {
        if (!isValid(sq)) {
            throw new IllegalArgumentException("Invalid square index: " + sq);
        }
        return File.toNotation(sq) + Rank.toRankNotation(sq);
    }

    /** Converts an <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)#Naming_the_squares">algebraic notation</a> to a square index.
     * @param algebraic the algebraic notation to convert
     * @return the square index of the algebraic notation
     * @throws IllegalArgumentException if the algebraic notation is invalid
    */
    public static int fromNotation(String algebraic) {
        if (algebraic.length() == 2) {
            final int xOffset = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h').indexOf(algebraic.charAt(0));
            final int yAxis = Integer.parseInt(Character.toString(algebraic.charAt(1))) - 1;
            if (xOffset >= 0 && yAxis >= 0 && yAxis<8) {
                return yAxis*8 + xOffset;
            }
        }
        throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
    }
}
