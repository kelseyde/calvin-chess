package com.kelseyde.calvin.board;

public class Bits {

    public static int next(final long board) {
        return Long.numberOfTrailingZeros(board);
    }

    public static long pop(long board) {
        return board & (board - 1);
    }

    public static long pop(long bb, int sq) {
        return bb ^ of(sq);
    }

    public static int count(long board) {
        return Long.bitCount(board);
    }

    public static long of(int sq) {
        return 1L << sq;
    }

    public static boolean contains(long bb, int sq) {
        return (bb & of(sq)) != 0;
    }

    public static long north(long board) {
        return board << 8;
    }

    public static long south(long board) {
        return board >>> 8;
    }

    public static long east(long board) {
        return (board << 1) & ~File.A;
    }

    public static long west(long board) {
        return (board >>> 1) & ~File.H;
    }

    public static long northEast(long board) {
        return (board << 9) & ~File.A;
    }

    public static long southEast(long board) {
        return (board >>> 7) & ~File.A;
    }

    public static long northWest(long board) {
        return (board << 7) & ~File.H;
    }

    public static long southWest(long board) {
        return (board >>> 9) & ~File.H;
    }

    public static int[] collect(long bb) {
        int size = count(bb);
        int[] squares = new int[size];
        for (int i = 0; i < size; ++i) {
            squares[i] = next(bb);
            bb = pop(bb);
        }
        return squares;
    }

    @Deprecated(forRemoval = true)
    /** @deprecated
     * Prints the bitboard to the standard output.
     *  Use {@link BoardPrinter#print(long)} or {@link #toString(long)} instead.
     */
    public static void print(long bb) {
    	new BoardPrinter(System.out::println).print(bb);
    	System.out.println();
    }
    
    public static String toString(long board) {
        final StringBuilder builder = new StringBuilder();
        new BoardPrinter(BoardPrinter.getConsumer(builder)).print(board);
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
}
