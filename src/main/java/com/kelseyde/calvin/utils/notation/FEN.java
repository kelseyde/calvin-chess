package com.kelseyde.calvin.utils.notation;

import com.kelseyde.calvin.board.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        private static final String noPieceCode = "x";
        private long whitePawns = 0L;
        private long whiteKnights = 0L;
        private long whiteBishops = 0L;
        private long whiteRooks = 0L;
        private long whiteQueens = 0L;
        private long whiteKing = 0L;
        private long blackPawns = 0L;
        private long blackKnights = 0L;
        private long blackBishops = 0L;
        private long blackRooks = 0L;
        private long blackQueens = 0L;
        private long blackKing = 0L;

        PiecesParser (String[] files) {
            List<List<String>> rankFileHash = Arrays.stream(files)
                    .map(file -> Arrays.stream(file.split(""))
                            .flatMap(this::parseSquare)
                            .toList())
                    .collect(Collectors.toList());
            if (rankFileHash.size() != 8) {
                throw new IllegalArgumentException("Illegal FEN: rank count is not 8!");
            }
            Collections.reverse(rankFileHash);
            for (int rankIndex = 0; rankIndex < rankFileHash.size(); rankIndex++) {
                final List<String> rank = rankFileHash.get(rankIndex);
                if (rank.size() != 8) {
                    throw new IllegalArgumentException("Illegal FEN: file count is not 8!");
                }
                for (int fileIndex = 0; fileIndex < rank.size(); fileIndex++) {
                    final int square = Square.of(rankIndex, fileIndex);
                    final String squareValue = rank.get(fileIndex);
                    final long squareBB = Bits.of(square);
                    switch (squareValue) {
                        case "P" -> whitePawns |= squareBB;
                        case "N" -> whiteKnights |= squareBB;
                        case "B" -> whiteBishops |= squareBB;
                        case "R" -> whiteRooks |= squareBB;
                        case "Q" -> whiteQueens |= squareBB;
                        case "K" -> whiteKing |= squareBB;
                        case "p" -> blackPawns |= squareBB;
                        case "n" -> blackKnights |= squareBB;
                        case "b" -> blackBishops |= squareBB;
                        case "r" -> blackRooks |= squareBB;
                        case "q" -> blackQueens |= squareBB;
                        case "k" -> blackKing |= squareBB;
                        case noPieceCode -> {
                            // No piece, do nothing
                        }
                        default -> illegalPiece(squareValue);
                    }
                }
            }
        }
        
        private Stream<String> parseSquare(String square) {
            if (square.length() != 1) {
                // A rank was empty
                throw new IllegalArgumentException("Illegal FEN a rank can't be empty!");
            }
            if (Character.isLetter(square.charAt(0))) {
                if (noPieceCode.equals(square)) {
                    illegalPiece(square);
                } else {
                    return Stream.of(square);
                }
            }
            return IntStream.range(0, Integer.parseInt(square)).mapToObj(i -> noPieceCode);
        }

        private void illegalPiece(String piece) {
            throw new IllegalArgumentException("Illegal FEN: " + piece + " is not a valid piece!");
        }

        private void fillBoard(Board board) {
            board.setPawns(whitePawns | blackPawns);
            board.setKnights(whiteKnights | blackKnights);
            board.setBishops(whiteBishops | blackBishops);
            board.setRooks(whiteRooks | blackRooks);
            board.setQueens(whiteQueens | blackQueens);
            board.setKings(whiteKing | blackKing);
            board.setWhitePieces(whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing);
            board.setBlackPieces(blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing);
            board.setPieces(calculatePieceList(board));
        }
        
        private Piece[] calculatePieceList(Board board) {
            final Piece[] pieceList = new Piece[Square.COUNT];
            for (int square = 0; square < Square.COUNT; square++) {
                final long squareMask = Bits.of(square);
                if ((squareMask & board.getPawns()) != 0)           pieceList[square] = Piece.PAWN;
                else if ((squareMask & board.getKnights()) != 0)    pieceList[square] = Piece.KNIGHT;
                else if ((squareMask & board.getBishops()) != 0)    pieceList[square] = Piece.BISHOP;
                else if ((squareMask & board.getRooks()) != 0)      pieceList[square] = Piece.ROOK;
                else if ((squareMask & board.getQueens()) != 0)     pieceList[square] = Piece.QUEEN;
                else if ((squareMask & board.getKings()) != 0)      pieceList[square] = Piece.KING;
            }
            return pieceList;
        }
    }

    /** 
     * Converts a Forsyth-Edwards Notation string to a Board
     * @param fen the Forsyth-Edwards Notation string. Both half move clock and move counter are optional.
     * <br>If half move clock is not provided, it is set to 0.
     * <br>Move counter is ignored, due to a limitation of Board class that does not support it when played moves are not provided.
     * @return the Board
     * @throws IllegalArgumentException if the FEN string is not valid
     */
    public static Board toBoard(String fen) {
            if (fen==null) {
                throw new IllegalArgumentException();
            }
            final String[] parts = fen.split(" ");
            final PiecesParser piecesParser = new PiecesParser(parts[0].split("/"));

            final boolean whiteToMove = parseSideToMove(parts[1]);
            final int castlingRights = parseCastlingRights(parts[2], piecesParser.whiteRooks, piecesParser.blackRooks, Bits.next(piecesParser.whiteKing), Bits.next(piecesParser.blackKing));
            final int enPassantFile = parseEnPassantFile(parts[3]);
            final int fiftyMoveCounter = parts.length > 4 ? parseFiftyMoveCounter(parts[4]) : 0;
            // This implementation does not require the full move counter (parts[5]).

            final Board board = new Board();
            piecesParser.fillBoard(board);
            board.setWhite(whiteToMove);
            board.getState().setRights(castlingRights);
            board.getState().setEnPassantFile(enPassantFile);
            board.getState().setHalfMoveClock(fiftyMoveCounter);
            board.getState().setKey(Key.generateKey(board));
            board.getState().setPawnKey(Key.generatePawnKey(board));
            board.getState().setNonPawnKeys(Key.generateNonPawnKeys(board));

            return board;
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

    private static int parseCastlingRights(String castlingRights, long whiteRooks, long blackRooks, int whiteKing, int blackKing) {
        if (castlingRights.length() > 4) {
            throw new IllegalArgumentException("Invalid castling rights! " + castlingRights);
        }
        int rights = Castling.empty();
        for (int i = 0; i < castlingRights.length(); i++) {
            char right = castlingRights.charAt(i);
            switch (right) {
                case 'K' -> rights = Castling.setRook(rights, true, true, findRook(whiteRooks, true, true));
                case 'Q' -> rights = Castling.setRook(rights, false, true, findRook(whiteRooks, true, false));
                case 'k' -> rights = Castling.setRook(rights, true, false, findRook(blackRooks, false, true));
                case 'q' -> rights = Castling.setRook(rights, false, false, findRook(blackRooks, false, false));
                case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' -> {
                    // Shredder FEN: White rooks on specified files
                    int file = File.fromNotation(right);
                    int kingFile = File.of(whiteKing);
                    if (file < kingFile) {
                        rights = Castling.setRook(rights, false, true, Square.of(0, file)); // Queenside
                    } else {
                        rights = Castling.setRook(rights, true, true, Square.of(0, file));  // Kingside
                    }
                }
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' -> {
                    // Shredder FEN: Black rooks on specified files
                    int file = File.fromNotation(Character.toUpperCase(right));
                    int kingFile = File.of(blackKing);
                    if (file < kingFile) {
                        rights = Castling.setRook(rights, false, false, Square.of(7, file)); // Queenside
                    } else {
                        rights = Castling.setRook(rights, true, false, Square.of(7, file));  // Kingside
                    }
                }
                case '-' -> {
                    // No castling rights, so return empty rights directly
                    return Castling.empty();
                }
                default -> throw new IllegalArgumentException("Invalid castling right! " + right);
            }
        }
        return rights;
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

    private static int parseEnPassantFile(String enPassantSquare) {
        if (enPassantSquare.equals("-")) {
            return -1;
        }
        int square = Square.fromNotation(enPassantSquare);
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

    private static int findRook(long rooks, boolean white, boolean kingside) {

        long firstRank = white ? Rank.FIRST : Rank.EIGHTH;
        long firstRankRooks = rooks & firstRank;

        if (Bits.count(firstRankRooks) == 0) {
            throw new IllegalArgumentException("Illegal FEN: castling rights with no rooks on the first rank!");
        }

        if (Bits.count(firstRankRooks) == 1) {
            return Bits.next(firstRankRooks);
        }

        List<Integer> squares = Arrays.stream(Bits.collect(firstRankRooks))
                .boxed()
                .sorted(Comparator.comparing(File::of))
                .toList();

        return kingside ? squares.get(squares.size() - 1) : squares.get(0);

    }


}
