package com.kelseyde.calvin.board;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a single chess move.
 * @param value The move encoded as a 16-bit integer. Bits 0 - 5 represent the start square,
 * bits 6 - 11 represent the end square, and bits 12 - 15 represent special move flags (one of the constants ending with _FLAG).
 * @see <a href="https://www.chessprogramming.org/Encoding_Moves">Chess Programming Wiki</a>.
 */
public record Move(short value) {

    // Special move flags
    /** Flag for a standard move (no special flags) */
    public static final short NO_FLAG = 0b0000;
    /** Flag for a move is en passant pawn capture */
    public static final short EN_PASSANT_FLAG = 0b0001;
    /** Flag for a castling move */
    public static final short CASTLE_FLAG = 0b0010;
    /** Flag for a two squares pawn move */
    public static final short PAWN_DOUBLE_MOVE_FLAG = 0b0011;
    /** Flag for a promotion to a queen */
    public static final short PROMOTE_TO_QUEEN_FLAG = 0b0100;
    /** Flag for a promotion to a knight */
    public static final short PROMOTE_TO_KNIGHT_FLAG = 0b0101;
    /** Flag for a promotion to a rook */
    public static final short PROMOTE_TO_ROOK_FLAG = 0b0110;
    /** Flag for a promotion to a bishop */
    public static final short PROMOTE_TO_BISHOP_FLAG = 0b0111;

    private static final int FROM_MASK = 0b0000000000111111;
    private static final int TO_MASK = 0b0000111111000000;

    /** Constructs a new standard move (no castle or pawn double, en passant, or promotion move)
     * @param from The start square
     * @param to The end square
     */
    public Move(int from, int to) {
        this((short) (from | to << 6));
    }

    /**
     * Constructs a new move from its start, end, and flag
     * @param from The start square
     * @param to The end square
     * @param flag The flag (see constants ending with _FLAG)
     */
    public Move(int from, int to, int flag) {
        this((short) (from | (to << 6) | (flag << 12)));
    }

    /** Gets the start square of this move
     * @return an int
     */
    public int from() {
        return value & FROM_MASK;
    }

    /** Gets the destination square of this move
     * @return an int
    */
    public int to() {
        return (value & TO_MASK) >>> 6;
    }

    /** Gets the flag of this move
     * @return an int
     */
    public int flag() {
        return value >>> 12;
    }

    /** Gets the promotion piece of this move
     * @return a Piece or null if this is not a promotion move
     * @see #isPromotion
     */
    public Piece promoPiece() {
        return switch (flag()) {
            case PROMOTE_TO_QUEEN_FLAG -> Piece.QUEEN;
            case PROMOTE_TO_ROOK_FLAG -> Piece.ROOK;
            case PROMOTE_TO_BISHOP_FLAG -> Piece.BISHOP;
            case PROMOTE_TO_KNIGHT_FLAG -> Piece.KNIGHT;
            default -> null;
        };
    }

    /** Checks if this move is a promotion
     * @return true if this is a promotion move, false otherwise
     * @see #promoPiece
     */
    public boolean isPromotion() {
        return flag() >= PROMOTE_TO_QUEEN_FLAG;
    }

    /** Checks if this move is an en passant capture
     * @return true if this is an en passant move, false otherwise
     */
    public boolean isEnPassant() {
        return flag() == EN_PASSANT_FLAG;
    }

    /** Checks if this move is a castling move
     * @return true if this is a castling move, false otherwise
    */
    public boolean isCastling() {
        return flag() == CASTLE_FLAG;
    }

    /** Checks if this move is a two squares pawn move
     * @return true if this is a two squares pawn move, false otherwise
     */
    public boolean isPawnDoubleMove() {
        return flag() == PAWN_DOUBLE_MOVE_FLAG;
    }

    /**
     * Checks if this move matches another move.
     * <br>A move matches this if their start and end positions are the same and this is not a promotion move, or both moves have the same promotion piece.
     * @param move The move to compare to
     * @return true if this move matches the other move, false otherwise
     */
    public boolean matches(Move move) {
        if (move == null) return false;
        boolean squareMatch = from() == move.from() && to() == move.to();
        boolean promotionMatch = Optional.ofNullable(promoPiece())
                .map(piece -> piece.equals(move.promoPiece()))
                .orElse(true);
        return squareMatch && promotionMatch;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Move move)) return false;
        return this.from() == move.from() && this.to() == move.to() && this.flag() == move.flag();
    }

    /**
     * Generates a {@link Move} from combined algebraic notation (e.g. "e2e4"), as used in the UCI protocol.
     * Special case promotion: "a2a1q" - values 'q' | 'b' | 'r' | 'n'
     * @param uci The move in UCI notation
     * @return A move.
     * <br><b>Warning: this method does set the special move flag except in case of promotion.</b>
     * One way to get a valid move is to find a legal move whose matches method returns true for returned move.
     * @see #matches(Move)
     */
    public static Move fromUCI(String uci) {
        int from = Square.fromNotation(uci.substring(0, 2));
        int to = Square.fromNotation(uci.substring(2, 4));

        int flag = NO_FLAG;
        if (uci.length() == 5) {
            String pieceCode = uci.substring(4, 5);
            Piece promotionPieceType = Arrays.stream(Piece.values())
                    .filter(entry -> entry.code().equalsIgnoreCase(pieceCode))
                    .findAny().orElseThrow();
            flag = Piece.promoFlag(promotionPieceType);
        }
        return new Move(from, to, flag);
    }

    /**
     * Generates a {@link Move} from combined algebraic notation (e.g. "e2e4"), as used in the UCI protocol.
     * Special case promotion: "a2a1q" - values 'q' | 'b' | 'r' | 'n'
     * @param uci The move in UCI notation
     * <br><b>Warning: this method ignores the promotion character of the uci notation.</b>
     * @param flag The special move flag (see constants ending with _FLAG)
     * @return A move.
     * <br>Warning: this method does set the special move flag except in case of promotion.
     */
       public static Move fromUCI(String uci, int flag) {
        int from = Square.fromNotation(uci.substring(0, 2));
        int to = Square.fromNotation(uci.substring(2, 4));
        return new Move(from, to, flag);
    }

    /**
     * Generates a UCI representation of a move (e.g. "e2e4").
     * @param move The move
     * @return The UCI notation
     */
    public static String toUCI(Move move) {
        if (move == null) return "-";
        final String notation = Square.toNotation(move.from()) + Square.toNotation(move.to());
        final Piece promoPiece = move.promoPiece();
		return promoPiece == null ? notation : notation + promoPiece.code();
    }

	@Override
	public String toString() {
		return toUCI(this);
	}
}
