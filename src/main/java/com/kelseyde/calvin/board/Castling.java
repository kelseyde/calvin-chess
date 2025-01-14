package com.kelseyde.calvin.board;

/**
 * Utility class to handle castling rights. Especially important for (D)FRC, where the starting positions of the rooks
 * and king can vary, and the logic for handling castle moves is more complex.
 */
public class Castling {

    // For standard chess we can use some hardcoded shortcut values to simplify the logic
    public static class Standard {
        // Masks for the squares that must be unoccupied for legal castling
        public static final long WHITE_QUEENSIDE_TRAVEL_MASK = 0x000000000000000EL;
        public static final long WHITE_KINGSIDE_TRAVEL_MASK = 0x0000000000000060L;
        public static final long BLACK_QUEENSIDE_TRAVEL_MASK = WHITE_QUEENSIDE_TRAVEL_MASK << (7 * 8);
        public static final long BLACK_KINGSIDE_TRAVEL_MASK = WHITE_KINGSIDE_TRAVEL_MASK  << (7 * 8);

        // Masks for the squares that must not be attacked for legal castling
        public static final long WHITE_QUEENSIDE_SAFE_MASK = 0x000000000000001CL;
        public static final long WHITE_KINGSIDE_SAFE_MASK = WHITE_QUEENSIDE_SAFE_MASK << 2;
        public static final long BLACK_QUEENSIDE_SAFE_MASK = WHITE_QUEENSIDE_SAFE_MASK << (7 * 8);
        public static final long BLACK_KINGSIDE_SAFE_MASK = WHITE_KINGSIDE_SAFE_MASK  << (7 * 8);

        public static long travelSquares(boolean white, boolean isKingside) {
            if (isKingside) return white ? WHITE_KINGSIDE_TRAVEL_MASK : BLACK_KINGSIDE_TRAVEL_MASK;
            else return white ? WHITE_QUEENSIDE_TRAVEL_MASK : BLACK_QUEENSIDE_TRAVEL_MASK;
        }

        public static long safeSquares(boolean white, boolean isKingside) {
            if (isKingside) return white ? WHITE_KINGSIDE_SAFE_MASK : BLACK_KINGSIDE_SAFE_MASK;
            else return white ? WHITE_QUEENSIDE_SAFE_MASK : BLACK_QUEENSIDE_SAFE_MASK;
        }

    }

    // Constants to represent shifts and encoding limits
    public static final int NO_ROOK = 64;
    private static final int SQUARE_MASK = 0x7F; // Mask to allow 7 bits, covering 0-64 range
    private static final int WK_SHIFT = 21; // White kingside rook (uppermost)
    private static final int WQ_SHIFT = 14; // White queenside rook
    private static final int BK_SHIFT = 7;  // Black kingside rook
    private static final int BQ_SHIFT = 0;  // Black queenside rook (lowermost)

    public static int rookFrom(boolean kingside, boolean white) {
        // (Standard chess) starting position for rooks
        if (kingside) {
            return white ? 7 : 63;
        } else {
            return white ? 0 : 56;
        }
    }

    public static int rookTo(boolean kingside, boolean white) {
        // Castling destination for rooks
        if (kingside) {
            return white ? 5 : 61;
        } else {
            return white ? 3 : 59;
        }
    }

    public static int kingTo(boolean kingside, boolean white) {
        // Castling destination for king
        return white ? (kingside ? 6 : 2) : (kingside ? 62 : 58);
    }

    public static boolean isKingside(int from, int to) {
        // The king is always inbetween the rooks
        return from < to;
    }

    public static int empty() {
        // All rooks set to NO_ROOK, meaning no castling rights
        return (NO_ROOK << WK_SHIFT) | (NO_ROOK << WQ_SHIFT) | (NO_ROOK << BK_SHIFT) | (NO_ROOK << BQ_SHIFT);
    }

    public static int startpos() {
        // Starting castling rights (standard chess only)
        return from(0, 7, 56, 63);
    }

    public static int from(int wk, int wq, int bk, int bq) {
        // Constructs castling rights from the starting rook squares
        return (encode(wk) << WK_SHIFT) | (encode(wq) << WQ_SHIFT) |
                (encode(bk) << BK_SHIFT) | (encode(bq) << BQ_SHIFT);
    }

    public static int getRook(int rights, boolean kingside, boolean white) {
        // Gets the starting rook square for the given side
        int shift = shift(kingside, white);
        return decode((rights >> shift) & SQUARE_MASK);
    }

    public static int setRook(int rights, boolean kingside, boolean white, int sq) {
        // Sets the starting rook square for the given side
        int shift = shift(kingside, white);
        rights &= ~(SQUARE_MASK << shift); // Clear the bits at the target shift
        rights |= (encode(sq) << shift);    // Set the encoded square at the shift
        return rights;
    }

    public static int clearRook(int rights, boolean kingside, boolean white) {
        // Unsets the starting rook square for the given side
        return setRook(rights, kingside, white, NO_ROOK);
    }

    public static int clearSide(int rights, boolean white) {
        // Unsets the starting rook squares for the given side
        return clearRook(clearRook(rights, true, white), false, white);
    }

    public static boolean kingsideAllowed(int rights, boolean white) {
        // Checks if kingside castling is allowed for the given side
        int shift = shift(true, white);
        return decode((rights >> shift) & SQUARE_MASK) != NO_ROOK;
    }

    public static boolean queensideAllowed(int rights, boolean white) {
        // Checks if queenside castling is allowed for the given side
        int shift = shift(false, white);
        return decode((rights >> shift) & SQUARE_MASK) != NO_ROOK;
    }

    public static int encode(int sq) {
        // Encodes the rook square to the castling rights (0-64 range, with 64 representing no rook)
        return (sq >= 0 && sq <= 64) ? sq : NO_ROOK;
    }

    public static int decode(int mask) {
        // Decodes the castling rights to the rook square (0-64 range, with 64 representing no rook)
        return (mask & SQUARE_MASK) <= 64 ? mask & SQUARE_MASK : NO_ROOK;
    }

    private static int shift(boolean kingside, boolean white) {
        // Helper to determine the shift based on side and color
        if (white) {
            return kingside ? WK_SHIFT : WQ_SHIFT;
        } else {
            return kingside ? BK_SHIFT : BQ_SHIFT;
        }
    }

}
