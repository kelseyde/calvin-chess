package com.kelseyde.calvin.board;

import java.util.function.Consumer;

/** A class for printing chess boards or bitboards */
public class BoardPrinter {
	@FunctionalInterface
	private interface CellBuilder<T> {
		String get(T board, int square);
	}

	static final String BORDER = "+---+---+---+---+---+---+---+---+";

	private boolean withCoordinates = true;
	private final Consumer<String> out;

	/**
	 * Creates a new {@link BoardPrinter} instance.
	 * @param out the consumer to call when a line is printed
	 * @throws IllegalArgumentException if {@code out} is {@code null}
	 */
	public BoardPrinter(Consumer<String> out) {
		if (out==null) {
			throw new IllegalArgumentException();
		}
		this.out = out;
	}
	
	/** Sets the printer to print with or without coordinates.
	 * @param withCoordinates the new value. Default value is true
	 * @return this instance. */
	public BoardPrinter withCoordinates(boolean withCoordinates) {
		this.withCoordinates = withCoordinates;
		return this;
	}

	/** Prints a board.
	 * @param board the board to print
	 */
	public void print(Board board) {
		print(board, this::getPieceCell);
 	}

	/** Prints a bitboard.
	 * @param bitBoard the bitboard to print
	 */
	public void print(long bitBoard) {
		print(bitBoard, this::getBitCell);
 	}

	private String getPieceCell(Board board, int square) {
		final Piece piece = board.pieceAt(square);
		if (piece == null) {
			return " ";
		}
		final boolean white = (board.getWhitePieces() & Bits.of(square)) != 0;
		return white ? piece.code().toUpperCase() : piece.code();
	}
	
	private String getBitCell(Long board, int sq) {
        boolean piece = (board & (Bits.of(sq))) != 0;
        return piece ? "1" : " ";
    }

	private <T>void print(T board, CellBuilder<T> cellBuilder) {
		for (int rank = 7; rank >= 0; --rank) {
			out.accept(BORDER);
			final StringBuilder builder = new StringBuilder();
			for (int file = 0; file < 8; ++file) {
				builder.append("| ");
				builder.append(cellBuilder.get(board, Square.of(rank, file)));
				builder.append(' ');
			}
			builder.append('|');
			if (withCoordinates) {
				builder.append(' ');
				builder.append(rank + 1);
			}
			out.accept(builder.toString());
		}
		out.accept(BORDER);
		if (withCoordinates) {
			out.accept("  a   b   c   d   e   f   g   h");
		}
	}
	
	/** Returns a consumer that appends strings to a StringBuilder inserting a newline after each string.
	 * @param builder the StringBuilder to append to
	 * @return a String consumer.
	 */
	public static Consumer<String> getConsumer(StringBuilder builder) {
	    return s -> {
            builder.append(s);
            builder.append('\n');
	    };
	}
}
