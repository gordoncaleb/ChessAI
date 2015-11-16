package com.gordoncaleb.chess.board;

import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.engine.AI;
import com.gordoncaleb.chess.ui.gui.game.Game;
import com.gordoncaleb.chess.board.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.board.Side.*;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;

public class Board {
    private static final Logger logger = LoggerFactory.getLogger(Board.class);

    private final RNGTable rngTable = RNGTable.instance;

    public static final int NEAR = 0;
    public static final int FAR = 1;
    public static final int[] MATERIAL_ROW = new int[2];
    public static final int[] PAWN_ROW = new int[2];

    static {
        PAWN_ROW[BLACK] = 1;
        PAWN_ROW[WHITE] = 6;
        MATERIAL_ROW[BLACK] = 0;
        MATERIAL_ROW[WHITE] = 7;
    }

    private long castleRights;
    private final long[][] rooksInitBitboards;
    private final long[] kingsInitBitBoards;
    public final long[][] kingToCastleMasks;
    public final long[][] rookToCastleMasks;

    private Deque<Long> castleRightsHistory = new ArrayDeque<>();

    private final Piece[][] board;
    private Game.GameStatus boardStatus = Game.GameStatus.IN_PLAY;
    private MoveContainer validMoves = new MoveContainer();
    private LinkedList<Piece>[] pieces = new LinkedList[2];
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

    private final long[] nullMoveInfo = {0L, ALL_ONES, 0L};

    private final long[][] posBitBoard;
    private final long[] allPosBitBoard;

    //Inputs are copied safely
    public Board(List<Piece>[] pieces, int turn) {
        this.piecesTaken[WHITE] = new ArrayDeque<>();
        this.piecesTaken[BLACK] = new ArrayDeque<>();

        this.turn = turn;

        this.pieces[BLACK] = pieces[BLACK].stream()
                .map(Piece::copy)
                .collect(Collectors.toCollection(LinkedList::new));

        this.pieces[WHITE] = pieces[WHITE].stream()
                .map(Piece::copy)
                .collect(Collectors.toCollection(LinkedList::new));

        kings = findKings(this.pieces);

        board = buildPieceBoard(this.pieces);
        posBitBoard = buildPieceBitBoards(this.pieces);
        allPosBitBoard = buildSideBitBoards(posBitBoard);

        rookStartCols = findRookStartCols(board, kings);
        kingStartCols = findKingStartCols(kings);

        kingsInitBitBoards = buildKingsInitBitboards(posBitBoard);
        rooksInitBitboards = buildRookInitBitBoards(board);
        kingToCastleMasks = buildKingToCastleMasks(kingsInitBitBoards, rooksInitBitboards);
        rookToCastleMasks = buildRookToCastleMasks(kingsInitBitBoards, rooksInitBitboards);

        castleRights = orKingsAndRooks(kingsInitBitBoards, rooksInitBitboards);
        castleRightsHistory.push(castleRights);

        initializeHashCode();
    }

    private void initializeHashCode() {
        hashCode = generateHashCode();
        hashCodeFreq = incrementHashCodeFrequency(hashCode);
    }

    private long orKingsAndRooks(long[] kingsInitBitBoards, long[][] rooksInitBitboards) {
        return kingsInitBitBoards[WHITE] |
                kingsInitBitBoards[BLACK] |
                rooksInitBitboards[WHITE][NEAR] |
                rooksInitBitboards[WHITE][FAR] |
                rooksInitBitboards[BLACK][NEAR] |
                rooksInitBitboards[BLACK][FAR];
    }

    private Piece[][] buildPieceBoard(List<Piece>[] pieces) {
        Piece[][] board = new Piece[8][8];

        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                board[r][c] = Piece.EMPTY;
            }
        }

        Stream.of(pieces)
                .flatMap(ps -> ps.stream())
                .forEach(p ->
                        board[p.getRow()][p.getCol()] = p
                );

        return board;
    }

    private long[][] buildPieceBitBoards(List<Piece>[] pieces) {
        long[][] posBitBoard = new long[PIECES_COUNT][2];
        Stream.of(pieces)
                .flatMap(List::stream)
                .forEach(p -> {
                    posBitBoard[p.getPieceID()][p.getSide()] |= p.asBitMask();
                });
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
        Stream.of(pieces).forEach(sidePieces -> {
            sidePieces.stream()
                    .filter(p -> p.getPieceID() == KING)
                    .findFirst()
                    .ifPresent(p -> kings[p.getSide()] = p);

        });
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
        final Move.MoveNote note = move.getNote();

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
            final Piece pieceTaken = board[pieceTakenRow][pieceTakenCol];

            // remove ref to piecetaken on board
            board[pieceTakenRow][pieceTakenCol] = Piece.EMPTY;

            // remove pieceTaken from vectors
            pieces[nextSide].remove(pieceTaken);
            piecesTaken[nextSide].addFirst(pieceTaken);

            posBitBoard[pieceTakenId][nextSide] ^= pieceTakenMask;
            allPosBitBoard[nextSide] ^= pieceTakenMask;

            // remove old hash from piece that was taken, if any
            hashCode ^= rngTable.getPiecePerSquareRandom(nextSide, pieceTakenId, pieceTakenRow, pieceTakenCol);
        }

        if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

            final Piece king = kings[turn];
            final Piece rook;

            if (note == Move.MoveNote.CASTLE_NEAR) {
                rook = board[MATERIAL_ROW[turn]][rookStartCols[turn][1]];

                movePiece(king, MATERIAL_ROW[turn], 6, Move.MoveNote.NONE);
                movePiece(rook, MATERIAL_ROW[turn], 5, Move.MoveNote.NONE);

                board[MATERIAL_ROW[turn]][6] = king;
                board[MATERIAL_ROW[turn]][5] = rook;

            } else {
                rook = board[MATERIAL_ROW[turn]][rookStartCols[turn][0]];

                movePiece(king, MATERIAL_ROW[turn], 2, Move.MoveNote.NONE);
                movePiece(rook, MATERIAL_ROW[turn], 3, Move.MoveNote.NONE);

                board[MATERIAL_ROW[turn]][2] = king;
                board[MATERIAL_ROW[turn]][3] = rook;

            }

        } else {
            movePiece(board[fromRow][fromCol], toRow, toCol, note);
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

    private void movePiece(final Piece pieceMoving, final int toRow, final int toCol, final Move.MoveNote note) {

        final long fromBit = getMask(pieceMoving.getRow(), pieceMoving.getCol());
        final long bitMove = fromBit | getMask(toRow, toCol);

        castleRights &= ~fromBit;

        // remove bit position from where piece was and add where it is now
        posBitBoard[pieceMoving.getPieceID()][pieceMoving.getSide()] ^= bitMove;
        allPosBitBoard[pieceMoving.getSide()] ^= bitMove;

        // remove old hash from where piece was
        hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), pieceMoving.getRow(), pieceMoving.getCol());

        // remove pieces old position
        board[pieceMoving.getRow()][pieceMoving.getCol()] = Piece.EMPTY;
        // update board to reflect piece's new position
        board[toRow][toCol] = pieceMoving;

        // tell piece its new position
        pieceMoving.move(toRow, toCol);

        if (note == Move.MoveNote.NEW_QUEEN) {
            pieceMoving.setPieceID(QUEEN);
            posBitBoard[PAWN][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
            posBitBoard[QUEEN][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
        } else if (note == Move.MoveNote.NEW_KNIGHT) {
            pieceMoving.setPieceID(KNIGHT);
            posBitBoard[PAWN][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
            posBitBoard[KNIGHT][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
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
        final Move.MoveNote note = lastMove.getNote();

        // last move made was made by previous player, which is also the next
        // player
        final int prevSide = turn;
        turn = Side.otherSide(turn);

        if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

            final Piece king = kings[turn];
            final Piece rook;

            if (note == Move.MoveNote.CASTLE_FAR) {
                rook = board[MATERIAL_ROW[turn]][3];

                undoMovePiece(king, MATERIAL_ROW[turn], kingStartCols[turn], Move.MoveNote.NONE);
                undoMovePiece(rook, MATERIAL_ROW[turn], rookStartCols[turn][0], Move.MoveNote.NONE);

                board[MATERIAL_ROW[turn]][kingStartCols[turn]] = king;
                board[MATERIAL_ROW[turn]][rookStartCols[turn][0]] = rook;

            } else {

                rook = board[MATERIAL_ROW[turn]][5];

                undoMovePiece(king, MATERIAL_ROW[turn], kingStartCols[turn], Move.MoveNote.NONE);
                undoMovePiece(rook, MATERIAL_ROW[turn], rookStartCols[turn][1], Move.MoveNote.NONE);

                board[MATERIAL_ROW[turn]][kingStartCols[turn]] = king;
                board[MATERIAL_ROW[turn]][rookStartCols[turn][1]] = rook;
            }

        } else {
            undoMovePiece(board[toRow][toCol], fromRow, fromCol, note);
        }

        if (lastMove.hasPieceTaken()) {
            // add taken piece back to vectors and board
            final Piece pieceTaken = piecesTaken[prevSide].removeFirst();

            pieces[prevSide].add(pieceTaken);

            // add piece taken to position bit board
            posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] |= pieceTaken.asBitMask();
            allPosBitBoard[pieceTaken.getSide()] |= pieceTaken.asBitMask();

            board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;
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

    private void undoMovePiece(final Piece pieceMoving, final int fromRow, final int fromCol, final Move.MoveNote note) {

        final long bitMove = getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ getMask(fromRow, fromCol);

        // remove bit position from where piece was and add where it is now
        posBitBoard[pieceMoving.getPieceID()][pieceMoving.getSide()] ^= bitMove;
        allPosBitBoard[pieceMoving.getSide()] ^= bitMove;

        // remove old position
        board[pieceMoving.getRow()][pieceMoving.getCol()] = Piece.EMPTY;
        // put piece in old position
        board[fromRow][fromCol] = pieceMoving;

        // tell piece where it was
        pieceMoving.unmove(fromRow, fromCol);

        if (note == Move.MoveNote.NEW_QUEEN) {
            pieceMoving.setPieceID(PAWN);
            posBitBoard[PAWN][pieceMoving.getSide()] |= pieceMoving.asBitMask();
            posBitBoard[QUEEN][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
        } else if (note == Move.MoveNote.NEW_KNIGHT) {
            pieceMoving.setPieceID(PAWN);
            posBitBoard[PAWN][pieceMoving.getSide()] |= pieceMoving.asBitMask();
            posBitBoard[KNIGHT][pieceMoving.getSide()] ^= pieceMoving.asBitMask();
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

    public MoveContainer generateValidMoves() {
        return generateValidMoves(null, AI.noKillerMoves, this.validMoves);
    }

    public MoveContainer generateValidMoves(MoveContainer validMoves) {
        return generateValidMoves(null, AI.noKillerMoves, validMoves);
    }

    public MoveContainer generateValidMoves(Move hashMove, Move[] killerMoves) {
        return generateValidMoves(hashMove, killerMoves, this.validMoves);
    }

    public MoveContainer generateValidMoves(Move hashMove, Move[] killerMoves, MoveContainer validMoves) {

        validMoves.clear();

        int prevMovesSize = 0;

        Move move;
        for (Piece p : pieces[turn]) {

            p.generateValidMoves(this, nullMoveInfo, allPosBitBoard, validMoves);

            for (int m = prevMovesSize; m < validMoves.size(); m++) {
                move = validMoves.get(m);

                if (move == hashMove) {
                    move.setValue(10000);
                    validMoves.set(m, move);
                    continue;
                }

                for (int k = 0; k < killerMoves.length; k++) {
                    if (move == killerMoves[k]) {
                        move.setValue(9999 - k);
                        break;
                    }
                }

                validMoves.set(m, move);
            }

            prevMovesSize = validMoves.size();
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
        // long nullMoveAttacks = 0;
        // long inCheckArrayList = ALL_ONES;
        // long bitAttackCompliment = 0;
        //
        // nullMoveInfo[0] = nullMoveAttacks;
        // nullMoveInfo[1] = inCheckArrayList;
        // nullMoveInfo[2] = bitAttackCompliment;

        // recalculating check info
        clearBoardStatus();
        pieces[turn].forEach(Piece::clearBlocking);

        final int otherSide = Side.otherSide(turn);
        final long friendOrFoe = allPosBitBoard[0] | allPosBitBoard[1];
        nullMoveInfo[0] = Pawn.getPawnAttacks(posBitBoard[PAWN][otherSide], otherSide);
        nullMoveInfo[0] |= Knight.getKnightAttacks(posBitBoard[KNIGHT][otherSide]);
        nullMoveInfo[0] |= King.getKingAttacks(posBitBoard[KING][otherSide]);

        Queen.getQueenAttacks(posBitBoard[QUEEN][otherSide], friendOrFoe, nullMoveInfo);
        Rook.getRookAttacks(posBitBoard[ROOK][otherSide], friendOrFoe, nullMoveInfo);
        Bishop.getBishopAttacks(posBitBoard[BISHOP][otherSide], friendOrFoe, nullMoveInfo);

        final long jumperAttacks = Pawn.getPawnAttacks(posBitBoard[KING][turn], turn) & posBitBoard[PAWN][otherSide] |
                Knight.getKnightAttacks(posBitBoard[KING][turn]) & posBitBoard[KNIGHT][otherSide];

        nullMoveInfo[1] = jumperAttacks == 0 ? ~posBitBoard[KING][turn] : (hasOneBitOrLess(jumperAttacks) ? jumperAttacks : 0);
        nullMoveInfo[2] = 0;

        King.getKingCheckInfo(this,
                posBitBoard[KING][turn],
                posBitBoard[QUEEN][otherSide],
                posBitBoard[ROOK][otherSide],
                posBitBoard[BISHOP][otherSide],
                friendOrFoe,
                nullMoveInfo);

        if ((kings[turn].asBitMask() & nullMoveInfo[0]) != 0) {
            setBoardStatus(Game.GameStatus.CHECK);
        }

        return nullMoveInfo;
    }

    public long[] getNullMoveInfo() {
        return nullMoveInfo;
    }

    public Move getLastMoveMade() {
        return moveHistory.isEmpty() ? Move.EMPTY_MOVE : moveHistory.peek();
    }

    public MoveContainer getMoveHistory() {
        return moveHistory;
    }

    public List<Piece>[] getPieces() {
        return this.pieces;
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public int getPieceID(int row, int col) {
        return board[row][col].getPieceID();
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

        return rooksInitBitboards;
    }

    private int[][] findRookStartCols(Piece[][] board, Piece[] kings) {

        int[][] rookCols = {{-1, -1}, {-1, -1}};

        for (int s : Arrays.asList(BLACK, WHITE)) {
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

        return rookCols;
    }

    private int[] findKingStartCols(Piece[] kings) {
        int[] kingStartCols = new int[2];
        kingStartCols[Side.BLACK] = kings[Side.BLACK].getCol();
        kingStartCols[Side.WHITE] = kings[Side.WHITE].getCol();
        return kingStartCols;
    }

    public boolean placePiece(Piece piece, int toRow, int toCol) {

        if (toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
            if (board[toRow][toCol] != null) {
                if (board[toRow][toCol].getPieceID() == KING) {
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
            // remove where piece was if it was on board
            posBitBoard[piece.getPieceID()][piece.getSide()] ^= piece.asBitMask();
            allPosBitBoard[piece.getSide()] ^= piece.asBitMask();
            board[piece.getRow()][piece.getCol()] = null;

        } else {

            pieces[piece.getSide()].add(piece);
        }

        if (toRow >= 0) {
            // remove where piece taken was
            if (board[toRow][toCol] != null) {

                Piece pieceTaken = board[toRow][toCol];

                // remove bit position of piece taken
                posBitBoard[pieceTaken.getPieceID()][pieceTaken.getSide()] ^= pieceTaken.asBitMask();
                allPosBitBoard[pieceTaken.getSide()] ^= pieceTaken.asBitMask();

                // remove ref to piece taken
                pieces[pieceTaken.getSide()].remove(pieceTaken);
            }

            // tell piece where it is now
            piece.setRow(toRow);
            piece.setCol(toCol);

            // reflect new piece in position bitboard
            posBitBoard[piece.getPieceID()][piece.getSide()] |= piece.asBitMask();
            allPosBitBoard[piece.getSide()] |= piece.asBitMask();

            // update board ref to show piece there
            board[toRow][toCol] = piece;
        } else {
            // piece is being taken off the board. Remove
            if (piece.getPieceID() != KING) {
                pieces[piece.getSide()].remove(piece);
            }
        }

        // basically start over with new board
        this.moveHistory.clear();
        this.hashCodeHistory.clear();

        this.hashCode = generateHashCode();

        return true;
    }

    public boolean isInCheck() {
        return (boardStatus == Game.GameStatus.CHECK);
    }

    public boolean isInCheckMate() {
        return (boardStatus == Game.GameStatus.CHECKMATE);
    }

    public boolean isInStaleMate() {
        return (boardStatus == Game.GameStatus.STALEMATE);
    }

    public boolean isTimeUp() {
        return (boardStatus == Game.GameStatus.TIMES_UP);
    }

    public boolean isDraw() {
        return (boardStatus == Game.GameStatus.DRAW);
    }

    public boolean isInvalid() {
        return (boardStatus == Game.GameStatus.INVALID);
    }

    public boolean isGameOver() {
        return (isInCheckMate() || isInStaleMate() || isTimeUp() || isDraw() || isInvalid());
    }

    public void clearBoardStatus() {
        boardStatus = Game.GameStatus.IN_PLAY;
    }

    public Game.GameStatus getBoardStatus() {
        return boardStatus;
    }

    public void setBoardStatus(Game.GameStatus boardStatus) {
        this.boardStatus = boardStatus;
    }

    public void applyCastleRights(int player, boolean nearRights, boolean farRights) {

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

        Board boardCopy = new Board(pieces, turn);

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
                if (board[row][col].getPieceID() != Piece.PieceID.NONE) {
                    stringBoard += board[row][col].toString() + ",";
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
                p = board[r][c];
                if (p.getPieceID() != Piece.PieceID.NONE) {
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

        for (List<Piece> ps : pieces) {
            for (Piece p : ps) {
                if ((p.getPieceID() == PAWN) || (p.getPieceID() == QUEEN) || (p.getPieceID() == ROOK)) {
                    return false;
                }
            }
        }

        return true;
    }

}
