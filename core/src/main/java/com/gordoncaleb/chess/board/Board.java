package com.gordoncaleb.chess.board;

import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.pieces.*;

import static com.gordoncaleb.chess.board.Side.*;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;

public class Board {

    private static final RNGTable rngTable = RNGTable.instance;

    public static final int FOE_ATTACKS = 0;
    public static final int CHECK_VECTORS = 1;
    public static final int CHECK_COMPLIMENT = 2;
    public static final int KING_WEST = 3;
    public static final int KING_EAST = 4;

    public static final int NEAR = 0;
    public static final int FAR = 1;
    private static final int[] MATERIAL_ROW = new int[2];

    static {
        MATERIAL_ROW[BLACK] = 0;
        MATERIAL_ROW[WHITE] = 7;
    }

    private long castleRights;
    private final long[][] rooksInitBitboards;
    private final long[] kingsInitBitBoards;
    public final long[][] kingToCastleMasks;
    public final long[][] rookToCastleMasks;

    private Deque<Long> castleRightsHistory = new ArrayDeque<>();

    private final Piece[][] piecesArray;
    private MoveContainer validMoves = new MoveContainer();
    private LinkedList<Piece>[] piecesList = new LinkedList[2];
    private Deque<Piece>[] piecesTaken = new ArrayDeque[2];

    private final Piece[] kings;

    private final int[][] rookStartCols;
    private final int[] kingStartCols;

    private int turn;
    private long hashCode;
    private int hashCodeFreq;
    private final MoveContainer moveHistory = new MoveContainer(1000);
    private final Deque<Long> hashCodeHistory = new ArrayDeque<>();
    private final Map<Long, Integer> hashCodeFrequencies = new HashMap<>();

    private final long[] nullMoveInfo = new long[5];

    private final long[][] posBitBoard;
    private final long[] allPosBitBoard;

    //Inputs are copied safely
    public Board(List<Piece> whitePieceList,
                 List<Piece> blackPieceList,
                 int turn,
                 boolean whiteNear,
                 boolean whiteFar,
                 boolean blackNear,
                 boolean blackFar) {

        this.piecesTaken[WHITE] = new ArrayDeque<>();
        this.piecesTaken[BLACK] = new ArrayDeque<>();

        this.turn = turn;

        this.piecesList[BLACK] = blackPieceList.stream()
                .map(Piece::copy)
                .collect(Collectors.toCollection(LinkedList::new));

        this.piecesList[WHITE] = whitePieceList.stream()
                .map(Piece::copy)
                .collect(Collectors.toCollection(LinkedList::new));

        kings = findKings(this.piecesList);

        piecesArray = buildPieceBoard(this.piecesList);
        posBitBoard = buildPieceBitBoards(this.piecesList);
        allPosBitBoard = buildSideBitBoards(posBitBoard);

        rookStartCols = findRookStartCols(piecesArray, kings);
        kingStartCols = findKingStartCols(kings);

        kingsInitBitBoards = buildKingsInitBitboards(posBitBoard);
        rooksInitBitboards = buildRookInitBitBoards(piecesArray);
        kingToCastleMasks = buildKingToCastleMasks(kingsInitBitBoards, rooksInitBitboards, MATERIAL_ROW);
        rookToCastleMasks = buildRookToCastleMasks(kingsInitBitBoards, rooksInitBitboards, MATERIAL_ROW);

        castleRights = orKingsAndRooks(kingsInitBitBoards, rooksInitBitboards);
        applyCastleRights(Side.WHITE, whiteNear, whiteFar);
        applyCastleRights(Side.BLACK, blackNear, blackFar);
        castleRightsHistory.push(castleRights);

        initializeHashCode();
    }

    private void initializeHashCode() {
        hashCode = generateHashCode();
        hashCodeFreq = incrementHashCodeFrequency(hashCode);
    }

    private static long orKingsAndRooks(long[] kingsInitBitBoards, long[][] rooksInitBitboards) {
        return kingsInitBitBoards[WHITE] |
                kingsInitBitBoards[BLACK] |
                rooksInitBitboards[WHITE][NEAR] |
                rooksInitBitboards[WHITE][FAR] |
                rooksInitBitboards[BLACK][NEAR] |
                rooksInitBitboards[BLACK][FAR];
    }

    private Piece[][] buildPieceBoard(List<Piece>[] pieces) {
        Piece[][] b = new Piece[8][8];

        for (int r = 0; r < b.length; r++) {
            for (int c = 0; c < b[r].length; c++) {
                b[r][c] = Piece.EMPTY;
            }
        }

        Stream.of(pieces)
                .flatMap(List::stream)
                .forEach(p ->
                        b[p.getRow()][p.getCol()] = p
                );

        return b;
    }

    private long[][] buildPieceBitBoards(List<Piece>[] pieces) {
        long[][] posBitBoard = new long[PIECES_COUNT][2];
        Stream.of(pieces)
                .flatMap(List::stream)
                .forEach(p ->
                        posBitBoard[p.getPieceID()][p.getSide()] |= p.asBitMask()
                );
        return posBitBoard;
    }

    private long[] buildSideBitBoards(long[][] pieceBitBoards) {
        long[] allPosBitBoard = new long[2];

        Stream.of(pieceBitBoards).forEach(bb -> {
            allPosBitBoard[0] |= bb[0];
            allPosBitBoard[1] |= bb[1];
        });

        return allPosBitBoard;
    }

    private Piece[] findKings(List<Piece>[] pieces) {
        Piece[] kings = new Piece[2];
        Stream.of(pieces).forEach(sidePieces ->
                sidePieces.stream()
                        .filter(p -> p.getPieceID() == KING)
                        .findFirst()
                        .ifPresent(p -> kings[p.getSide()] = p)
        );
        return kings;
    }

    private long[] buildKingsInitBitboards(long[][] posBitBoards) {
        long[] kingInitBitboards = new long[2];
        kingInitBitboards[WHITE] = posBitBoards[KING][WHITE];
        kingInitBitboards[BLACK] = posBitBoards[KING][BLACK];
        return kingInitBitboards;
    }

    public boolean makeMove(final Move move) {

        final int fromRow = move.getFromRow();
        final int fromCol = move.getFromCol();
        final int toRow = move.getToRow();
        final int toCol = move.getToCol();
        final int note = move.getNote();

        final int nextSide = Side.otherSide(turn);

        // save off hash code
        hashCodeHistory.push(hashCode);
        // save off castle rights
        castleRightsHistory.push(castleRights);

        // remove previous castle options
        hashCode ^= rngTable.getCastlingRightsRandom(turn,
                canCastleNear(turn) ? RNGTable.YES : RNGTable.NO,
                canCastleFar(turn) ? RNGTable.YES : RNGTable.NO
        );

        // remove taken piece first
        if (move.hasPieceTaken()) {

            final int pieceTakenRow = move.getPieceTakenRow();
            final int pieceTakenCol = move.getPieceTakenCol();
            final int pieceTakenId = move.getPieceTakenId();
            final long pieceTakenMask = getMask(pieceTakenRow, pieceTakenCol);

            // get ref of pieceTaken
            final Piece pieceTaken = piecesArray[pieceTakenRow][pieceTakenCol];

            // remove ref to piecetaken on piecesArray
            piecesArray[pieceTakenRow][pieceTakenCol] = Piece.EMPTY;

            // remove pieceTaken from vectors
            piecesList[nextSide].remove(pieceTaken);
            piecesTaken[nextSide].addFirst(pieceTaken);

            posBitBoard[pieceTakenId][nextSide] ^= pieceTakenMask;
            allPosBitBoard[nextSide] ^= pieceTakenMask;

            // remove old hash from piece that was taken, if any
            hashCode ^= rngTable.getPiecePerSquareRandom(nextSide, pieceTakenId, pieceTakenRow, pieceTakenCol);
        }

        if ((note & Move.MoveNote.CASTLE) != 0) {

            final Piece king = kings[turn];
            final Piece rook;

            if (note == Move.MoveNote.CASTLE_NEAR) {
                rook = piecesArray[MATERIAL_ROW[turn]][rookStartCols[turn][1]];

                movePiece(king, MATERIAL_ROW[turn], 6, Move.MoveNote.NORMAL);
                movePiece(rook, MATERIAL_ROW[turn], 5, Move.MoveNote.NORMAL);

                piecesArray[MATERIAL_ROW[turn]][6] = king;
                piecesArray[MATERIAL_ROW[turn]][5] = rook;

            } else {
                rook = piecesArray[MATERIAL_ROW[turn]][rookStartCols[turn][0]];

                movePiece(king, MATERIAL_ROW[turn], 2, Move.MoveNote.NORMAL);
                movePiece(rook, MATERIAL_ROW[turn], 3, Move.MoveNote.NORMAL);

                piecesArray[MATERIAL_ROW[turn]][2] = king;
                piecesArray[MATERIAL_ROW[turn]][3] = rook;

            }

        } else {
            movePiece(piecesArray[fromRow][fromCol], toRow, toCol, note);
        }

        // if last move made is pawn leap, remove en passant file num
        if (getLastMoveMade().getNote() == Move.MoveNote.PAWN_LEAP) {
            hashCode ^= rngTable.getEnPassantFile(getLastMoveMade().getToCol());
        }

        // if new move is pawn leap, add en passant file num
        if (note == Move.MoveNote.PAWN_LEAP) {
            hashCode ^= rngTable.getEnPassantFile(move.getToCol());
        }

        // add new castle options
        hashCode ^= rngTable.getCastlingRightsRandom(nextSide,
                canCastleNear(nextSide) ? RNGTable.YES : RNGTable.NO,
                canCastleFar(nextSide) ? RNGTable.YES : RNGTable.NO
        );

        // either remove black and add white or reverse. Same operation.
        hashCode ^= rngTable.getBlackToMoveRandom();

        hashCodeFreq = incrementHashCodeFrequency(hashCode);

        // show that this move is now the last move made
        moveHistory.add(move);

        // move was made, next player's turn
        turn = nextSide;

        return true;
    }

    private void movePiece(final Piece pieceMoving, final int toRow, final int toCol, final int note) {

        final long bitMove = getMask(pieceMoving.getRow(), pieceMoving.getCol()) | getMask(toRow, toCol);

        castleRights &= ~bitMove;

        // remove bit position from where piece was and add where it is now
        posBitBoard[pieceMoving.getPieceID()][pieceMoving.getSide()] ^= bitMove;
        allPosBitBoard[pieceMoving.getSide()] ^= bitMove;

        // remove old hash from where piece was
        hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), pieceMoving.getRow(), pieceMoving.getCol());

        // remove piecesList old position
        piecesArray[pieceMoving.getRow()][pieceMoving.getCol()] = Piece.EMPTY;
        // update piecesArray to reflect piece's new position
        piecesArray[toRow][toCol] = pieceMoving;

        // tell piece its new position
        pieceMoving.move(toRow, toCol);

        if ((note & Move.MoveNote.NEW_QUEEN) != 0) {
            final int queenChoice = note >> 8;
            pieceMoving.setPieceID(queenChoice);
            posBitBoard[PAWN][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
            posBitBoard[queenChoice][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
        }

        // add hash of piece at new location
        hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), toRow, toCol);
    }

    public Move undoMove() {

        // retrieve last move made
        final Move lastMove = getLastMoveMade();

        final int fromRow = lastMove.getFromRow();
        final int fromCol = lastMove.getFromCol();
        final int toRow = lastMove.getToRow();
        final int toCol = lastMove.getToCol();
        final int note = lastMove.getNote();

        // last move made was made by previous player, which is also the next
        // player
        final int prevSide = turn;
        turn = Side.otherSide(turn);

        if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

            final Piece king = kings[turn];
            final Piece rook;

            if (note == Move.MoveNote.CASTLE_FAR) {
                rook = piecesArray[MATERIAL_ROW[turn]][3];

                undoMovePiece(king, MATERIAL_ROW[turn], kingStartCols[turn], Move.MoveNote.NORMAL);
                undoMovePiece(rook, MATERIAL_ROW[turn], rookStartCols[turn][0], Move.MoveNote.NORMAL);

                piecesArray[MATERIAL_ROW[turn]][kingStartCols[turn]] = king;
                piecesArray[MATERIAL_ROW[turn]][rookStartCols[turn][0]] = rook;

            } else {

                rook = piecesArray[MATERIAL_ROW[turn]][5];

                undoMovePiece(king, MATERIAL_ROW[turn], kingStartCols[turn], Move.MoveNote.NORMAL);
                undoMovePiece(rook, MATERIAL_ROW[turn], rookStartCols[turn][1], Move.MoveNote.NORMAL);

                piecesArray[MATERIAL_ROW[turn]][kingStartCols[turn]] = king;
                piecesArray[MATERIAL_ROW[turn]][rookStartCols[turn][1]] = rook;
            }

        } else {
            undoMovePiece(piecesArray[toRow][toCol], fromRow, fromCol, note);
        }

        if (lastMove.hasPieceTaken()) {
            // add taken piece back to vectors and piecesArray
            final Piece pieceTaken = piecesTaken[prevSide].removeFirst();

            piecesList[prevSide].add(pieceTaken);

            // add piece taken to position bit piecesArray
            posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] |= pieceTaken.asBitMask();
            allPosBitBoard[pieceTaken.getSide()] |= pieceTaken.asBitMask();

            piecesArray[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;
        }

        //decrement old hashcode frequency
        hashCodeFreq = decrementHashCodeFrequency(hashCode);

        // move was undone so show move made before that as the last move made
        moveHistory.pop();

        castleRights = castleRightsHistory.pop();

        if (hashCodeHistory.isEmpty()) {
            // if no hashCode was saved then generate it the hard way
            hashCode = generateHashCode();
        } else {
            // retrieve what the hashCode was before move was made
            hashCode = hashCodeHistory.pop();
        }

        return lastMove;
    }

    private void undoMovePiece(final Piece pieceMoving, final int fromRow, final int fromCol, final int note) {

        final long bitMove = getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ getMask(fromRow, fromCol);

        // remove bit position from where piece was and add where it is now
        posBitBoard[pieceMoving.getPieceID()][pieceMoving.getSide()] ^= bitMove;
        allPosBitBoard[pieceMoving.getSide()] ^= bitMove;

        // remove old position
        piecesArray[pieceMoving.getRow()][pieceMoving.getCol()] = Piece.EMPTY;
        // put piece in old position
        piecesArray[fromRow][fromCol] = pieceMoving;

        // tell piece where it was
        pieceMoving.unmove(fromRow, fromCol);

        if ((note & Move.MoveNote.NEW_QUEEN) != 0) {
            final int queenChoice = note >> 8;
            pieceMoving.setPieceID(PAWN);
            posBitBoard[PAWN][pieceMoving.getSide()] |= pieceMoving.asBitMask();
            posBitBoard[queenChoice][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
        }

    }

    public boolean canCastleNear(int side) {
        return ((castleRights & kingsInitBitBoards[side]) != 0 &&
                (castleRights & rooksInitBitboards[side][NEAR]) != 0);
    }

    public boolean canCastleFar(int side) {
        return ((castleRights & kingsInitBitBoards[side]) != 0 &&
                (castleRights & rooksInitBitboards[side][FAR]) != 0);
    }

    public boolean canUndo() {
        return (!moveHistory.isEmpty());
    }

    public void undoAll() {
        while (canUndo()) {
            undoMove();
        }
    }

    public void undo(final int num) {
        int i = num;
        while (canUndo() && i > 0) {
            undoMove();
            i--;
        }
    }

    public MoveContainer generateValidMoves() {
        return generateValidMoves(this.validMoves);
    }

    public MoveContainer generateValidMoves(MoveContainer validMoves) {

        validMoves.clear();

        for (Piece p : piecesList[turn]) {
            p.generateValidMoves(this, nullMoveInfo, allPosBitBoard, validMoves);
        }

        return validMoves;
    }

    public long[] getAllPosBitBoard() {
        return allPosBitBoard;
    }

    public long[][] getPosBitBoard() {
        return posBitBoard;
    }

    public long[] makeNullMove() {

        // recalculating check info
        piecesList[turn].forEach(Piece::clearBlocking);

        final int otherSide = Side.otherSide(turn);
        final long friendOrFoe = allPosBitBoard[WHITE] | allPosBitBoard[BLACK];
        nullMoveInfo[FOE_ATTACKS] = Pawn.getPawnAttacks(posBitBoard[PAWN][otherSide], otherSide);
        nullMoveInfo[FOE_ATTACKS] |= Knight.getKnightAttacks(posBitBoard[KNIGHT][otherSide]);
        nullMoveInfo[FOE_ATTACKS] |= King.getKingAttacks(posBitBoard[KING][otherSide]);

        Queen.getQueenAttacks(posBitBoard[QUEEN][otherSide], friendOrFoe, nullMoveInfo);
        Rook.getRookAttacks(posBitBoard[ROOK][otherSide], friendOrFoe, nullMoveInfo);
        Bishop.getBishopAttacks(posBitBoard[BISHOP][otherSide], friendOrFoe, nullMoveInfo);

        final long jumperAttacks = Pawn.getPawnAttacks(posBitBoard[KING][turn], turn) & posBitBoard[PAWN][otherSide] |
                Knight.getKnightAttacks(posBitBoard[KING][turn]) & posBitBoard[KNIGHT][otherSide];

        nullMoveInfo[CHECK_VECTORS] = jumperAttacks == 0L ?
                ~posBitBoard[KING][turn] :
                (hasOneBitOrLess(jumperAttacks) ? jumperAttacks : 0L);

        nullMoveInfo[CHECK_COMPLIMENT] = 0L;

        King.getKingCheckInfo(this,
                posBitBoard[KING][turn],
                posBitBoard[QUEEN][otherSide],
                posBitBoard[ROOK][otherSide],
                posBitBoard[BISHOP][otherSide],
                friendOrFoe,
                nullMoveInfo);

        return nullMoveInfo;
    }

    public long[] getNullMoveInfo() {
        return nullMoveInfo;
    }

    public Move getLastMoveMade() {
        return moveHistory.isEmpty() ? Move.EMPTY_MOVE : moveHistory.peek();
    }

    public List<Move> getMoveHistory() {
        return moveHistory.toList();
    }

    public int getMoveNumber(){
        return moveHistory.size();
    }

    public List<Piece>[] getPiecesList() {
        return this.piecesList;
    }

    public Piece getPiece(int row, int col) {
        return piecesArray[row][col];
    }

    public int getPieceID(int row, int col) {
        return piecesArray[row][col].getPieceID();
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public Deque<Piece> getPiecesTakenFor(int player) {
        return piecesTaken[player];
    }

    private long[][] buildRookInitBitBoards(final Piece[][] board) {

        long[][] rooksInitBitboards = new long[2][2];

        for (int s : Arrays.asList(BLACK, WHITE)) {
            if (kings[s] != null) {
                for (int c = kings[s].getCol() - 1; c >= 0; c--) {

                    if (board[MATERIAL_ROW[s]][c] != null) {
                        Piece p = board[MATERIAL_ROW[s]][c];
                        if (p.getPieceID() == ROOK) {
                            rooksInitBitboards[p.getSide()][FAR] = p.asBitMask();
                            break;
                        }
                    }
                }

                for (int c = kings[s].getCol() + 1; c < 8; c++) {
                    if (board[MATERIAL_ROW[s]][c] != null) {
                        Piece p = board[MATERIAL_ROW[s]][c];
                        if (p.getPieceID() == ROOK) {
                            rooksInitBitboards[p.getSide()][NEAR] = p.asBitMask();
                            break;
                        }
                    }
                }
            }
        }

        return rooksInitBitboards;
    }

    private int[][] findRookStartCols(Piece[][] board, Piece[] kings) {

        int[][] rookCols = {{-1, -1}, {-1, -1}};

        for (int s : Arrays.asList(BLACK, WHITE)) {
            if (kings[s] != null) {
                for (int c = kings[s].getCol() - 1; c >= 0; c--) {

                    if (board[MATERIAL_ROW[s]][c] != null) {
                        Piece p = board[MATERIAL_ROW[s]][c];
                        if (p.getPieceID() == ROOK) {
                            rookCols[s][0] = c;
                            break;
                        }
                    }
                }

                for (int c = kings[s].getCol() + 1; c < 8; c++) {
                    if (board[MATERIAL_ROW[s]][c] != null) {
                        Piece p = board[MATERIAL_ROW[s]][c];
                        if (p.getPieceID() == ROOK) {
                            rookCols[s][1] = c;
                            break;
                        }
                    }
                }
            }
        }

        return rookCols;
    }

    private int[] findKingStartCols(Piece[] kings) {
        int[] kingStartCols = new int[]{-1, -1};
        if (kings[Side.BLACK] != null) {
            kingStartCols[Side.BLACK] = kings[Side.BLACK].getCol();
        }
        if (kings[WHITE] != null) {
            kingStartCols[Side.WHITE] = kings[Side.WHITE].getCol();
        }
        return kingStartCols;
    }

    public boolean placePiece(Piece piece, int toRow, int toCol) {

        if (toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
            if (piecesArray[toRow][toCol] != null) {
                if (piecesArray[toRow][toCol].getPieceID() == KING) {
                    return false;
                }
            }
        }

        if (piece.getPieceID() == KING) {
            if (toRow < 0 || toCol < 0) {
                return false;
            } else {
                kings[piece.getSide()] = piece;
            }
        }

        if (piece.getRow() >= 0) {
            // remove where piece was if it was on piecesArray
            posBitBoard[piece.getPieceID()][piece.getSide()] ^= piece.asBitMask();
            allPosBitBoard[piece.getSide()] ^= piece.asBitMask();
            piecesArray[piece.getRow()][piece.getCol()] = null;

        } else {

            piecesList[piece.getSide()].add(piece);
        }

        if (toRow >= 0) {
            // remove where piece taken was
            if (piecesArray[toRow][toCol] != null) {

                Piece pieceTaken = piecesArray[toRow][toCol];

                // remove bit position of piece taken
                posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] ^= pieceTaken.asBitMask();
                allPosBitBoard[pieceTaken.getSide()] ^= pieceTaken.asBitMask();

                // remove ref to piece taken
                piecesList[pieceTaken.getSide()].remove(pieceTaken);
            }

            // tell piece where it is now
            piece.setRow(toRow);
            piece.setCol(toCol);

            // reflect new piece in position bitboard
            posBitBoard[piece.getPieceID()][piece.getSide()] |= piece.asBitMask();
            allPosBitBoard[piece.getSide()] |= piece.asBitMask();

            // update piecesArray ref to show piece there
            piecesArray[toRow][toCol] = piece;
        } else {
            // piece is being taken off the piecesArray. Remove
            if (piece.getPieceID() != KING) {
                piecesList[piece.getSide()].remove(piece);
            }
        }

        // basically start over with new piecesArray
        this.moveHistory.clear();
        this.hashCodeHistory.clear();

        this.hashCode = generateHashCode();

        return true;
    }

    private void applyCastleRights(int player, boolean nearRights, boolean farRights) {

        if (nearRights) {
            castleRights |= rooksInitBitboards[player][NEAR];
        } else {
            castleRights &= ~rooksInitBitboards[player][NEAR];
        }

        if (farRights) {
            castleRights |= rooksInitBitboards[player][FAR];
        } else {
            castleRights &= ~rooksInitBitboards[player][FAR];
        }

        if (!nearRights && !farRights) {
            castleRights &= ~kingsInitBitBoards[player];
        }
    }

    public Board copy() {
        List<Move> moveHistory = new ArrayList<>(this.moveHistory.toList());

        while (canUndo()) {
            undoMove();
        }

        Board boardCopy = new Board(piecesList[WHITE], piecesList[BLACK],
                turn,
                canCastleNear(WHITE),
                canCastleFar(WHITE),
                canCastleNear(BLACK),
                canCastleFar(BLACK));

        moveHistory.stream()
                .forEach(m -> {
                    this.makeMove(m);
                    boardCopy.makeMove(m);
                });

        return boardCopy;
    }

    public String toString() {
        String stringBoard = "";

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (piecesArray[row][col].getPieceID() != Piece.PieceID.NO_PIECE) {
                    stringBoard += piecesArray[row][col].toString() + ",";
                } else {
                    stringBoard += "_,";
                }

            }
            stringBoard += "\n";
        }

        return stringBoard;
    }

    public String toJson(boolean includeHistory) throws JsonProcessingException {
        return JSONParser.toJSON(this, includeHistory);
    }

    public long generateHashCode() {
        long hashCode = 0;

        if (turn == Side.BLACK) {
            hashCode = rngTable.getBlackToMoveRandom();
        }

        Piece p;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                p = piecesArray[r][c];
                if (p.getPieceID() != Piece.PieceID.NO_PIECE) {
                    hashCode ^= rngTable.getPiecePerSquareRandom(p.getSide(), p.getPieceID(), r, c);
                }
            }
        }

        // add new castle options
        hashCode ^= rngTable.getCastlingRightsRandom(turn,
                canCastleNear(turn) ? RNGTable.YES : RNGTable.NO,
                canCastleFar(turn) ? RNGTable.YES : RNGTable.NO
        );

        if (getLastMoveMade().getNote() == Move.MoveNote.PAWN_LEAP) {
            hashCode ^= rngTable.getEnPassantFile(getLastMoveMade().getToCol());
        }

        return hashCode;
    }

    public long getHashCode() {
        return hashCode;
    }

    private int incrementHashCodeFrequency(long hashCode) {
        return Optional.ofNullable(
                hashCodeFrequencies.put(hashCode, Optional.ofNullable(
                        hashCodeFrequencies.get(hashCode))
                        .orElse(0) + 1))
                .orElse(0) + 1;
    }

    private int decrementHashCodeFrequency(long hashCode) {
        return Optional.ofNullable(hashCodeFrequencies.get(hashCode))
                .map(i -> {
                    i--;
                    if (i == 0) {
                        hashCodeFrequencies.remove(hashCode);
                    } else {
                        hashCodeFrequencies.put(hashCode, i);
                    }
                    return i;
                }).orElse(0);
    }

    public int getHashCodeFreq() {
        return this.hashCodeFreq;
    }

    public boolean drawByThreeRule() {
        return hashCodeFreq >= 3;
    }

    public boolean insufficientMaterial() {

        long pawnsQueensAndRooks = posBitBoard[PAWN][WHITE] |
                posBitBoard[QUEEN][WHITE] |
                posBitBoard[ROOK][WHITE] |
                posBitBoard[PAWN][BLACK] |
                posBitBoard[QUEEN][BLACK] |
                posBitBoard[ROOK][BLACK];

        return pawnsQueensAndRooks == 0;
    }

    public boolean isInCheck() {
        return ((posBitBoard[KING][turn] & nullMoveInfo[FOE_ATTACKS]) != 0);
    }

    public boolean isDraw() {
        return (drawByThreeRule() || insufficientMaterial());
    }

    public boolean isGameOver() {
        return isDraw() || isCheckMate();
    }

    public boolean isCheckMate() {
        return (isInCheck() && generateValidMoves().isEmpty());
    }

    public Board startingPosition() {
        Board newBoard = copy();
        newBoard.undoAll();
        return newBoard;
    }

}
