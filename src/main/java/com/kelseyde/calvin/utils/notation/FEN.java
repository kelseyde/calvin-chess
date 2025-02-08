package com.kelseyde.calvin.utils.notation;

import static com.kelseyde.calvin.board.Piece.*;

import com.kelseyde.calvin.board.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** 
 * Converts a Board to and from a <a href="https://www.chessprogramming.org/Forsyth-Edwards_Notation">Forsyth-Edwards Notation</a>
 */
public class FEN {
    private FEN() {
        super();
    }

    /**
     * The standard starting position in Forsyth-Edwards Notation.
     */
    public static final String STARTPOS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static final class PiecesParser {
        private static final String EMPTY_CELL = "x";

        /** Populates a builder with the pieces.
         * @param builder The builder to populate 
         * @param pieces The FEN pieces parts ranks
         * @return
         */
        static int[] parsePieces(BoardBuilder builder, String pieces) {
            final List<List<String>> rankFileHash = Arrays.stream(pieces.split("/"))
                    .map(file -> Arrays.stream(file.split(""))
                            .flatMap(PiecesParser::parseSquare)
                            .toList())
                    .collect(Collectors.toList());
            if (rankFileHash.size() != 8) {
                throw new IllegalArgumentException("Illegal FEN: rank count is not 8!");
            }
            Collections.reverse(rankFileHash);
            final int[] kingsPosition = {-1, -1};
            for (int rankIndex = 0; rankIndex < rankFileHash.size(); rankIndex++) {
                final List<String> rank = rankFileHash.get(rankIndex);
                if (rank.size() != 8) {
                    throw new IllegalArgumentException("Illegal FEN: file count is not 8!");
                }
                for (int fileIndex = 0; fileIndex < rank.size(); fileIndex++) {
                    final int square = Square.of(rankIndex, fileIndex);
                    String squareValue = rank.get(fileIndex);
                    final boolean white = Character.isUpperCase(squareValue.charAt(0));
                    if (white) squareValue = squareValue.toLowerCase();
                    switch (squareValue) {
                        case "p" -> builder.addPiece(square, PAWN, white);
                        case "n" -> builder.addPiece(square, KNIGHT, white);
                        case "b" -> builder.addPiece(square, BISHOP, white);
                        case "r" -> builder.addPiece(square, ROOK, white);
                        case "q" -> builder.addPiece(square, QUEEN, white);
                        case "k" -> {
                            builder.addPiece(square, KING, white);
                            if (white) {
                                kingsPosition[0] = square;
                            } else {
                                kingsPosition[1] = square;
                            }
                        }
                        case EMPTY_CELL -> {
                            // No piece, do nothing
                        }
                        default -> illegalPiece(squareValue);
                    }
                }
            }
            return kingsPosition;
        }
        
        private static Stream<String> parseSquare(String square) {
            if (square.length() != 1) {
                // A rank was empty
                throw new IllegalArgumentException("Illegal FEN a rank can't be empty!");
            }
            if (Character.isLetter(square.charAt(0))) {
                if (EMPTY_CELL.equals(square)) {
                    illegalPiece(square);
                } else {
                    return Stream.of(square);
                }
            }
            return IntStream.range(0, Integer.parseInt(square)).mapToObj(i -> EMPTY_CELL);
        }

        private static void illegalPiece(String piece) {
            throw new IllegalArgumentException(String.format("Illegal FEN: '%s' is not a valid piece!", piece));
        }
    }
    
    private static class CastlingParser {
        private final BitSet hasCastling;
        private final BoardBuilder builder;

        private CastlingParser(BoardBuilder builder) {
            hasCastling = new BitSet();
            this.builder = builder;
        }
        private void set(boolean white, boolean kingside, Integer file) {
            int index = Colour.index(white);
            if (!kingside) {
                index += 2;
            }
            if (hasCastling.get(index)) {
                throw new IllegalArgumentException(String.format("Illegal FEN: %s %s side castling rights are defined multiple times!", Colour.label(white), kingside ? "king":"queen"));
            }
            hasCastling.set(index, white);
            if (file != null) {
                builder.addCastlingRights(white, kingside, file);
            } else {
                builder.addCastlingRights(white, kingside);
            }
        }

        private void parseCastlingRights(String castlingRights, BoardBuilder builder, int whiteKing, int blackKing) {
            if (castlingRights.length() > 4) {
                throw new IllegalArgumentException("Invalid castling rights! " + castlingRights);
            }
            for (int i = 0; i < castlingRights.length(); i++) {
                char right = castlingRights.charAt(i);
                switch (right) {
                    case 'K' -> set(true, true, null);
                    case 'Q' -> set(true, false, null);
                    case 'k' -> set(false, true, null);
                    case 'q' -> set(false, false, null);
                    case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' -> {
                        // Shredder FEN: White rooks on specified files
                        int file = File.fromNotation(right);
                        int kingFile = File.of(whiteKing);
                        set(true, file > kingFile, file);
                    }
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' -> {
                        // Shredder FEN: Black rooks on specified files
                        int file = File.fromNotation(Character.toUpperCase(right));
                        int kingFile = File.of(blackKing);
                        set(false, file > kingFile, file);
                    }
                    case '-' -> {
                        // No castling rights, nothing to do
                    }
                    default -> throw new IllegalArgumentException("Invalid castling right! " + right);
                }
            }
        }
    }

    /** 
     * Converts a Forsyth-Edwards Notation string to a standard board
     * @param fen the Forsyth-Edwards Notation string. Both half move clock and move counter are optional.
     * <br>If half move clock is not provided, it is set to 0.
     * <br>Move counter is ignored, due to a limitation of Board class that does not support it when played moves are not provided.
     * @return the Board
     * @throws IllegalArgumentException if the FEN string is not valid
     */
    public static Board toBoard(String fen) {
        return toBoard(fen, ChessVariant.STANDARD);
    }

    /** 
     * Converts a Forsyth-Edwards Notation string to a Board
     * @param fen the Forsyth-Edwards Notation string. Both half move clock and move counter are optional.
     * <br>If half move clock is not provided, it is set to 0.
     * <br>Move counter is ignored, due to a limitation of Board class that does not support it when played moves are not provided.
     * @param variant the ChessVariant to use
     * @return the Board
     * @throws IllegalArgumentException if the FEN string is not valid
     */
    public static Board toBoard(String fen, ChessVariant variant) {
        if (fen==null || fen.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        final BoardBuilder builder = new BoardBuilder(variant);
        final String[] parts = fen.split(" ");
        final int[] kingPositions = PiecesParser.parsePieces(builder, parts[0]);

        final boolean whiteToMove = parseSideToMove(parts[1]);
        builder.setWhiteToMove(whiteToMove);
        
        new CastlingParser(builder).parseCastlingRights(parts[2], builder, kingPositions[0], kingPositions[1]);
        builder.setEnPassantFile(parseEnPassantFile(parts[3], whiteToMove));
        builder.setFiftyMoveCounter(parts.length > 4 ? parseFiftyMoveCounter(parts[4]) : 0);
        // This implementation does not require the full move counter (parts[5]).

        return builder.build();
    }

    /**
     * Converts a Board to a Forsyth-Edwards Notation string
     * @param board the Board
     * @return the Forsyth-Edwards Notation string
     */
    public static String toFEN(Board board) {
        try {
            StringBuilder sb = new StringBuilder();

            for (int rank = 7; rank >= 0; rank--) {
                int emptySquares = 0;
                for (int file = 0; file < 8; file++) {
                    int square = Square.of(rank, file);
                    Piece piece = board.pieceAt(square);
                    if (piece != null) {
                        if (emptySquares != 0) {
                            sb.append(emptySquares);
                            emptySquares = 0;
                        }
                        long squareBB = Bits.of(square);
                        boolean white = (board.getWhitePieces() & squareBB) != 0;
                        String pieceCode = piece.code();
                        if (white) pieceCode = pieceCode.toUpperCase();
                        sb.append(pieceCode);
                    } else {
                        emptySquares++;
                    }
                }
                if (emptySquares != 0) {
                    sb.append(emptySquares);
                }
                if (rank > 0) {
                    sb.append('/');
                }
            }

            String whiteToMove = toSideToMove(board.isWhite());
            sb.append(" ").append(whiteToMove);

            String castlingRights = toCastlingRights(board, board.getState().getRights());
            sb.append(" ").append(castlingRights);

            String enPassantSquare = toEnPassantSquare(board.getState().getEnPassantFile(), board.isWhite());
            sb.append(" ").append(enPassantSquare);

            String fiftyMoveCounter = toFiftyMoveCounter(board.getState().getHalfMoveClock());
            sb.append(" ").append(fiftyMoveCounter);

            String fullMoveNumber = toFullMoveCounter(board.getPly());
            sb.append(" ").append(fullMoveNumber);

            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException(board.toString(), e);
        }
    }

    private static boolean parseSideToMove(String sideToMove) {
        return switch (sideToMove) {
            case "w" -> true;
            case "b" -> false;
            default -> throw new IllegalArgumentException("Invalid side to move! " + sideToMove);
        };
    }

    private static String toSideToMove(boolean sideToMove) {
        return sideToMove ? "w" : "b";
    }

    private static String toCastlingRights(Board board, int rights) {
        if (rights == Castling.empty()) {
            return "-";
        }
        String rightsString = "";
        int wk = Castling.getRook(rights, true, true);
        if (wk != Castling.NO_ROOK) {
            rightsString += switch (board.variant()) {
                case STANDARD -> "K";
                case CHESS960 -> File.toNotation(wk).toUpperCase();
            };
        }
        int wq = Castling.getRook(rights, false, true);
        if (wq != Castling.NO_ROOK) {
            rightsString += switch (board.variant()) {
                case STANDARD -> "Q";
                case CHESS960 -> File.toNotation(wq).toUpperCase();
            };
        }
        int bk = Castling.getRook(rights, true, false);
        if (bk != Castling.NO_ROOK) {
            rightsString += switch (board.variant()) {
                case STANDARD -> "k";
                case CHESS960 -> File.toNotation(bk);
            };
        }
        int bq = Castling.getRook(rights, false, false);
        if (bq != Castling.NO_ROOK) {
            rightsString += switch (board.variant()) {
                case STANDARD -> "q";
                case CHESS960 -> File.toNotation(bq);
            };
        }
        return rightsString;
    }

    private static int parseEnPassantFile(String enPassantSquare, boolean white) {
        if (enPassantSquare.equals("-")) {
            return -1;
        }
        final int square = Square.fromNotation(enPassantSquare);
        final int expectedRank = white ? 5 : 2;
        if (Rank.of(square) != expectedRank) {
            throw new IllegalArgumentException(String.format("Invalid en passant square! Rank should be %s", Integer.toString(expectedRank+1)));
        }
        return File.of(square);
    }

    private static String toEnPassantSquare(int enPassantFile, boolean white) {
        int rank = white ? 2 : 5;
        if (enPassantFile == -1) {
            return "-";
        }
        return Square.toNotation(Square.of(rank, enPassantFile));
    }

    private static int parseFiftyMoveCounter(String fiftyMoveCounter) {
        return Character.isDigit(fiftyMoveCounter.charAt(0)) ? Integer.parseInt(fiftyMoveCounter) : 0;
    }

    private static String toFiftyMoveCounter(int fiftyMoveCounter) {
        return Integer.toString(fiftyMoveCounter);
    }

    private static String toFullMoveCounter(int ply) {
        return Integer.toString(1 + (ply / 2));
    }
}
