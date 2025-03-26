package com.kelseyde.calvin.utils.notation;

import com.kelseyde.calvin.board.Board;
import com.kelseyde.calvin.board.ChessVariant;
import com.kelseyde.calvin.board.Draw;
import com.kelseyde.calvin.board.Move;
import com.kelseyde.calvin.movegen.MoveGenerator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Board to <a href="https://en.wikipedia.org/wiki/Portable_Game_Notation">Portable General Notation</a> converter.
 */
public class PGN {
    /** The event tag name */
    public static final String EVENT_TAG = "Event";
    /** The site tag name */
    public static final String SITE_TAG = "Site";
    /** The date tag name */
    public static final String DATE_TAG = "Date";
    /** The round tag name */
    public static final String ROUND_TAG = "Round";
    /** The FEN tag name */
    public static final String FEN_TAG = "FEN";
    /** The variant tag name */
    public static final String VARIANT_TAG = "Variant";
    /** The result tag name */
    public static final String RESULT_TAG = "Result";
    /** The white tag name */
    public static final String WHITE_TAG = "White";
    /** The black tag name */
    public static final String BLACK_TAG = "Black";
    
    /** The unknown tag value */
    public static final String UNKNOWN = "?";

    /** The result white won tag value */
    public static final String WHITE_WON = "1-0";
    /** The result black won tag value */
    public static final String BLACK_WON = "0-1";
    /** The result draw tag value */
    public static final String DRAW = "1/2-1/2";
    /** The result playing tag value */
    public static final String PLAYING = "*";
    
    private final Map<String, String> tagPairs;
    private final Board board;

    /** Creates a new PGN converter.
     * <br>This creates a PGN representation of the given board with the default tags.
     * <br>You can then add/replace tags with the setTag method and get the PGN with the toString method.
     * @param board the board to convert
     * @see #setTag(String, String)
     * @see #toString()
     */
    public PGN(Board board) {
        this.board = board.copy();
        this.tagPairs = new LinkedHashMap<>();
        // Adds default tags
        tagPairs.put(EVENT_TAG, UNKNOWN);
        tagPairs.put(SITE_TAG, UNKNOWN);
        tagPairs.put(DATE_TAG, "????.??.??");
        tagPairs.put(ROUND_TAG, UNKNOWN);
        tagPairs.put(WHITE_TAG, UNKNOWN);
        tagPairs.put(BLACK_TAG, UNKNOWN);
        tagPairs.put(RESULT_TAG, getResult(board));
    }

    /** Sets a tag pair.
     * @param name the tag name
     * @param value the tag value
     * @see #toString()
     */
    public void setTag(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("Name or value is null!");
        }
        tagPairs.put(name, value);
    }
    
    /** 
     * Returns the pgn representation of the board
     */
    @Override
    public String toString() {
        final StringBuilder pgn = new StringBuilder();
        tagPairs.forEach((key, value) -> tagPair(pgn, key, value));

        final List<Move> moves = Arrays.asList(board.getMoves());
        final int moveCount = board.getPly();
        for (int i = 0; i < moveCount; i++) {
            board.unmakeMove();
        }
        final String fen = FEN.toFEN(board);
        if (board.variant()==ChessVariant.CHESS960) {
            tagPair(pgn, VARIANT_TAG, "Chess960");
        }
        if (!FEN.STARTPOS.equals(fen)) {
            // If not a standard start position, the FEN tag is required
            tagPair(pgn,FEN_TAG, fen);
        }

        pgn.append('\n');

        final StringBuilder movesBuilder = new StringBuilder();
        for (int plyCount = 0; plyCount < moveCount; plyCount++) {
            final Move move = moves.get(plyCount);
            if (plyCount % 2 == 0) {
                addToken(pgn, movesBuilder, Integer.toString(plyCount / 2 + 1)+'.');
            }
            addToken(pgn, movesBuilder, SAN.fromMove(move, board));
            board.makeMove(move);
        }
        pgn.append(movesBuilder);
        return pgn.toString();
    }

    /**
     * Converts a Board to a <a href="https://en.wikipedia.org/wiki/Portable_Game_Notation">Portable General Notation</a>.
     * @param board the board to convert
     * @return the PGN representation of the board. Each element is a line of the PGN file.
     */
    public static String toPGN(Board board) {
        return new PGN(board).toString();
    }
    
    private static String getResult(Board board) {
        final MoveGenerator moveGenerator = new MoveGenerator();
        if (Draw.isDraw(board, moveGenerator)) {
            return DRAW;
        }
        if (moveGenerator.isCheck(board, board.isWhite()) && moveGenerator.generateMoves(board).isEmpty()) {
            return board.isWhite() ? BLACK_WON : WHITE_WON;
        }
        return PLAYING;
    }

    private static void tagPair(StringBuilder builder, String tagName, String value) {
        builder.append('[').append(tagName).append(" \"").append(value).append("\"]").append('\n');
    }

    private static void addToken(StringBuilder pgn, StringBuilder builder, String token) {
        if (builder.length() + token.length() >= 80) {
            pgn.append(builder);
            pgn.append('\n');
            builder.setLength(0);
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(token);
    }
}
