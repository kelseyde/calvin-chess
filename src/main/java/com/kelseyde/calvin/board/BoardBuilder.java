package com.kelseyde.calvin.board;

import static com.kelseyde.calvin.board.Piece.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A chess board builder. */
public class BoardBuilder {
    private static class StartPositionBuilder {
        private static final Piece[][] KRN = {
            {KNIGHT, KNIGHT, ROOK, KING, ROOK},
            {KNIGHT, ROOK, KNIGHT, KING, ROOK},
            {KNIGHT, ROOK, KING, KNIGHT, ROOK},
            {KNIGHT, ROOK, KING, ROOK, KNIGHT},
            {ROOK, KNIGHT, KNIGHT, KING, ROOK},
            {ROOK, KNIGHT, KING, KNIGHT, ROOK},
            {ROOK, KNIGHT, KING, ROOK, KNIGHT},
            {ROOK, KING, KNIGHT, KNIGHT, ROOK},
            {ROOK, KING, KNIGHT, ROOK, KNIGHT},
            {ROOK, KING, ROOK, KNIGHT, KNIGHT},
        };

        private static void fillFromPositionNumber(BoardBuilder builder, int position) {
            if (position<0 || position>=960) {
                throw new IllegalArgumentException();
            }
            // Add pawns
            IntStream.range(0, 8).forEach(i->builder.addPiece(Square.of(1, i), PAWN, true));
            IntStream.range(0, 8).forEach(i->builder.addPiece(Square.of(6, i), PAWN, false));
            
            // Add bishops
            final int whiteCellBishop = 1+(position % 4)*2;
            builder.addPiece(whiteCellBishop, BISHOP, true);
            builder.addPiece(Square.flipRank(whiteCellBishop), BISHOP, false);
            position = position/4;
            final int blackCellBishop = (position % 4)*2;
            builder.addPiece(blackCellBishop, BISHOP, true);
            builder.addPiece(Square.flipRank(blackCellBishop), BISHOP, false);
            position = position/4;
            
            // We will maintain a list of free cells in order to position the remaining piece
            final List<Integer> freeCells = IntStream.range(0, 8).mapToObj(Integer::valueOf).collect(Collectors.toList());
            freeCells.remove(Integer.valueOf(whiteCellBishop));
            freeCells.remove(Integer.valueOf(Square.flipFile(whiteCellBishop)));
            // Add queens
            final int queenPosition = freeCells.remove(position%6);
            builder.addPiece(Square.of(0, queenPosition), QUEEN, true);
            builder.addPiece(Square.of(7, queenPosition), QUEEN, false);
            
            // Add rest of pieces
            addKRN(builder, freeCells, KRN[position/6], true);
            addKRN(builder, freeCells, KRN[position/6], false);

            // add castling rights
            builder.addCastlingRights(true, true);
            builder.addCastlingRights(true, false);
            builder.addCastlingRights(false, true);
            builder.addCastlingRights(false, false);
        }
        
        private static void addKRN(BoardBuilder builder, List<Integer> positions, Piece[] krn, boolean white) {
            IntStream.range(0, positions.size()).forEach(i -> builder.addPiece(Square.of(white?0:7, positions.get(i)), krn[i], white));
        }        
    }

    private static final class CastlingRights {
        private static final int OUTER_MOST_ROOK = Integer.MAX_VALUE;
        private static final int NO_RIGHT = -1;
        private int whiteKingsideFile = NO_RIGHT;
        private int whiteQueensideFile = NO_RIGHT;
        private int blackKingsideFile = NO_RIGHT;
        private int blackQueensideFile = NO_RIGHT;

        private void add(boolean white, boolean kingside, int file) {
            if (white) {
                if (kingside) whiteKingsideFile = file;
                else whiteQueensideFile = file;
            } else {
                if (kingside) blackKingsideFile = file;
                else blackQueensideFile = file;
            }
        }

        private boolean isEmpty() {
            return whiteKingsideFile == NO_RIGHT &&
                   whiteQueensideFile == NO_RIGHT &&
                   blackKingsideFile == NO_RIGHT &&
                   blackQueensideFile == NO_RIGHT;
        }
        
        private boolean colorHasCastling(boolean white) {
            if (white) {
                return whiteKingsideFile!=NO_RIGHT || whiteQueensideFile!=NO_RIGHT;
            } else {
                return blackKingsideFile!=NO_RIGHT || blackQueensideFile!=NO_RIGHT;
            }
        }

        private boolean bothColorsHasSideCastling(boolean kindSide) {
            if (kindSide) {
                return whiteKingsideFile!=NO_RIGHT && blackKingsideFile!=NO_RIGHT;
            } else {
                return whiteQueensideFile!=NO_RIGHT && blackQueensideFile!=NO_RIGHT;
            }
        }

        private int get(boolean white, boolean kingside) {
            if (white) {
                return kingside ? whiteKingsideFile : whiteQueensideFile;
            } else {
                return kingside ? blackKingsideFile : blackQueensideFile;
            }
        }
    }
    
    private static class BitMaps {
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
        private final long whitePieces;
        private final long blackPieces;
        
        private BitMaps(Piece[] pieceList, long whitePieces) {
            for (int i = 0; i < pieceList.length; i++) {
                final Piece piece = pieceList[i];
                if (piece!=null) {
                    final long squareBit = Bits.of(i);
                    addPiece(squareBit, piece, (whitePieces & squareBit) !=0);
                }
            }
            this.whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
            if (whitePieces != this.whitePieces) {
                throw new IllegalArgumentException();
            }
            blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        }
        
        private void addPiece(long squareBit, Piece piece, boolean white) {
            if (white) {
                switch (piece) {
                    case PAWN -> whitePawns |= squareBit;
                    case KNIGHT -> whiteKnights |= squareBit;
                    case BISHOP -> whiteBishops |= squareBit;
                    case ROOK -> whiteRooks |= squareBit;
                    case QUEEN -> whiteQueens |= squareBit;
                    case KING -> whiteKing |= squareBit;
                }
            } else {
                switch (piece) {
                    case PAWN -> blackPawns |= squareBit;
                    case KNIGHT -> blackKnights |= squareBit;
                    case BISHOP -> blackBishops |= squareBit;
                    case ROOK -> blackRooks |= squareBit;
                    case QUEEN -> blackQueens |= squareBit;
                    case KING -> blackKing |= squareBit;
                }
            }
        }
        
        private int kingSquare(boolean white) {
            final long kings = white ? whiteKing : blackKing;
            final long pieces = getPieces(white);
            return Bits.next(kings & pieces);
        }
        
        private long getPieces(boolean white) {
            return white ? whitePieces : blackPieces;
        }

        private void fill(Board board) {
            board.setPawns(whitePawns | blackPawns);
            board.setKnights(whiteKnights | blackKnights);
            board.setBishops(whiteBishops | blackBishops);
            board.setRooks(whiteRooks | blackRooks);
            board.setQueens(whiteQueens | blackQueens);
            board.setKings(whiteKing | blackKing);
            board.setWhitePieces(whitePieces);
            board.setBlackPieces(blackPieces);
        }
    }

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private final ChessVariant variant;
    // The added pieces
    private Piece[] pieceList = new Piece[Square.COUNT];
    // A bitmap of white added pieces. If a piece is in pieceList and its bit is 1, it is white
    private long whitePieces = 0L;
    private int enPassantFile = -1;
    private int fiftyMoveCounter = 0;
    private CastlingRights castlingRights = new CastlingRights();
    private boolean whiteToMove = true;
    
    /** Creates a new standard board initialized with the standard starting position.
     * @return the board
     */
    public static Board newStandard() {
        BoardBuilder builder = new BoardBuilder(ChessVariant.STANDARD);
        StartPositionBuilder.fillFromPositionNumber(builder, 518);
        return builder.build();
    }
    
    /** Creates a new <a href="https://en.wikipedia.org/wiki/Chess960">Fischer random board</a> initialized with a random starting position.
     * @return the board
     */
    public static Board newFischerRandom() {
        return newFischerRandom(RANDOM.nextInt(960));
    }

   /** Creates a new <a href="https://en.wikipedia.org/wiki/Chess960">Fischer random board</a> initialized with a specified starting position.
     * @param positionId the position id in <a href="https://en.wikipedia.org/wiki/Fischer_random_chess_numbering_scheme">Fischer random chess numbering scheme</a>
     * @return the board
     */
    public static Board newFischerRandom(int positionId) {
        BoardBuilder builder = new BoardBuilder(ChessVariant.CHESS960);
        StartPositionBuilder.fillFromPositionNumber(builder, positionId);
        return builder.build();
    }

    /** Creates a new board builder.
     * @param variant the variant of the board
     */
    public BoardBuilder(ChessVariant variant) {
        if (variant==null) {
            throw new IllegalArgumentException();
        }
        this.variant = variant;
    }

    /** Adds, removes or replace a piece to the board at the given square.
     * <br>If there's already a piece at the provided square, it is replaced.
     * <br>By default, the built board contains no pieces.
     * @param square the square to add the piece to
     * @param piece the piece to add, or null to remove the piece
     * @param white if the piece is white (ignored if the piece is null)
     */
    public void addPiece(int square, Piece piece, boolean white) {
        final long squareBit = Bits.of(square);
        if (pieceList[square] != null) {
            // piece replacement -> remove the piece from the white pieces list
            whitePieces = whitePieces ^ squareBit;
        }
        pieceList[square] = piece;
        if (white && piece != null) {
            whitePieces = whitePieces | squareBit;
        }
    }

    /** Sets the white to move flag.
     * <br>Defaults to true
     * @param whiteToMove the white to move flag
     */
    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }

    /** Adds a standard castling right.
     * <br>A standard castling right is the right to castle with the outermost rook. This is the only castling allowed with standard chess.
     * <br>Defaults to no castling allowed.
     * @param white the color to add the castling rights to
     * @param kingSide true for king side castling, false for queen side castling.
     * @see #addCastlingRights(boolean, boolean, int)
     */
    public void addCastlingRights(boolean white, boolean kingSide) {
        this.castlingRights.add(white, kingSide, CastlingRights.OUTER_MOST_ROOK);
    }

    /** Adds a castling right with a specified rook file.
     * <br>This castling is essential to describe castling with an inner rook in <a href="https://en.wikipedia.org/wiki/X-FEN#Encoding_castling_rights">Chess960</a>.
     * <br>Defaults to no castling allowed.
     * @param white the color to add the castling rights to
     * @param kingSide true for king side castling, false for queen side castling.
     * @param file the rook's file.
     * @see #addCastlingRights(boolean, boolean)
     * @see File#of(int)
     */
    public void addCastlingRights(boolean white, boolean kingSide, int file) {
        this.castlingRights.add(white, kingSide, file);
    }

    /** Sets the en passant file.
     * <br>Defaults to -1
     * @param enPassantFile the en passant file, or -1 if no en passant is possible
     */
    public void setEnPassantFile(int enPassantFile) {
        this.enPassantFile = enPassantFile;
    }

    /** Sets the fifty move counter.
     * <br>Defaults to 0
     * @param fiftyMoveCounter the fifty move counter
     */
    public void setFiftyMoveCounter(int fiftyMoveCounter) {
        this.fiftyMoveCounter = fiftyMoveCounter;
    }

    /** Builds a board based on the current state of the builder.
     * @return the built board
     * @throws IllegalArgumentException if current state is inconsistent, for example if a color has castle rights but no rook.
     */
    public Board build() {
        final BitMaps bitmaps = new BitMaps(pieceList, whitePieces);
        checkOneKing(bitmaps.whiteKing, true);
        checkOneKing(bitmaps.blackKing, false);
        checkEnPassant(bitmaps, enPassantFile);
        final int rights = toRights(bitmaps);
        
        final Board board = new Board();
        board.setVariant(variant);
        board.setPieces(pieceList);
        bitmaps.fill(board);
        board.setWhite(whiteToMove);
        board.getState().setEnPassantFile(enPassantFile);
        board.getState().setRights(rights);
        board.getState().setHalfMoveClock(fiftyMoveCounter);

        board.getState().setKey(Key.generateKey(board));
        board.getState().setPawnKey(Key.generatePawnKey(board));
        board.getState().setNonPawnKeys(Key.generateNonPawnKeys(board));
        return board;
    }
    
    private void checkOneKing(long kings, boolean white) {
        final int kingCount = Bits.count(kings);
        if (kingCount != 1) {
            throw new IllegalArgumentException(String.format("Expected one %s king, found %d.", Colour.label(white), kingCount));
        }
    }

    private void checkEnPassant(BitMaps board, int enPassantFile) {
        if (enPassantFile == -1) return;
        // Check that en passant square corresponds to a pawn of the color that is NOT to move
        final int rank = whiteToMove ? 4 : 3;
        final int pawnSquare = Square.of(rank, enPassantFile);
        final long colorPieces = whiteToMove ? board.blackPieces : board.whitePieces;
        if (pieceList[pawnSquare] != PAWN || (Bits.of(pawnSquare) & colorPieces) == 0) {
            throw new IllegalArgumentException(String.format("Illegal 'en passant' square. There's no %s pawn at %s!", Colour.label(!whiteToMove), Square.toNotation(pawnSquare)));
        }
    }
 
    private int toRights(BitMaps board) {
        int rights = Castling.empty();
        if (castlingRights.isEmpty()) return rights;
        rights = updateCastlingRight(board, rights, true, true);
        rights = updateCastlingRight(board, rights, false, true);
        rights = updateCastlingRight(board, rights, true, false);
        rights = updateCastlingRight(board, rights, false, false);
        if (variant==ChessVariant.CHESS960) {
            // Check that files of kings are consistent if both colors can castle
            if (castlingRights.colorHasCastling(true) && castlingRights.colorHasCastling(false) && File.of(board.kingSquare(true)) != File.of(board.kingSquare(false))) {
                throw new IllegalArgumentException("Illegal castling rights, both kings are not on the same file");
            }
            // Check that files of rooks are consistent
            if (castlingRights.bothColorsHasSideCastling(true) && File.of(Castling.getRook(rights, true, true)) != File.of(Castling.getRook(rights, true, false))) {
                throw new IllegalArgumentException("Illegal castling rights, king side rooks are not on the same file");
            }
            if (castlingRights.bothColorsHasSideCastling(false) && File.of(Castling.getRook(rights, false, true)) != File.of(Castling.getRook(rights, false, false))) {
                throw new IllegalArgumentException("Illegal castling rights, queen side rooks are not on the same file");
            }
        }
        return rights;
    }

    private int updateCastlingRight(BitMaps bitmaps, int rights, boolean kingside, boolean white) {
        final int right = castlingRights.get(white, kingside);
        if (right == CastlingRights.NO_RIGHT) {
            return rights;
        } else if (right == CastlingRights.OUTER_MOST_ROOK) {
            rights = Castling.setRook(rights, kingside, white, findRook(bitmaps, kingside, white));
        } else {
            final int rootSquare = Square.of(white ? 0:7, right);
            rights = Castling.setRook(rights, kingside, white, rootSquare);
        }
        // Check if rook is at its starting position and if king is too
        final long pieces = bitmaps.getPieces(white);
        final int rookSquare;
        if (variant==ChessVariant.CHESS960) {
            rookSquare = Castling.getRook(rights, kingside, white);
            // Check if king is at its starting position. In Chess960, the king can be at almost any position in its side of the board.
            // The only constraint is there should be a rook at its side.
            final int kingExpectedRank = white ? 0 : 7;
            final int kingSquare = bitmaps.kingSquare(white);
            if (Rank.of(kingSquare) != kingExpectedRank) {
                throw new IllegalArgumentException(String.format("Illegal castling rights for %s, king is not at its starting rank", Colour.label(white)));
            }
            // Check if king has moved (it has moved if it is at first or last file, or if it is not at the right side of the rook involved in the castling
            final int kingFile = File.of(kingSquare);
            final boolean effectiveKingSide = kingSquare<rookSquare;
            if (kingFile==0 || kingFile==7 || kingside != effectiveKingSide) {
                throw new IllegalArgumentException(String.format("Illegal castling rights for %s, king is not at its starting file", Colour.label(white)));
            }
        } else {
            // Check if king is at its starting position.
            final int kingSquare = white ? 4 : 60;
            if (pieceList[kingSquare] != KING || (pieces&Bits.of(kingSquare)) == 0) {
                throw new IllegalArgumentException(String.format("Illegal castling rights for %s, king is not at %s",Colour.label(white),Square.toNotation(kingSquare)));
            }
            rookSquare = Castling.rookFrom(kingside, white);
        }
        if (pieceList[rookSquare] != ROOK || (pieces&Bits.of(rookSquare)) == 0) {
            throw new IllegalArgumentException(String.format("Illegal castling rights for %s, there's no rook at %s",Colour.label(white),Square.toNotation(rookSquare)));
        }
        return rights;
    }

    private int findRook(BitMaps bitMaps, boolean kingside, boolean white) {
        final long firstRankRooks = getFirstRankRooks(bitMaps, white);
        final List<Integer> squares = Arrays.stream(Bits.collect(firstRankRooks))
                .boxed()
                .sorted(Comparator.comparing(File::of))
                .toList();
        return kingside ? squares.get(squares.size() - 1) : squares.get(0);
    }

    private long getFirstRankRooks(BitMaps bitMaps, boolean white) {
        final long firstRank = white ? Rank.FIRST : Rank.EIGHTH;
        final long firstRankRooks = (white ? bitMaps.whiteRooks : bitMaps.blackRooks) & firstRank;
        final long firstRankKings = (white ? bitMaps.whiteKing : bitMaps.blackKing) & firstRank;
        if (Bits.count(firstRankRooks) == 0) {
            throw new IllegalArgumentException(String.format("Illegal castling rights, there are no %s rooks on the first rank!", Colour.label(white)));
        }
        if (Bits.count(firstRankKings) == 0) {
            throw new IllegalArgumentException(String.format("Illegal castling rights, there are no %s kings on the first rank!", Colour.label(white)));
        }
        return firstRankRooks;
    }
}
